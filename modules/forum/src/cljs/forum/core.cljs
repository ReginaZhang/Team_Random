(ns forum.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as re :refer [atom]]
            [ajax.core :as ajax :refer [ajax-request]]
            [cemerick.url :refer [url]]
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
  [comment-id user-id cache extra-success-callback]
  (backend-request "/child_comments"
                   {:parent_id comment-id
                    :user_id user-id}
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

(defn add-question
  "Make a backend request to post a new question"
  [text user-id title update-callback]
  (backend-request "/add_question"
                   {:text text :user_id user-id :title title}
                   (fn [[ok response]] (if ok                                          
                                         (update-callback)
                                         (print (str "Error: " response))))))

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

(defn votefor-commment
  "Submit an up or down vote for a comment"
  [commentid userid vote error-store success-callback]
  (backend-request "/vote_for" {:comment_id commentid :user_id @userid :vote_type vote}
                   (fn [[ok response]] (if ok (success-callback (:newscore response))
                                           (reset! error-store "Error: db rejected vote; maybe you used a non-existing userid? Try userid 1.")))))

(defn get-flag-types
  "Get a mapping of flag ids to flag names"
  [flagtype-store]
  (backend-request "/flag_types" {}
                   (fn [[ok response]] (if ok (reset! flagtype-store
                                                      (reduce #(assoc %1 (:flagid %2) (:flagname %2)) {} response))
                                           (do (.error js/console (str response))
                                               (get-flag-types flagtype-store))))))

(defn get-questions
  "Get a list of questions"
  [questions-store userid error-store questionid]
  (backend-request "/questions" (if questionid {:user_id @userid :question_id questionid} {:user_id @userid})
                   (fn [[ok response]] (if ok (reset! questions-store response)
                                           (reset! error-store "Error: could not get questions from DB. Maybe the DB is down?")))))


(defn check-loggedin
  "Return the user id if the user is loggedin.
  Check is based on user IP and Request Header"
  [userid-store]
  (backend-request "/check_loggedin" {} (fn [[ok response]] (if ok (do (reset! userid-store (:id response))
                                                                       (print (str "response is"  (:id response))))
                                                                (check-loggedin userid-store)))))


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
                 user-id (:user-id req)
                 children-store (:atom req)
                 cached-val (get @cache post-id)]
              (if cached-val
                ;;If cached val exists, use it
                (reset! children-store cached-val)
                
                ;;Otherwise add store to cache and request updating it
                (do
                  (swap! cache assoc post-id {:children nil :store children-store})
                  (request-child-comments post-id @user-id cache nil))))
            
            :update-children
            (let
                [post-id (:comment-id req)
                 user-id (:user-id req)
                 children-store (:atom req)
                 cached-val (get @cache post-id)
                 success-callback (:success-callback req)]
              (when cached-val
                (request-child-comments post-id @user-id cache success-callback)))))))))

