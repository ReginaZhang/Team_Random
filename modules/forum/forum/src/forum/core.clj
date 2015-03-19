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
  
(defn query-child-comments [parent-id]
  (jdb/query health-db
             ["SELECT * from Comment where ParentId = ?" parent-id]
             :row-fn #(select-keys % [:commentid :text :userid])))

(defn get_child_comments [request]
  {:status 200
   :body (let [prms (get-in request [:params "parent_id"])]
           (query-child-comments prms))})

(def child_comment_handler (-> get_child_comments
                                   (json/wrap-json-response)
                                   (prms/wrap-params)))

(def forum (bidi/make-handler ["/"
                               {"child_comments" child_comment_handler}]))
