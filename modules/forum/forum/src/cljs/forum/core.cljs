(ns forum.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as re :refer [atom]]
            [ajax.core :as ajax :refer [ajax-request]]
            [cljs.core.async :refer [chan <! >! take!]]))

(enable-console-print!)

(comment
(let [req-c request-chan
      c (chan)]
  (go
    (>! req-c {:comment-id 0})
    (let [res (<! req-c)]
      (print @res))))

(defn start-resource-provider
  "Tbe resource provider fetches and caches comments
  Takes a channel through which {:chan, :comment-id}
  can be sent, and an atom will be sent back through that chan
  containing that comment's child comments."
  [request-chan]
  (let [c request-chan]
    (go-loop [atoms {0 (re/atom {:children "foo"}) }]
      (let [req (<! c)
            post-id (:comment-id req)
            reply-ch (:chan req)
            data (get atoms post-id)]
        (if data
          (do
            (>! c data)
            (request-child-comments post-id data)
            (recur atoms))
          (let [empty-data (re/atom {})
                updated-atoms (assoc atoms post-id empty-data)]
            (>! c empty-data)
            (request-child-comments post-id empty-data)
            (recur updated-atoms)))))                    
    c))

(defn display-comment [req-c]
  (let [expanded (re/atom false)
        child-comment-chan (go
                             (>! req-c {:comment-id 0})
                             (<! req-c))]
    (fn []
      [:div.comment-region
       [:div.comment-text 
        "I'm a comment, yo!"]
       [:div.comment-child-toggle {:on-click #(swap! expanded not)}
        (if @expanded "-" "+")]
       (if @expanded
         (take! child-comment-chan (fn [child-comment-atom]
                                     (print child-comment-atom)
                                     (for [comment (:children @child-comment-atom)]                                       
                                       [:div.body comment]))))])))

)

(defn request-child-comments
  "Make an asynchronous request to get child comments and store them
  in the given atom, updating the cache to contain the results"
  [comment-id store cache]
  (ajax-request
        {:uri "/child_comments"
         :method :get
         :params {:parent_id comment-id}
         :handler (fn [[ok response]] (if ok
                                        (do
                                          (reset! store {:children response})
                                          (swap! cache assoc comment-id {:children response}))
                                        (.error js/console (str response))))
         :format (ajax/url-request-format)
         :response-format (ajax/json-response-format {:keywords? true})}))

(defn start-resource-provider
  "Tbe resource provider fetches and caches comments
  Takes a channel through which {:chan, :comment-id}
  can be sent, and an atom will be sent back through that chan
  containing that comment's child comments."
  [request-chan]
  (let [c request-chan        
        cache (re/atom {})] ;a map of post ids to children
    (go
      (while true
        (let [req (<! c)
              post-id (:comment-id req)
              children-store (:atom req)
              cached-children (get @cache post-id)]
          (if cached-children
            (reset! children-store cached-children)
            (request-child-comments post-id children-store cache)))))))

(def request-chan (chan))
(start-resource-provider request-chan)

(defn display-comment [req-c {:keys [userid text commentid]}]
  (let [expanded (re/atom false)
        child-comment-atom (re/atom {})
        children-req {:comment-id commentid :atom child-comment-atom}]
    (fn []
      [:div.comment-region
       [:div.comment-text (str "Comment by user id: " userid " with text: " text " and id "  commentid)]
       [:div.comment-child-toggle {:on-click #(swap! expanded not)}
        (if @expanded "-" "+")]
       (when @expanded
         (print children-req)
         (go (>! req-c children-req))
         (print child-comment-atom)
         (for [child-comment (:children @child-comment-atom)]                                       
           ^{:key (:commentid child-comment)}
           [display-comment req-c child-comment]))])))


(defn hello [name]
  [:div "hello " name
   [display-comment request-chan]])

(re/render [display-comment request-chan {:userid 0 :text "test comment" :commentid 0}] (.-body js/document))