(defn userid-select
  "Select user id. For testing, until proper auth is set up"
  [userid-atom]
  (fn []
    [:div.somebox
     [:div.text "Select your userid. You can edit and delete posts with the same posterid as your userid."]
     [:div.text "This is just for testing."]
     [:div.text "NOTE: right now only users 1 to 3 can post, flag and vote for comments, as these are the only user in the database, and trying to post with a non-existing user violates a foreign key constraint."]
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

(defn new-question-box
  "Render a question posting box, for posting a new question."
  [{:keys [user-id-atom update-callback]}]
  (let [txt (re/atom "")
        title (re/atom "")]
    (fn []
      [:div.new-question-box
       [:div.question-title-entry-box
        [:input {:type "text"
                 :placeholder "Enter the question title..."
                 :value @title
                 :on-change #(reset! title (-> % .-target .-value))}]]
       [:div.question-text-entry-box
        [:input {:type "text"
                 :placeholder "Enter the question..."
                 :value @txt
                 :on-change #(reset! txt (-> % .-target .-value))}]]
       [:button {:on-click #(add-question @txt @user-id-atom @title update-callback)}
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
  [{:keys [req-c userid text commentid questionid parentid flagids flagtypes score votetype deleted filter-store cur-user-atom]}]
  (let [expanded (re/atom false)
        child-comment-atom (re/atom {})
        showing-comment-entry (re/atom false)
        showing-update-flags (re/atom false)
        error-atom (re/atom "")
        edited-text-atom (re/atom @text)
        editing-comment (re/atom false)
        children-req {:type :children-request :comment-id commentid :user-id cur-user-atom :atom child-comment-atom}
        children-update-callback #(go (>! req-c {:type :update-children :comment-id commentid :user-id cur-user-atom}))
        comment-flag-store (re/atom {})
        update-parents-children #(go (>! req-c {:type :update-children :comment-id parentid :user-id cur-user-atom :success-callback
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
                                     (update-parents-children))
        vote-callback (fn [vote] (votefor-commment commentid cur-user-atom vote error-atom
                                                   (fn [newscore]  (reset! votetype vote)
                                                     (reset! score newscore)                        
                                                     (update-parents-children))))]
    (fn []
      (if (and (not (some #(get @filter-store %) @flagids)) (> (count @flagids) 0)) nil
          [:div.comment-region

           [:div.comment-rest
            [:div.comment-buttons
             (when (and (not @deleted) (not (= @votetype "up")) @cur-user-atom)
                   [:button.vote.up {:on-click #(vote-callback "up")}
                    "Upvote"])
             (when (and (not @deleted) (not (= @votetype "down")) @cur-user-atom)
                   [:button.vote.down {:on-click #(vote-callback "down")}
                    "Downvote"])
             (when (and (= @cur-user-atom userid) (not @deleted))
                   [:button.edit-select-box {:on-click #(swap! editing-comment not)}
                    (if @editing-comment "Abort editing" "Edit")])
             (when (and (= @cur-user-atom userid) @editing-comment)
                   [comment-edit-box {:comment-id      commentid :text-store edited-text-atom :error-store error-atom
                                      :update-callback post-comment-edit-callback}])

             (when (and (= @cur-user-atom userid) (not @deleted))
                   [:button.delete-text {:on-click comment-delete-fn} "Delete"])

             (when (and (not @deleted) @cur-user-atom)
                   [:button.flag_button {:on-click #(swap! showing-update-flags not)}
                    (if @showing-update-flags "Abort flagging" "Add Comment Flag")])
             (when @showing-update-flags [flag-select {:flagtype-store flagtypes :select-flag-store comment-flag-store
                                                       :text           "What flags apply to this comment?" :callback-fn flag-update-fn}])
             [:button.comment-child-toggle {:on-click #(swap! expanded not)}
              (if @expanded "hide replies" "show replies")]
             (if (and @expanded @cur-user-atom)
               [:button.comment-entry-box-toggle {:on-click #(swap! showing-comment-entry not)}
                (if @showing-comment-entry "Abort comment" "Enter Reply")])]
            [:div.comment-score (str "Comment Score : " @score)]
            ; [:div.comment-text-region
            ; [:div.comment-text (str "Comment by user id: " userid " with comment id: " commentid)]
            ; [:div.comment-text (str "score is : " @score ", and current user voted it: " @votetype)]
            ; [:div.comment-text "Flagged as: " (doall (map #(str (get @flagtypes %) " ") @flagids))]
            ; [:div.comment-text
            [:div.comment-text-string (if @deleted "!!DELETED!!" (str "Comment Text :  " @text))]

            (when @showing-comment-entry [comment-entry-box {:parent-id         commentid :user-id-atom cur-user-atom :question-id questionid
                                                             :parent-box-toggle showing-comment-entry :error-store error-atom
                                                             :update-callback   children-update-callback}])
            (when (not= @error-atom "") [:div.error-text @error-atom])
            (when @expanded
                  (go (>! req-c children-req))
                  (doall (for [child-comment (:children @child-comment-atom)]
                              (let [[flags-store text-store deleted-store score-store vote-store]
                                    (map #(re/atom (% child-comment)) [:flagids :text :deleted :score :votetype])]
                                   ^{:key (:commentid child-comment)}
                                   [display-comment
                                    (assoc child-comment :req-c req-c :questionid questionid :filter-store filter-store :flagtypes flagtypes
                                           :cur-user-atom cur-user-atom :flagids flags-store :text text-store :deleted deleted-store
                                           :score score-store :votetype vote-store)]))))]]))))


(defn forum-page
  "Forum page containing all the components, used for testing and demonstration"
  []
  (let [userid-store (re/atom nil)
        flagtype-store (re/atom {})
        filtered-flags (re/atom {1 true 2 true 3 true 4 true})
        request-chan (chan)
        question-store (re/atom {})
        question-id (get (:query (url js/window.location.href)) "question_id")]
    (start-resource-provider request-chan)
    (check-loggedin userid-store)
    (get-flag-types flagtype-store)
    (get-questions question-store userid-store (re/atom {}) question-id)
    (fn []
      [:div.whole-page
;       [userid-select userid-store]
       [flag-select {:flagtype-store flagtype-store :select-flag-store filtered-flags
                     :text "Show what kind of comments?" :callback-fn nil}]              
       (for [{:keys [questionid questiondeleted userid commentid commenttext questiontitle votetype score flagids]} @question-store]
         ^{:key questionid}
         [:div.question
          [:div.question-title (str "Question title is: " questiontitle)]
          [display-comment {:req-c request-chan :userid userid
                            :text (re/atom commenttext)
                            :commentid commentid :questionid questionid :parentid nil :flagids (re/atom flagids) :filter-store filtered-flags
                            :flagtypes flagtype-store :cur-user-atom userid-store :deleted (re/atom questiondeleted) :score (re/atom score)
                            :votetype (re/atom votetype)}]])])))

(defn question-list
  "Display a list of all questions, ordered by score"
  []
  (let [userid-store (re/atom nil)
        question-store (re/atom {})
        update-questions #(get-questions question-store userid-store (re/atom {}) nil)]
    (check-loggedin userid-store)
    (update-questions)
    (fn []
      [:div.forum-index
       (if @userid-store [:div.question-add
                          [new-question-box {:user-id-atom userid-store :update-callback update-questions}]]
           [:div.login-prompt "Login to ask a question"]) 
       [:div.question-list
        (for [{:keys [questionid questiontitle score]} (reverse (sort-by :score @question-store))]
          ^{:key questionid}
          [:div.question
           [:div.one_question_div
            [:a.one_question_link {:href (str "/static/forum.html?question_id=" questionid)}
             questiontitle]
            [:span.question-score (str "Question Score : " score)]]
           ])]])))

(set! (.-onload js/window)
      #(let [q-list-div (js/document.getElementById "question_list")
             forum-div (js/document.getElementById "forum")]
         (re/render [(if q-list-div question-list forum-page) ]
                    (if q-list-div q-list-div forum-div))))

                                        ;(print ( js/document.getElementById "test"))
                                        ;(re/render [forum-page] (js/document.getElementById "test"))

