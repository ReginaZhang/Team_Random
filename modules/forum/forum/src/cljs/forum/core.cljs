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
  [comment-id cache extra-success-callback]
  (backend-request "/child_comments"
                   {:parent_id comment-id}
                   (fn [[ok response]] (if ok (let [store (:store (get @cache comment-id))
                                                    new-val {:children response :store store }]
                                                (swap! cache assoc comment-id new-val)
                                                (reset! store {:children response})
                                                (when extra-success-callback (extra-success-callback)))
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

(defn delete-comment
  "Make a backend request to delete a comment"
  [comment-id error-store success-callback]
  (backend-request "/delete_comment" {:comment_id comment-id}
                   (fn [[ok response]] (if ok (success-callback) (reset! error-store (str response))))))

(defn edit-comment
  "Make a backend request to edit a comment"
  [comment-id text error-store success-callback]
  (backend-request "/edit_comment" {:comment_id comment-id :text text}
                   (fn [[ok response]] (if ok (success-callback) (reset! error-store (str response))))))


(defn flag-comment
  "Tell the backend to flag a comment"
  [flagger-id comment-id flag-ids-store error-store update-callback]
  (let [flag-ids (keys (filter #(val %) @flag-ids-store))]
    (backend-request "/flag_comment" {:comment_id comment-id :user_id flagger-id :flag_ids flag-ids}
                     (fn [[ok response]] (if ok (update-callback flag-ids)
                                             (reset! error-store "Error: db rejected flag; maybe you used a non-existing userid? Try userid 1."))))))

(defn get-flag-types
  "Get a mapping of flag ids to flag names"
  [flagtype-store]
  (backend-request "/flag_types" {}
                   (fn [[ok response]] (if ok (reset! flagtype-store
                                                      (reduce #(assoc %1 (:flagid %2) (:flagname %2)) {} response))
                                           (do (.error js/console (str response))
                                               (get-flag-types flagtype-store))))))

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
                  (request-child-comments post-id cache nil))))
            
            :update-children
            (let
                [post-id (:comment-id req)
                 children-store (:atom req)
                 cached-val (get @cache post-id)
                 success-callback (:success-callback req)]
              (when cached-val
                  (request-child-comments post-id cache success-callback)))))))))

(defn userid-select
  "Select user id. For testing, until proper auth is set up"
  [userid-atom]
  (fn []
    [:div.somebox
     [:div.text "Select your userid. You can edit and delete posts with the same posterid as your userid."]
     [:div.text "NOTE: right now only user 1 can flag comments, as user 1 is the only user in the database, and trying to create a flag with a non-existing user violates a foreign key constraint."]
     [:input {:type "number"
              :placeholder "Enter a userid"
              :value @userid-atom
              :on-change #(let [newval (-> % .-target .-value)
                                numval (js/parseInt newval)]
                            (if numval
                              (reset! userid-atom numval)
                              (reset! userid-atom 1)))}]]))

