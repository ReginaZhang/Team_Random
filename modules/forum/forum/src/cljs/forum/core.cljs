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
  [comment-id store cache]
  (backend-request "/child_comments"
                   {:parent_id comment-id}
                   (fn [[ok response]] (if ok (do
                                                (reset! store {:children response})
                                                (swap! cache assoc comment-id {:children response}))
                                           (.error js/console (str response))))))

(defn add-comment
  "Make a backend request to add a comment"
  [question-id parent-id user-id parent-box-toggle text error-store]
  (backend-request "/add_comment"
                   {:question_id question-id :parent_id parent-id :user_id user-id :text text}
                   (fn [[ok response]] (if ok 
                                         (reset! parent-box-toggle false)
                                         (reset! error-store (str response))))))

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

(defn comment-entry-box [{:keys [parent-id user-id question-id parent-box-toggle error-store]}]
  "Render a comment entry box. Disables the parent box toggle upon successful;y adding
   a comment, and updates parent error"
  (let [txt (re/atom "")]
    (fn []
      [:div.comment-entry-box
       [:input {:type "text"
                :placeholder "Enter a comment..."
                :value @txt
                :on-change #(reset! txt (-> % .-target .-value))}]
       [:button {:on-click #(add-comment question-id parent-id user-id parent-box-toggle @txt error-store)}
        "Submit"]])))

(defn display-comment [req-c {:keys [userid text commentid questionid]}]
  (let [expanded (re/atom false)
        child-comment-atom (re/atom {})
        showing-comment-entry (re/atom false)
        error-atom (re/atom "")
        children-req {:comment-id commentid :atom child-comment-atom}]
    (fn []
      [:div.comment-region
       [:div.comment-text (str "Comment by user id: " userid " with comment id: " commentid)]
       [:div.comment-text text]
       [:div.comment-child-toggle {:on-click #(swap! expanded not)}
        (if @expanded "-" "+")]
       (if @expanded
         [:div.comment-entry-box-toggle {:on-click #(swap! showing-comment-entry not)}
          (if @showing-comment-entry "Abort comment" "Enter Comment")])
       (when @showing-comment-entry [comment-entry-box {:parent-id commentid :user-id userid :question-id
                                                        questionid :parent-box-toggle showing-comment-entry
                                                        :error-store error-atom}])
       (when (not= @error-atom "") [:div.error-text @error-atom])
       (when @expanded
         (go (>! req-c children-req))
         (for [child-comment (:children @child-comment-atom)]                                       
           ^{:key (:commentid child-comment)}
           [display-comment req-c child-comment]))])))


(defn hello [name]
  [:div "hello " name
   [display-comment request-chan]])

(re/render [display-comment request-chan {:userid 0 :text "test comment" :commentid 0 :questionid 0}] (.-body js/document))

