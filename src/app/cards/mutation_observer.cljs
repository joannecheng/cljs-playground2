(ns app.cards.mutation-observer
  (:require [reagent.core :as r]
            [devcards.core :as dc :refer [defcard]]))

;; Layer 1: canvas
;;  - svg rect
;;  - increase size by increasing atom
;; Layer 2: edit
;;  - selection rectangle

(def !x-pos (r/atom 100))
(def !rect2-attrs (r/atom {}))

(defn observable-rectangle
  "Form-3 component that renders a rectangle"
  [_]
  (let [!rect-el (atom nil)
        observer (js/MutationObserver. (fn [mutations]
                                         (let [rect2-elem (.-target (first mutations))]
                                           (reset! !rect2-attrs {:y (js/parseFloat (.getAttribute rect2-elem "y"))
                                                                 :x (js/parseFloat (.getAttribute rect2-elem "x"))
                                                                 :height (js/parseFloat (.getAttribute rect2-elem "height"))
                                                                 :width (js/parseFloat (.getAttribute rect2-elem "height"))})
                                           (.log js/console mutations))))]
    (r/create-class
     {:component-did-mount
      (fn []
        (.observe observer
                  ;; observer takes a target element
                  ;; Using a ref
                  @!rect-el
                  ;; Observer config
                  #js {:attributes true}))

      :component-will-unmount #(.disconnect observer)
      :component-did-update
      (fn [_]
        (.log js/console "updated")
        (.observe observer
                  ;; observer takes a target element
                  ;; Using a ref
                  @!rect-el

                ;; Observer config
                  #js {:attributes true}))

      :reagent-render
      (fn [{:keys [x-pos]}]
        [:rect {:ref (fn [el] (reset! !rect-el el))
                :id "rect-2" :fill "#22aa33" :width 30 :height 30 :x (* 0.5 x-pos) :y 175}])})))

(defn svg-canvas [{:keys [x-pos]}]
  [:svg {:width 500 :height 500}
   #_[:rect {:fill "#3322aa" :width width :height 100 :x 0 :y 0}]
   [observable-rectangle {:x-pos x-pos}]])

(defn canvas-layer [props]
  [:div.canvas [svg-canvas (select-keys props [:x-pos])]])

(defn edit-layer [{:keys [x y height width]}]
  (let [padding 5]
    [:div.edit-layer
     [:div.selection {:style {:width (+ padding width)
                              :height (+ padding height)
                              :transform (str "translate(" (- x padding) "px," (- y padding) "px)")}}]]))

(defn el []
  [:<>
   [:button
    {:on-click #(swap! !x-pos (fn [x] (+ 10 x)))}
    (str "Move rect: " @!x-pos)]
   [:div.content
    [canvas-layer {:x-pos @!x-pos}]
    [edit-layer @!rect2-attrs]]])

(defcard test-card
  (dc/reagent [el]))