(defn flag-select
  "Select one or more flags via a html checkbox"
  [{:keys [flagtype-store select-flag-store text callback-fn]}]
  (fn []
    [:div.flag-select-box
     text
     (doall (for [keyval @flagtype-store]
              ^{:key (key keyval)}
              [:div.checkbox
               [:input {:type "checkbox"
                        :checked (get @select-flag-store (key keyval))
                        :on-change #(swap! select-flag-store update (key keyval) not)}]
               (val keyval)]))
     (if callback-fn
       [:button {:on-click callback-fn}
        "Update"] nil)]))

(defn comment-entry-box
  "Render a comment entry box. Disables the parent box toggle upon successful;y adding
  a comment, and updates parent error"
  [{:keys [parent-id user-id-atom question-id parent-box-toggle error-store update-callback]}]
  (let [txt (re/atom "")]
    (fn []
      [:div.comment-entry-box
       [:input {:type "text"
                :placeholder "Enter a comment..."
                :value @txt
                :on-change #(reset! txt (-> % .-target .-value))}]
       [:button {:on-click #(add-comment question-id parent-id @user-id-atom parent-box-toggle @txt error-store update-callback)}
        "Submit"]])))

(defn comment-edit-box
  "Render a comment entry box. Disables the parent edit box toggle upon successful;y editing
  a comment, and updates parent error"
  [{:keys [comment-id text-store error-store update-callback]}]
  (fn []
    [:div.comment-entry-box
     [:input {:type "text"
              :placeholder "Edit your comment..."
              :value @text-store
              :on-change #(reset! text-store (-> % .-target .-value))}]
       [:button {:on-click #(edit-comment comment-id @text-store error-store update-callback)}
        "Save"]]))

(defn display-comment
  "Display a comment, and its children if show children is clicked. Also may show options
  for replying, flagging, voting, editing and deleting a comment."
  [{:keys [req-c userid text commentid questionid parentid flagids flagtypes deleted filter-store cur-user-atom]}]
  (let [expanded (re/atom false)
        child-comment-atom (re/atom {})
        showing-comment-entry (re/atom false)
        showing-update-flags (re/atom false)
        error-atom (re/atom "")
        edited-text-atom (re/atom @text)
        editing-comment (re/atom false)
        children-req {:type :children-request :comment-id commentid :atom child-comment-atom}
        children-update-callback #(go (>! req-c {:type :update-children :comment-id commentid}))
        comment-flag-store (re/atom {})
        update-parents-children #(go (>! req-c {:type :update-children :comment-id parentid :success-callback
                                                (fn [] (reset! showing-update-flags false))}))
        post-flag-update-callback (fn [new-flagids]                                        
                                    (reset! flagids new-flagids) ;Update flags on the comment
                                    (update-parents-children))    ;Request flags be updated in backend, and reset flag display
        flag-update-fn #(flag-comment @cur-user-atom commentid comment-flag-store error-atom post-flag-update-callback)
        comment-delete-fn #(delete-comment commentid error-atom (fn [] (reset! text "!!DELETED!!")
                                                                  (reset! deleted true)
                                                                  (update-parents-children)))
        post-comment-edit-callback (fn[] (reset! text @edited-text-atom)
                                     (reset! editing-comment false)
                                     (update-parents-children))]
    (fn []
      (if (some #(get @filter-store %) @flagids) nil
          [:div.comment-region
           [:div.comment-text (str "Comment by user id: " userid " with comment id: " commentid)]
           [:div.comment-text "Flagged as: " (doall (map #(str (get @flagtypes %) " ") @flagids))]
           [:div.comment-text  (if @deleted "!!DELETED!!" (str "Comment text is: " "\"" @text "\""))]
           (when (and (= @cur-user-atom userid) (not @deleted))
             [:div.edit-select-box {:on-click #(swap! editing-comment not)}
              (if @editing-comment "Abort editing" "E (Click here to edit this comment)")])
           (when (and (= @cur-user-atom userid) @editing-comment)
             [comment-edit-box {:comment-id commentid :text-store edited-text-atom :error-store error-atom
                                :update-callback post-comment-edit-callback}])
           (when (and (= @cur-user-atom userid) (not @deleted))
             [:div.delete-text {:on-click comment-delete-fn} "D (Click here to delete this comment)"])
           (when (not @deleted)
             [:div.flag-select-box {:on-click #(swap! showing-update-flags not)}
              (if @showing-update-flags "Abort flagging" "F (Click here to flag this comment)")])
           (when @showing-update-flags [flag-select {:flagtype-store flagtypes :select-flag-store comment-flag-store
                     :text "What flags apply to this comment?" :callback-fn flag-update-fn}])
           [:div.comment-child-toggle {:on-click #(swap! expanded not)}
            (if @expanded "- (click here to hide children)" "+ (click here to show children)")]
           (if @expanded
             [:div.comment-entry-box-toggle {:on-click #(swap! showing-comment-entry not)}
              (if @showing-comment-entry "Abort comment" "Click here to enter a reply to this comment!")])
           (when @showing-comment-entry [comment-entry-box {:parent-id commentid :user-id-atom cur-user-atom :question-id questionid
                                                            :parent-box-toggle showing-comment-entry :error-store error-atom
                                                            :update-callback children-update-callback}])
           (when (not= @error-atom "") [:div.error-text @error-atom])
           (when @expanded
             (go (>! req-c children-req))
             (doall (for [child-comment (:children @child-comment-atom)]
                      (let [flags-store (re/atom (:flagids child-comment))
                            text-store (re/atom (:text child-comment))
                            deleted-store (re/atom (:deleted child-comment))]
                        ^{:key (:commentid child-comment)}
                        [display-comment
                         (assoc child-comment :req-c req-c :questionid questionid :filter-store filter-store :flagtypes flagtypes
                                :cur-user-atom cur-user-atom :flagids flags-store :text text-store :deleted deleted-store)]))))]))))

(defn forum-page
  "Forum page containing all the components, used for testing and demonstration"
  []
  (let [userid-store (re/atom 1)
        flagtype-store (re/atom {})
        filtered-flags (re/atom {})
        request-chan (chan)]
    (start-resource-provider request-chan)
    (get-flag-types flagtype-store)
    (fn []
      [:div.whole-page
       [userid-select userid-store]
       [flag-select {:flagtype-store flagtype-store :select-flag-store filtered-flags
                     :text "Filter out what kind of comments?" :callback-fn nil}]
       [display-comment {:req-c request-chan :userid 0
                         :text (re/atom "I am a dummy root comment with no life in the DB, a mere placeholder for a health question. 
I am immortal, and cannot be flagged or permanently edited/deleted.")
                         :commentid 0 :questionid 0 :parentid 0 :flagids (re/atom [3]) :filter-store filtered-flags
                         :flagtypes flagtype-store :cur-user-atom userid-store :deleted (re/atom false)}]])))
      
(re/render [forum-page] (.-body js/document))

