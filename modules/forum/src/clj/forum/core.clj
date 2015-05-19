(ns forum.core
  (:require [clojure.java.jdbc :as jdb]
            [ring.adapter.jetty :as jetty]
            [ring.util.response :as resp]
            [ring.middleware.json :as json]
            [ring.middleware.params :as prms]
            [noir.util.crypt :as crypt]
            [bidi.bidi :as bidi])
  (:gen-class))

;;The connection options for the db, for use with JDBC
(def health-db {:subprotocol "mysql"
                :subname "//45.56.85.191/HealthDB"
                :user "kimjongun"
                :password "KimJongUnIsGreat"})

(defn merge-comments-with-flags
  "Helper function for combining a list of comments with a list of flags
  Used as the accumulator function for a fold/reduce function"
  [acc cur]
  (let [id (:commentid cur)
        flag (:flagid cur)]
    (if (contains? acc id)
      (update-in acc [id :flagids] conj flag)
      (assoc acc id (assoc cur :flagids (if flag [flag] []))))))

(defn get-child-comments-db
  "Get the child comments of a given parent comment, and the flags given
  to them by the specified user"
  [parent-id user-id]
  (sort-by :commentid (vals (reduce merge-comments-with-flags {}
          (jdb/query health-db
             ["select * from Comment left join Vote on Vote.CommentId = Comment.CommentId and Vote.UserId = ? left join CommentFlag 
on CommentFlag.CommentId = Comment.CommentId 
where ParentId = ? order by Comment.CommentId" user-id parent-id]
             :row-fn #(let [comment (select-keys % [:commentid :commenttext :userid :flagid :parentid :score :deleted :votetype])]
                        (assoc comment :text (if (:deleted comment) "!!DELETED!!" (:commenttext comment)))))))))


