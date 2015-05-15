(ns forum.core
  (:require [clojure.java.jdbc :as jdb]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp]
            [ring.middleware.json :as json]
            [ring.middleware.params :as prms]
            [bidi.bidi :as bidi])
  (:gen-class))

(def health-db {:subprotocol "mysql"
                :subname "//45.56.85.191/HealthDB"
                :user "kimjongun"
                :password "KimJongUnIsGreat"})
  
(defn merge-comments-with-flags [acc cur]
  (let [id (:commentid cur)
        flag (:flagid cur)]
    (if (contains? acc id)
      (update-in acc [id :flagids] conj flag)
      (assoc acc id (assoc cur :flagids (if flag [flag] []))))))

(defn get-child-comments-db [parent-id user-id]
  (sort-by :commentid (vals (reduce merge-comments-with-flags {}
          (jdb/query health-db
             ["select * from Comment left join Vote on Vote.CommentId = Comment.CommentId and Vote.UserId = ? left join CommentFlag 
on CommentFlag.CommentId = Comment.CommentId 
where ParentId = ? order by Comment.CommentId" user-id parent-id]
             :row-fn #(let [comment (select-keys % [:commentid :commenttext :userid :flagid :parentid :score :deleted :votetype])]
                        (assoc comment :text (if (:deleted comment) "!!DELETED!!" (:commenttext comment)))))))))


(defn update-comment-flags [{{:strs [flag_ids comment_id user_id]} :params}]
  (let [flag-ids (if (vector? flag_ids) flag_ids [flag_ids])]
    (jdb/delete! health-db :CommentFlag ["CommentId = ? and UserId = ?" comment_id user_id])
    (when (not= flag_ids "null")
      (let [vecs (map (fn [flag-id] [user_id comment_id flag-id]) flag-ids)
            insert (fn [& vals]
                     (apply jdb/insert! health-db :CommentFlag
                            [:UserId :CommentId :FlagId]
                            vals))]
        (if (seq? vecs) (apply insert vecs) (insert vecs))))
    {:status 200 :body {:text "Successfully updated comment flags!"}}))

(defn update-comment-vote [{{:strs [comment_id user_id vote_type]} :params}]
  ;;TODO: Don't allow another upvote if upvote already exists
  (if (and (not= vote_type "up") (not= vote_type "down"))
    (:status 404 :body {:text (str "Incorrect vote type " vote_type)})
    (do
      (jdb/delete! health-db :Vote ["CommentId = ? and UserId = ?" comment_id user_id])
      (jdb/insert! health-db :Vote {:CommentId comment_id
                                    :UserId user_id
                                    :VoteType vote_type})
      (jdb/execute! health-db ["UPDATE Comment SET Score = Score + ? WHERE CommentId = ?" (if (= vote_type "up") 1 -1) comment_id])
      {:status 200 :body {:text "Successfully updated comment vote!"}})))

(defn delete-comment [{{:strs [comment_id]} :params}]
  (jdb/update! health-db :Comment {:CommentDeleted 1} ["CommentId = ?" comment_id])
  {:status 200 :body {:text "Successfully deleted comment!"}})

(defn edit-comment [{{:strs [comment_id text]} :params}]
  (jdb/update! health-db :Comment {:CommentText text} ["CommentId = ?" comment_id])
  {:status 200 :body {:text "Successfully edited comment!"}})

