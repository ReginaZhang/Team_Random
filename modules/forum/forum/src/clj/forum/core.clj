(ns forum.core
  (:require [clojure.java.jdbc :as jdb]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.json :as json]
            [ring.middleware.params :as prms]
            [bidi.ring :as bidi])
  (:gen-class))

(def health-db {:subprotocol "mysql"
               :subname "//45.56.85.191:3306/HealthDB"
               :user "kimjongun"
               :password "KimJongUnIsGreat"})
  
(defn get-child-comments-db-2 [parent-id]
  (let [comments (jdb/query health-db
                            ["SELECT * from Comment where ParentId = ?" parent-id]
                            :row-fn #(select-keys % [:commentid :text :userid]))]
    (map (fn [comment]
           (assoc comment :flags
                  (jdb/query health-db
                              ["SELECT * from CommentFlag where CommentId = ?" (:commentid comment)]
                              :row-fn #(select-keys % [:flagid]))))
         comments)))

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
             :row-fn #(select-keys % [:commentid :text :userid :flagid]))))))


(defn update-comment-flags [{{:strs [flag_ids comment_id user_id]} :params}]
  (jdb/delete! health-db :CommentFlag ["CommentId = ? and UserId = ?" comment_id user_id])
  (let [vecs (map (fn [flag-id] [user_id comment_id flag-id]) flag_ids)
        insert (fn [& vals]
                 (apply jdb/insert! health-db :CommentFlag
                                     [:UserId :CommentId :FlagId]
                                     vals))]
    (apply insert vecs)
    {:status 200 :body {:text "Successfully updated comment flags!"}}))


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
                                   :Text text
                                   :UserId user_id})
    
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

(def forum (bidi/make-handler ["/" {"child_comments" (rest-wrap get_child_comments)
                                    "add_comment" (rest-wrap add_comment)
                                    "flag_types" (rest-wrap get-flag-types)
                                    "flag_comment" (rest-wrap update-comment-flags)
                                    "index" index
                                    "static/js/cljs.js" serve_js
                                    #".*" (fn [_]
                                            {:status 404
                                             :body "404 Page not found"})}]))

(let [{{:strs [a d]} :params} {:params {"a" "A", "b" "B", "c" "C", "d" "D"}}]
  (println a d))
