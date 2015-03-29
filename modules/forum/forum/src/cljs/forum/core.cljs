(ns forum.core
  (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(defn hello [name]
  [:div "hello " name])

(reagent/render [hello "Nima"] (.-body js/document))
