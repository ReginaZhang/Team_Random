(ns forum.core
  (:require [clojure.java.jdbc :as jdb]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :as json]
            [ring.middleware.params :as prms]
            [bidi.bidi :as bidi])
  (:gen-class))

(def health-db {:subprotocol "mysql"
               :subname "//45.56.85.191:3306/HealthDB"
               :user "kimjongun"
               :password "KimJongUnIsGreat"})
  
(defn merge-comments-with-flags [acc cur]
  (let [id (:commentid cur)
        flag (:flagid cur)]
    (if (contains? acc id)
      (update-in acc [id :flagids] conj flag)
      (assoc acc id (assoc cur :flagids (if flag [flag] []))))))

(defn get-child-comments-db [parent-id]
  (sort-by :commentid (vals (reduce merge-comments-with-flags {}
          (jdb/query health-db
             ["select * from Comment left join CommentFlag 
on CommentFlag.CommentId = Comment.CommentId where ParentId = ? order by Comment.CommentId" parent-id]
             :row-fn #(let [comment (select-keys % [:commentid :commenttext :userid :flagid :parentid :deleted])]
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

(defn delete-comment [{{:strs [comment_id]} :params}]
  (jdb/update! health-db :Comment {:Deleted 1} ["CommentId = ?" comment_id])
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

(defn get_child_comments [{{:strs [parent_id]} :params}]
  {:status 200
   :body (get-child-comments-db parent_id)})


(defn add_comment [{{:strs [parent_id question_id text user_id]} :params}]
  (jdb/insert! health-db :Comment {:ParentId parent_id
                                   :QuestionId question_id
                                   :CommentText text
                                   :UserId user_id
                                   :CommentDeleted false
                                   :NumChildren 0})
    
  {:status 200
   :body {:text "Successfully added comment!"}})

(defn index [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (slurp "static/index.html")})

(defn serve_js [request]
  {:status 200
   :headers {"Content-Type" "text/javascript"}
   :body (slurp "static/js/cljs.js")})

(defn rest-wrap [handler] (-> handler
                                   (json/wrap-json-response)
                                   (prms/wrap-params)))

(def routes ["/" {"child_comments" :child-comments
                  "add_comment" :add-comment
                  "delete_comment" :delete-comment
                  "edit_comment" :edit-comment
                  "flag_types" :flag-types
                  "flag_comment" :flag-comment
                  "index" :index
                  "static/js/cljs.js" :serve_js}])

(defn forum [request]
  (if-let [match (bidi/match-route routes (:uri request))]
    (let [handler (:handler match)
          handler-fn       
          (get {:child-comments (rest-wrap get_child_comments)
                :add-comment (rest-wrap add_comment)
                :delete-comment (rest-wrap delete-comment)
                :edit-comment (rest-wrap edit-comment)
                :flag-types (rest-wrap get-flag-types)
                :flag-comment (rest-wrap update-comment-flags)
                :index index
                :serve_js serve_js}
               handler)]
      (handler-fn request))
    {:status 404 :body "404 page not found"}))
