(ns todo.core
  (:require [goog.dom :as gdom]
            [om.dom :as dom :include-macros true]
            [om.next :as om :refer-macros [defui]])
  (:import [goog.ui IdGenerator]))

(enable-console-print!)

(defonce id-gen (IdGenerator.))

(defn next-id []
  (.getNextUniqueId id-gen))

(defonce app-state (atom
                    {:todos
                      [{:text "Comprar x calota" :id (next-id) :completed true}
                       {:text "Comprar pasta de dente" :id (next-id)}]
                     :filter-fn identity}))

(defn remove-todo [todo]
  (defn not-same [item]
    (not (= item todo)))

  (swap! app-state update-in [:todos]
    #(filter not-same %)))


(defn toggle-todo [todo]
  (defn toggle-if-current [item]
    (if (= item todo)
      (update-in item
                 [:completed]
                 #(not (:completed item)))
      item))
  (swap! app-state update-in [:todos] #(map toggle-if-current %)))


(defui TodoItem
  Object
  (render [this]
    (let [todo (get (om/props this) :todo)]
      (dom/li nil
        (dom/input
          #js {:type "checkbox"
               :checked (:completed todo)
               :onChange #(toggle-todo todo)} nil)
        (dom/span nil (:text todo))
        (dom/a #js {:href "#" :onClick #(remove-todo todo)} "X")))))

(def todo-item (om/factory TodoItem
                {:keyfn :id}))

(defn render-todos [todos]
  (map #(todo-item {:todo % :id (:id %) }) todos))

(defn handle-key-up [evt]
  (let [key-code (.-keyCode evt)
        target (.-target evt)
        value (.-value target)]
    (if (= 13 key-code)
      ((swap! app-state update-in [:todos] #(conj % {:text value :id (next-id)}))
       (set! (.-value target) ""))
      nil)))

(defn items-left []
  (->> (:todos @app-state)
      (filter #(not (:completed %)))
      count))

(defn filtered-todos []
  (println (:filter-fn @app-state))
  (->> (:todos @app-state)
       (filter (:filter-fn @app-state))))

(defn items-active []
  (defn do-filter [todo]
    (not (:completed todo)))
  (swap! app-state assoc-in [:filter-fn] do-filter))

(defn items-completed []
  (defn do-filter1 [todo]
    (:completed todo))
  (swap! app-state assoc-in [:filter-fn] do-filter1))

(defn all-items []
  (swap! app-state assoc-in [:filter-fn] identity))

(defui App
  Object
  (render [this]
    (let [todos (filtered-todos)]
      (println todos)
      (dom/div nil
        (dom/h1 nil "TODO")
        (dom/input #js {:id "input" :type "text" :autoFocus true :placeholder "What needs to be done?" :onKeyUp handle-key-up})
        (dom/ul nil (render-todos todos))
        (dom/div nil
          (dom/span nil (str "Items left:" (items-left)))
          (dom/a #js {:href "#active" :onClick items-active} "Active")
          (dom/a #js {:href "#completed" :onClick items-completed} "Completed")
          (dom/a #js {:href "#all" :onClick all-items} "All"))))))



(def reconciler
  (om/reconciler {:state app-state}))

(om/add-root! reconciler
  App (gdom/getElement "app"))

(defn on-js-reload [])
  ; (let [elem (gdom/getElement "input")]
  ;   (when elem (.focus elem)))

  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