(defn update-comment-flags
  "Add the given flags (flagids) to a comment, by the given user"
  [{{:strs [flag_ids comment_id user_id]} :params}]
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

(defn update-comment-vote
  "Vote for the given comment, replacing the previous vote by the given user if one exists"
  ;;TODO: Don't allow another upvote if upvote already exists. The frontend already blocks this, but better safe than sorry.
  [{{:strs [comment_id user_id vote_type]} :params}]
  (if (and (not= vote_type "up") (not= vote_type "down"))
    (:status 404 :body {:text (str "Incorrect vote type " vote_type)})
    (do
      (jdb/delete! health-db :Vote ["CommentId = ? and UserId = ?" comment_id user_id])
      (jdb/insert! health-db :Vote {:CommentId comment_id
                                    :UserId user_id
                                    :VoteType vote_type})
      (jdb/execute! health-db ["UPDATE Comment SET Score = Score + ? WHERE CommentId = ?" (if (= vote_type "up") 1 -1) comment_id])
      {:status 200 :body {:text "Successfully updated comment vote!"}})))

(defn delete-comment
  "Delete the given comment, setting is deleted flag to true"
  [{{:strs [comment_id]} :params}]
  (jdb/update! health-db :Comment {:CommentDeleted 1} ["CommentId = ?" comment_id])
  {:status 200 :body {:text "Successfully deleted comment!"}})

(defn edit-comment
  "Edit a comment, changing its text"
  ;;Todo: update scheme to allow keeping of old comment texts, by having commenttext as a separate, dated entry
  [{{:strs [comment_id text]} :params}]
  (jdb/update! health-db :Comment {:CommentText text} ["CommentId = ?" comment_id])
  {:status 200 :body {:text "Successfully edited comment!"}})

(defn get-flag-types
  "Return all the FlagTypes in the db, their ids and names"
  [_]  
  {:status 200
   :body
   (jdb/query health-db
              ["SELECT * from FlagType"]
              :row-fn #(select-keys % [:flagid :flagname]))})

(defn get_child_comments
  "Get the child comments of a given comment"
  [{{:strs [parent_id user_id]} :params}]
  {:status 200
   :body (get-child-comments-db parent_id user_id)})


(defn add_comment
  "Add a new comment to the db"
  [{{:strs [parent_id question_id text user_id]} :params}]
  (jdb/insert! health-db :Comment {:ParentId parent_id
                                   :QuestionId question_id
                                   :CommentText text
                                   :UserId user_id
                                   :CommentDeleted false
                                   :NumChildren 0
                                   :Score 0})
    
  {:status 200
   :body {:text "Successfully added comment!"}})

(defn get_questions
  "Get all questions from the db, and what the given user has voted them.
  The question_id field is optional, and if supplied will only get one question."
  [{{:strs [user_id question_id]} :params}]
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

(defn add_question
  "Add a question to the db"
  [{{:strs [text user_id title]} :params}]
  (let [[{question_id :generated_key}] (jdb/insert! health-db :Question
                                                    {:QuestionDeleted false
                                                     :QuestionTitle title})]
    (add_comment {:params {"parent_id" nil "question_id" question_id "text" text
                           "user_id" user_id}})
    {:status 200 :body {:text "Successfully added question!"}}))

(defn rest-wrap
  "Wrap a Ring handler so that it returns a JSON response and stores
  the incoming params in a :params key"
  [handler] (-> handler
                (json/wrap-json-response)
                (prms/wrap-params)))

(def cors-headers {"Access-Control-Allow-Origin" "*"
                   "Access-Control-Allow-Methods" "GET, POST"})

(defn check-credentials
  "Check a username and password against the db.
  Not currently used, as encoding issues seem to make the hash not match perfectly
  when it should"
  [username passwd]  
  (let [digest (java.security.MessageDigest/getInstance "SHA")]
    (.update digest (.getBytes passwd))
    (let [encrypted (.digest digest)
          str-encrypted (new String encrypted)
          [user] (jdb/query health-db
                          ["select Password from User where UserName = ?" (.toLowerCase username)]
                          :row-fn #(select-keys % [:password]))]
      (print (str user " " str-encrypted))
      (.equals (:password user) str-encrypted))))

(defn register-user
  "Add a user to the DB. Uses a different password hashing scheme, and so is not currently used.
  Was just created as a backup in case the other one didn't end up working."
  [{{:strs [username email password weight height address]} :params}]
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

(defn check-user-credentials
  "Check a user's credentials against hte DB. Uses a different password hashing scheme, and so is not currently used.
  Was just created as a backup in case the other one didn't end up working."
  [{{:strs [username password]} :params}]
  (let [[{db-password :password user-id :userid}] (jdb/query health-db ["select Password, UserId from User where UserName = ?" username])
        matches (crypt/compare password db-password)]
    {:status 200 :headers cors-headers :body {:id (if matches user-id nil)}}))
   

;;In-memory store of currently logged in users, used for handling sessions.
(def loggedin-users (atom {}))

(defn login-user
  "Register a user session as logged in, based on the ip and user agent string
  These can optionally be supplied as params, which will be used instead of those of the request itself"
  [{{:strs [user_id ip header]} :params {req-agent "user-agent" req-ip "x-forwarded-for"} :headers}]
  (swap! loggedin-users #(assoc % [(if ip ip req-ip), (if header header req-agent)] user_id))
  {:status 200 :headers cors-headers :body {:text "User recorded as logged in"}})

(defn check-loggedin
  "Check whether a user session is logged in, based on the ip and user agent string
  These can optionally be supplied as params, which will be used instead of those of the request itself"
  [{{:strs [ip header]}  :params {req-agent "user-agent" req-ip "x-forwarded-for"} :headers}]
  (let [id (get @loggedin-users [(if ip ip req-ip), (if header header req-agent)])
        details (if id (jdb/query health-db
                                  ["select * from User where UserId = ?" id]
                                  :row-fn #(dissoc % :password))
                    nil)]
    {:status 200 :headers cors-headers
     :body {:id id :details details}}))

(defn logout-user
  "Register a user session as logged out, based on the ip and user agent string
  These can optionally be supplied as params, which will be used instead of those of the request itself"
  [{{:strs [ip header]}  :params {req-agent "user-agent" req-ip "x-forwarded-for"} :headers}]
  (swap! loggedin-users #(assoc % [(if ip ip req-ip), (if header header req-agent)] nil))
  {:status 200 :headers cors-headers :body {:text "User recorded as logged out"}})

(defn showmy-ip
  "Show user IP"
  [{{ip "x-forwarded-for"} :headers :as req}]
    {:status 200 :headers cors-headers :body {:text (str "ip is " ip " total req is: " req)}})

(defn update-vitals
  "Update a user's weight and height"
  [{{:strs [user_id weight height]}  :params}]
  (jdb/update! health-db :User {:Weight weight :Height height} ["UserId = ?" user_id])  
  {:status 200 :headers cors-headers :body {:text "Details updated!"}})

(defn get-recommendation
  "Get a food recommendation for the given user. The current dietitems are summed, and the food is
  picked that minimises the difference between itself plus this sum and the daily RDI values"
  [{{:strs [user_id weekday]} :params}]
  ;Recommended values from http://en.wikipedia.org/wiki/Reference_Daily_Intake
  (let [nutrients {:niacin 16, :iron 18, :thiamin 1.2, :vitaminb6 1.7, :carbohydratebydifference 300, :calcium 1300,
                   :vitaminctotalascorbicacid 90, :sodium 2400, :phosphorus 1250, :vitaminarae 900, :potassium 4700,
                   :riboflavin 1.3, :magnesium 420, :cholesterol 300, :fibertotaldietary 25, :vitaminb12 2.4,
                   :energy 2000, :totallipid_fat 65, :zinc 11, :protein 50, :folatedfe 400}
        nut-arr (into [] (keys nutrients))
        n+ (fnil + 0 0)

        ;;The current total amount of each nutrient in the user's diet
        current-nutrition (apply merge-with n+
                                 (jdb/query health-db
                                            ["SELECT * from Diet natural join DietItem inner join Food on DietItem.Ndbno = Food.Ndbno where UserId = ? and Weekday = ?" user_id weekday]
                                            :row-fn #(select-keys % nut-arr)))
        ;;The foods that could be added to his died
        possible-foods (jdb/query health-db ["SELECT * from Food limit 100"]
                                  :row-fn #(select-keys % (conj nut-arr :foodname :foodid)))

        ;;The food which, when its nutrients are added to current-nutrition, is closest to the recommend nutrition amount
        recommended-food (apply min-key (fn [food]
                                          (let [strip (fn [m] (dissoc m :foodname :foodid))]
                                            (reduce (fnil + 0 0)
                                                    (vals (merge-with #(Math/abs ((fnil - 0 0) %1 %2)) nutrients
                                                                      (merge-with n+ (strip food) (strip current-nutrition)))))))
                                possible-foods)]
    (print current-nutrition)
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
                  "check_user_credentials" :check-user-credentials
                  "update_vitals" :update-vitals}])

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
                :check-user-credentials (rest-wrap check-user-credentials)
                :update-vitals (rest-wrap update-vitals)}
               handler)]
      (handler-fn request))
    {:status 404 :body "404 page not found"}))