(defn get-flag-types [_]
  {:status 200
   :body
   (jdb/query health-db
              ["SELECT * from FlagType"]
              :row-fn #(select-keys % [:flagid :flagname]))})

(defn get_child_comments [{{:strs [parent_id user_id]} :params}]
  {:status 200
   :body (get-child-comments-db parent_id user_id)})


(defn add_comment [{{:strs [parent_id question_id text user_id]} :params}]
  (jdb/insert! health-db :Comment {:ParentId parent_id
                                   :QuestionId question_id
                                   :CommentText text
                                   :UserId user_id
                                   :CommentDeleted false
                                   :NumChildren 0
                                   :Score 0})
    
  {:status 200
   :body {:text "Successfully added comment!"}})

(defn get_questions [{{:strs [user_id]} :params}]
  {:status 200
   :body
   (vals (reduce merge-comments-with-flags {}
                 (jdb/query health-db
                            ["SELECT * from Question natural join Comment
left join Vote on Vote.CommentId = Comment.CommentId and Vote.UserId = ? left join CommentFlag 
on CommentFlag.CommentId = Comment.CommentId  where ParentId is NULL" user_id]
                            :row-fn #(select-keys % [:questionid :questiondeleted :userid :questiontitle
                                                     :commentid :commenttext :votetype :score :flagid]))))})

(defn add_question [{{:strs [text user_id title]} :params}]
  (let [[{question_id :generated_key}] (jdb/insert! health-db :Question
                                                    {:QuestionDeleted false
                                                     :QuestionTitle title})]
    (add_comment {:params {"parent_id" nil "question_id" question_id "text" text
                           "user_id" user_id}})
    {:status 200 :body {:text "Successfully added question!"}}))

(defn index [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (slurp "static/index.html")})

(defn forum [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (slurp "static/index.html")})

(defn mk-serve-js [jsfile]
  (fn [request]
    (let [path (str "static/js/" jsfile ".js")]
      (resp/content-type (resp/file-response path) "text/javascript"))))

(defn mk-serve-css [cssfile]
  (fn [request]
    {:status 200
     :headers {"Content-Type" "text/css"}
     :body (slurp (str "static/css/" cssfile ".css"))}))

(defn mk-serve-asset [assetfile]
      (fn [request]
          {:status 200
           :headers {"Content-Type" "image/gif"}
           :body (slurp (str "static/assets/" assetfile))}))

(defn mk-serve-png [pngfile]
  (fn [request]
    (let [path (str "static/resources/" pngfile ".png")]
      (resp/content-type (resp/file-response path) "image/png"))))


(defn rest-wrap [handler] (-> handler
                                   (json/wrap-json-response)
                                   (prms/wrap-params)))

(def cors-headers {"Access-Control-Allow-Origin" "*"
                   "Access-Control-Allow-Methods" "GET, POST"})

(defn check-credentials [username passwd]
  (let [digest (java.security.MessageDigest/getInstance "SHA")]
    (.update digest (.getBytes passwd))
    (let [encrypted (.digest digest)
          str-encrypted (new String encrypted)
          [user] (jdb/query health-db
                          ["select Password from User where UserName = ?" (.toLowerCase username)]
                          :row-fn #(select-keys % [:password]))]
      (print (str user " " str-encrypted))
      (.equals (:password user) str-encrypted))))

(def loggedin-users (atom {}))
(defn login-user [{{:strs [user_id ip-prm agent-prm]} :params ip :remote-addr {agent "user-agent"} :headers}]
  (swap! loggedin-users #(assoc % [(if ip-prm ip-prm ip), (if agent-prm agent-prm agent)] user_id))
  {:status 200 :headers cors-headers :body {:text "User recorded as logged in"}})

(defn check-loggedin [{{:strs [ip-prm agent-prm]} :params ip :remote-addr {agent "user-agent"} :headers}]
  {:status 200 :headers cors-headers
   :body {:id (get @loggedin-users [(if ip-prm ip-prm ip), (if agent-prm agent-prm agent)])}})

(defn logout-user [{{:strs [ip-prm agent-prm]} :params ip :remote-addr {agent "user-agent"} :headers}]
  (swap! loggedin-users #(assoc % [(if ip-prm ip-prm ip), (if agent-prm agent-prm agent)] nil))
  {:status 200 :headers cors-headers :body {:text "User recorded as logged out"}})

(defn showmy-ip [{ip :remote-addr}]
    {:status 200 :headers cors-headers :body {:text (str ip)}})

(def routes ["/" {"child_comments" :child-comments
                  "add_comment" :add-comment
                  "delete_comment" :delete-comment
                  "edit_comment" :edit-comment
                  "flag_types" :flag-types
                  "flag_comment" :flag-comment
                  "vote_for" :vote-for
                  "questions" :questions
                  "add_question" :add-question
                  "index" :index
                  ["forum/" :question-id] :forum
                  "login_user" :login-user
                  "logout_user" :logout-user
                  "check_loggedin" :check-loggedin
                  "show_my_ip" :show-my-ip
                  ["static/js/" :jsfile ".js"] :serve_js
                  ["static/css/" :cssfile ".css"] :serve_css
                  ["static/assets/" :pngfile ".png"] :serve_png
                  ["static/assets/" :assetfile] :serve_asset}])

(defn forum [request]
  (if-let [match (bidi/match-route routes (:uri request))]
    (let [handler (:handler match)
          params (:route-params match)
          handler-fn       
          (get {:child-comments (rest-wrap get_child_comments)
                :add-comment (rest-wrap add_comment)
                :delete-comment (rest-wrap delete-comment)
                :edit-comment (rest-wrap edit-comment)
                :flag-types (rest-wrap get-flag-types)
                :flag-comment (rest-wrap update-comment-flags)
                :vote-for (rest-wrap update-comment-vote)
                :questions (rest-wrap get_questions)
                :add-question (rest-wrap add_question)
                :index index
                :forum index
                :login-user (rest-wrap login-user)
                :logout-user (rest-wrap logout-user)
                :check-loggedin (rest-wrap check-loggedin)
                :show-my-ip (rest-wrap showmy-ip)
                :serve_js (mk-serve-js (:jsfile params))
                :serve_css (mk-serve-css (:cssfile params))
                :serve_png (mk-serve-png (:pngfile params))
                :serve_asset (mk-serve-asset (:assetfile params))}
               handler)]
      (handler-fn request))
    {:status 404 :body "404 page not found"}))

;(defn -main []
 ; (hkit/run-server forum {:port 3000}))

