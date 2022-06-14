(ns app.cards.undo-editor
  (:require [devcards.core :as dc :refer [defcard]]
            [clojure.spec.alpha :as s]
            [day8.re-frame.undo :as undo :refer [undoable]]
            [reagent.core :as r]
            [re-frame.core :as rf :refer [dispatch subscribe]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; db
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(s/def ::id int?)
(s/def ::title string?)
(s/def ::done? boolean?)
(s/def ::todo (s/keys :req-un [::id ::title ::done?]))
(s/def ::todos (s/coll-of ::todo))

(s/def ::db (s/keys :req-un [::todos]))

(def default-db
  {:todos []})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; interceptors
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def check-db-interceptor
  (re-frame.core/->interceptor
   :id      :check-db
   :after  (fn [context]
             (let [db (get-in context [:effects :db])]
               (if (s/valid? ::db db)
                 context
                 (throw (ex-info (str "Failed clojure.spec:"
                                      (s/explain ::db db)) {})))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn generate-id [{:keys [todos]}]
  (if (empty? todos)
    1
    (->> todos
         (mapv :id)
         (apply max)
         inc)))

(rf/reg-event-fx
 :initialize
 [check-db-interceptor]
 (fn [_ _]
   {:db default-db}))

(rf/reg-event-db
 :add-todo
 [check-db-interceptor
  #_(undoable "Add todo")]
 (fn [db [_ todo]]
   (let [next-id (generate-id db)]
     (-> db
         (update-in [:todos] conj {:id next-id
                                   :title todo
                                   :done? false})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(rf/reg-sub
 :todos
 (fn [db _]
   (:todos db)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fn for views
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn on-input-change [!content e]
  (reset! !content (.-target.value ^js/Event e)))

(defn add-todo [todo]
  (dispatch [:add-todo todo]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Views
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn undo-button []
  (let [undos? (some? @(subscribe [:undos?]))]
    [:button (cond-> {:on-click #(dispatch [:undo])}
               (not undos?) (assoc :disabled "disabled"))
     "Undo"]))

(defn input-field []
  (r/with-let [!content (r/atom "")
               on-click #(do
                           (add-todo @!content)
                           (reset! !content nil))]
    [:form
     [:input {:type "text"
              :value @!content
              :placeholder "What do you need to do?"
              :on-change (partial on-input-change !content)}]
     [:button
      {:type :submit
       :on-click on-click}
      "Add"]]))

(defn todo-item [{:keys [title]}]
  [:div
   [:li title]
   [:button "mark done"]
   [:button "edit"]
   [:button "delete"]])

(defn empty-list []
  [:div "No todos yet!"])

(defn todos-list [{:keys [todos]}]
  [:div
   [:ul
    (if (empty? todos)
      [empty-list]
      (for [todo todos]
        [todo-item (assoc todo :key (:id todo))]))]])

(defn app []
  (let [todos @(subscribe [:todos])]
    [:div
     [:h1 "Todo list"]
     [undo-button]
     [input-field]
     [todos-list {:todos todos}]]))

(defn init-app []
  (dispatch [:initialize])
  [app])

(defcard undo-app
  (dc/reagent [init-app]))
