(ns forum.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defn hello [name]
  [:div "hello " name
   [expandable-view]])

(defn expandable-view []
  (let [expanded (reagent/atom true)]
    (fn []
      [:div.expandable
       [:div.header {:on-click #(swap! expanded not)}
        "Click me to expand and collapse"]
       (if @expanded
         (for [comment ["cat" "dog" "mouse"]]
           [:div.body comment]))])))
;         [:div.body "I am the body"])])))

(reagent/render [hello "Nima"] (.-body js/document))
