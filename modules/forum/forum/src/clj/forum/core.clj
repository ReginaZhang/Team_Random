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
  
(defn get-child-comments-db [parent-id]
  (jdb/query health-db
             ["SELECT * from Comment where ParentId = ?" parent-id]
             :row-fn #(select-keys % [:commentid :text :userid])))

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

(defn rest_wrap [handler] (-> handler
                                   (json/wrap-json-response)
                                   (prms/wrap-params)))

(def forum (bidi/make-handler ["/" {"child_comments" (rest_wrap get_child_comments)
                                    "add_comment" (rest_wrap add_comment)
                                    "index" index
                                    "static/js/cljs.js" serve_js
                                    #".*" (fn [_]
                                            {:status 404
                                             :body "404 Page not found"})}]))

(let [{{:strs [a d]} :params} {:params {"a" "A", "b" "B", "c" "C", "d" "D"}}]
  (println a d))
