(ns app.hello
  (:require [reagent.core :as r]
            [d3 :refer [selectAll]]))

(set! *warn-on-infer* true)

(defn draw-box []
  (-> (selectAll "#canvas")
      (.append "div")
      (.style "background" "red")
      (.style "height" "50px")
      (.style "width" "50px")))

(defn draw-box-button []
  [:<>
   [:div#canvas {:style {:height 200 :width 200}}]
   [:input {:type "button" :value "Draw Box"
            :on-click draw-box}]])

(defn click-counter [click-count]
  [:div
   "The atom " [:code "click-count"] " has value: "
   @click-count ". "
   [:input {:type "button" :value "Click me!"
            :on-click #(swap! click-count inc)}]])

(def click-count (r/atom 0))

(defn hello []
  [:<>
   [:p "Hello, cljs-playground2 is running!"]
   [:p "Here's an example of using a component with state:"]
   [click-counter click-count]
   [:hr]
   [:p "D3 Example"]
   #_[draw-box-button]])
