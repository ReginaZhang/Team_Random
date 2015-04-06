(ns forum.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as re :refer [atom]]
            [ajax.core :as ajax :refer [ajax-request]]
            [cljs.core.async :refer [chan <! >! take!]]))

(enable-console-print!)

(defn backend-request
  "Make an asynchronous request to the backend at the given uri and
  the provided params, calling the supplied function upon completion"
  [uri params func]
  (ajax-request
        {:uri uri
         :method :get
         :params params
         :handler func
         :format (ajax/url-request-format)
         :response-format (ajax/json-response-format {:keywords? true})}))
  

(defn request-child-comments
  "Make an asynchronous request to get child comments and store them
  in the given atom, updating the cache to contain the results"
  [comment-id cache]
  (backend-request "/child_comments"
                   {:parent_id comment-id}
                   (fn [[ok response]] (if ok (let [store (:store (get @cache comment-id))
                                                    new-val {:children response :store store }]
                                                (swap! cache assoc comment-id new-val)
                                                (reset! store {:children response}))
                                           (.error js/console (str response))))))

(defn add-comment
  "Make a backend request to add a comment"
  [question-id parent-id user-id parent-box-toggle text error-store update-callback]
  (backend-request "/add_comment"
                   {:question_id question-id :parent_id parent-id :user_id user-id :text text}
                   (fn [[ok response]] (if ok 
                                         (do (reset! parent-box-toggle false)
                                             (update-callback))
                                         (reset! error-store (str response))))))

(defn start-resource-provider
  "Tbe resource provider fetches and caches comments
  Takes a channel through which {:chan, :comment-id}
  can be sent, and an atom will be sent back through that chan
  containing that comment's child comments."
  [request-chan]
  (let [c request-chan        
        cache (re/atom {})] ;a map of post ids to {:children :store} maps
    (go
      (while true
        (let [req (<! c)
              req-type (:type req)]
          (case req-type
            :children-request
            (let
                [post-id (:comment-id req)
                 children-store (:atom req)
                 cached-val (get @cache post-id)]
              (if cached-val
                ;;If cached val exists, use it
                (reset! children-store cached-val)
                
                ;;Otherwise add store to cache and request updating it
                (do
                  (swap! cache assoc post-id {:children nil :store children-store})
                  (request-child-comments post-id cache))))
            
            :update-children
            (let
                [post-id (:comment-id req)
                 children-store (:atom req)
                 cached-val (get @cache post-id)]
              (when cached-val
                  (request-child-comments post-id cache)))))))))

(def request-chan (chan))
(start-resource-provider request-chan)

(defn comment-entry-box [{:keys [parent-id user-id question-id parent-box-toggle error-store update-callback]}]
  "Render a comment entry box. Disables the parent box toggle upon successful;y adding
   a comment, and updates parent error"
  (let [txt (re/atom "")]
    (fn []
      [:div.comment-entry-box
       [:input {:type "text"
                :placeholder "Enter a comment..."
                :value @txt
                :on-change #(reset! txt (-> % .-target .-value))}]
       [:button {:on-click #(add-comment question-id parent-id user-id parent-box-toggle @txt error-store update-callback)}
        "Submit"]])))

(defn display-comment [req-c {:keys [userid text commentid questionid]}]
  (let [expanded (re/atom false)
        child-comment-atom (re/atom {})
        showing-comment-entry (re/atom false)
        error-atom (re/atom "")
        children-req {:type :children-request :comment-id commentid :atom child-comment-atom}
        children-update-callback (fn [] (go (>! req-c {:type :update-children :comment-id commentid})))]
    (fn []
      [:div.comment-region
       [:div.comment-text (str "Comment by user id: " userid " with comment id: " commentid)]
       [:div.comment-text text]
       [:div.comment-child-toggle {:on-click #(swap! expanded not)}
        (if @expanded "-" "+")]
       (if @expanded
         [:div.comment-entry-box-toggle {:on-click #(swap! showing-comment-entry not)}
          (if @showing-comment-entry "Abort comment" "Enter Comment")])
       (when @showing-comment-entry [comment-entry-box {:parent-id commentid :user-id userid :question-id questionid
                                                        :parent-box-toggle showing-comment-entry :error-store error-atom
                                                        :update-callback children-update-callback}])
       (when (not= @error-atom "") [:div.error-text @error-atom])
       (when @expanded
         (go (>! req-c children-req))
         (for [child-comment (:children @child-comment-atom)]                                       
           ^{:key (:commentid child-comment)}
           [display-comment req-c (assoc child-comment :questionid questionid)]))])))

(re/render [display-comment request-chan {:userid 0 :text "test comment" :commentid 0 :questionid 0}] (.-body js/document))

