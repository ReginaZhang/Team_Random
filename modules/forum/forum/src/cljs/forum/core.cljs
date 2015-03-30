(ns forum.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defn hello [name]
  [:div "hello " name
   [expandable-view]])

(defn expandable-view []
  (let [expanded (reagent/atom true)]
    (fn []
      [:div.comment-region
       [:div.comment-text 
        "I'm a comment, yo!"]
       [:div.comment-child-toggle {:on-click #(swap! expanded not)}
        (if @expanded "-" "+")]
       (if @expanded
         (for [comment ["cat" "dog" "mouse"]]
           [:div.body comment]))])))

(reagent/render [hello "Nima"] (.-body js/document))
