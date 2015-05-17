(ns forum.core
  (:require [clojure.java.jdbc :as jdb]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp]
            [ring.middleware.json :as json]
            [ring.middleware.params :as prms]
            [noir.util.crypt :as crypt]
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

(defn get_questions [{{:strs [user_id question_id]} :params}]
  (let [query (if question_id ["SELECT * from Question natural join Comment
left join Vote on Vote.CommentId = Comment.CommentId and Vote.UserId = ? left join CommentFlag 
on CommentFlag.CommentId = Comment.CommentId  where ParentId is NULL and QuestionId = ?" user_id question_id]
                  ["SELECT * from Question natural join Comment
left join Vote on Vote.CommentId = Comment.CommentId and Vote.UserId = ? left join CommentFlag 
on CommentFlag.CommentId = Comment.CommentId  where ParentId is NULL" user_id])]
    {:status 200
     :body
     (vals (reduce merge-comments-with-flags {}
                   (jdb/query health-db
                              query
                              :row-fn #(select-keys % [:questionid :questiondeleted :userid :questiontitle
                                                       :commentid :commenttext :votetype :score :flagid]))))}))

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

(defn register-user [{{:strs [username email password weight height address]} :params}]
  (if (first (jdb/query health-db ["select UserId from User where UserName = ?" username]))
    {:status 200 :headers cors-headers :body {:error "User already exists!"}}
    (let [[{addr-id :generated_key}] (jdb/insert! health-db :Address {:Content address})
          passwd-hash (crypt/encrypt password)
          [{id :generated_key}] (jdb/insert! health-db :User {:Email email
                                           :UserName username
                                           :Weight weight 
                                           :Height height
                                           :Password passwd-hash
                                           :AddressId addr-id})]
      {:status 200 :headers cors-headers :body {:id id}})))

(defn check-user-credentials [{{:strs [username password]} :params}]
  (let [[{db-password :password user-id :userid}] (jdb/query health-db ["select Password, UserId from User where UserName = ?" username])
        matches (crypt/compare password db-password)]
    {:status 200 :headers cors-headers :body {:id (if matches user-id nil)}}))
   

      
(def loggedin-users (atom {}))
(defn login-user [{{:strs [user_id ip header]} :params {req-agent "user-agent" req-ip "x-forwarded-for"} :headers}]
  (swap! loggedin-users #(assoc % [(if ip ip req-ip), (if header header req-agent)] user_id))
  {:status 200 :headers cors-headers :body {:text "User recorded as logged in"}})

(defn check-loggedin [{{:strs [ip header]}  :params {req-agent "user-agent" req-ip "x-forwarded-for"} :headers}]
  (let [id (get @loggedin-users [(if ip ip req-ip), (if header header req-agent)])
        details (if id (jdb/query health-db
                                  ["select * from User where UserId = ?" id]
                                  :row-fn #(dissoc % :password))
                    nil)]
    {:status 200 :headers cors-headers
     :body {:id id :details details}}))

(defn logout-user [{{:strs [ip header]}  :params {req-agent "user-agent" req-ip "x-forwarded-for"} :headers}]
  (swap! loggedin-users #(assoc % [(if ip ip req-ip), (if header header req-agent)] nil))
  {:status 200 :headers cors-headers :body {:text "User recorded as logged out"}})

(defn showmy-ip [{{ip "x-forwarded-for"} :headers :as req}]
    {:status 200 :headers cors-headers :body {:text (str "ip is " ip " total req is: " req)}})

(defn get-recommendation [{{:strs [user_id]} :params}]
  ;Recommended values from http://en.wikipedia.org/wiki/Reference_Daily_Intake
  (let [nutrients {:niacin 16, :iron 18, :thiamin 1.2, :vitaminb6 1.7, :carbohydratebydifference 300, :calcium 1300, :vitaminctotalascorbicacid 90, :sodium 2400, :phosphorus 1250, :vitaminarae 900, :potassium 4700, :riboflavin 1.3, :magnesium 420, :cholesterol 300, :fibertotaldietary 25, :vitaminb12 2.4, :energy 2000, :totallipid_fat 65, :zinc 11, :protein 50, :folatedfe 400}
        nut-arr (into [] (keys nutrients))
        n+ (fnil + 0 0)
        current-nutrition (apply merge-with n+
                                 (jdb/query health-db
                                            ["SELECT * from Diet natural join DietItem inner join Food on DietItem.Ndbno = Food.Ndbno where UserId = ?" user_id]
                                            :row-fn #(select-keys % nut-arr)))
        possible-foods (jdb/query health-db ["SELECT * from Food limit 100"]
                                  :row-fn #(select-keys % (conj nut-arr :foodname :foodid)))
        recommended-food (apply min-key (fn [food]
                                          (let [strip (fn [m] (dissoc m :foodname :foodid))]
                                            (reduce (fnil + 0 0)
                                                    (vals (merge-with #(Math/abs ((fnil - 0 0) %1 %2)) nutrients
                                                                      (merge-with n+ (strip food) (strip current-nutrition)))))))
                                possible-foods)]
    {:status 200 :headers cors-headers
     :body recommended-food}))


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
                  "login_user" :login-user
                  "logout_user" :logout-user
                  "check_loggedin" :check-loggedin
                  "show_my_ip" :show-my-ip
                  "diet_recommendation" :diet-recommendation
                  "register_user" :register-user
                  "check_user_credentials" :check-user-credentials}])

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
                :login-user (rest-wrap login-user)
                :logout-user (rest-wrap logout-user)
                :check-loggedin (rest-wrap check-loggedin)
                :show-my-ip (rest-wrap showmy-ip)
                :diet-recommendation (rest-wrap get-recommendation)
                :register-user (rest-wrap register-user)
                :check-user-credentials (rest-wrap check-user-credentials)}
               handler)]
      (handler-fn request))
    {:status 404 :body "404 page not found"}))

;(defn -main []
 ; (hkit/run-server forum {:port 3000}))

