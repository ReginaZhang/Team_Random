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

(defn get_child_comments2 [request]
  {:status 200
   :body (let [prms
               (get-in request [:params "parent_id"])]
           (get-child-comments-db prms))})

(defn add_comment [{{:strs [parent_id question_id text user_id]} :params}]
  (jdb/insert! health-db :Comment {:ParentId parent_id
                                   :QuestionId question_id
                                   :Text text
                                   :UserId user_id})
    
  {:status 200
   :body "Successfully added comment!"})

(defn add_comment2 [request]
  (letfn [(get [prm]
             (get-in request [:params prm]))]
    (jdb/insert! health-db :Comment {:ParentId (get "parent_id")
                                     :QuestionId (get "question_id")
                                     :Text (get "text")
                                     :UserId (get "user_id")}))
    
  {:status 200
   :body "Successfully added comment!"})

(defn rest_wrap [handler] (-> handler
                                   (json/wrap-json-response)
                                   (prms/wrap-params)))

(def forum (bidi/make-handler ["/" {"child_comments" (rest_wrap get_child_comments)
                                    "add_comment" (rest_wrap add_comment)
                                    #".*" (fn [_]
                                            {:status 404
                                             :body "404 Page not found"})}]))

(let [{{:strs [a d]} :params} {:params {"a" "A", "b" "B", "c" "C", "d" "D"}}]
  (println a d))
