(ns todo.core
  (:require [goog.dom :as gdom]
            [om.dom :as dom :include-macros true]
            [om.next :as om :refer-macros [defui]]
            [todo.store :as store]))

(enable-console-print!)

(defui TodoItem
  Object
  (render [this]
    (let [todo (get (om/props this) :todo)]
      (println todo)
      (dom/li nil
        (dom/input
          #js {:type "checkbox"
               :checked (:completed todo)
               :onChange #(store/toggle-todo todo)} nil)
        (dom/span nil (:text todo))
        (dom/a #js {:href "#" :onClick #(store/remove-todo todo)} "X")))))

(def todo-item (om/factory TodoItem
                {:keyfn :id}))

(defn render-todos [todos]
  (map #(todo-item {:todo % :id (:id %)}) todos))

(defn handle-key-up [evt]
  (let [key-code (.-keyCode evt)
        target (.-target evt)
        value (.-value target)]
    (if (= 13 key-code)
      (do (store/add-todo value)
          (set! (.-value target) ""))
      nil)))


(defn active-items []
  (store/set-filter #(not (:completed %))))

(defn completed-items []
  (store/set-filter #(:completed %)))

(defn all-items []
  (store/set-filter identity))

(defui App
  Object
  (render [this]
    (let [todos (store/filtered-todos)]
      (dom/div #js {:className "todo-container"}
        (dom/h1 nil "TODO")
        (dom/input #js {:id "input"
                        :type "text"
                        :autoFocus true
                        :placeholder "What needs to be done?"
                        :onKeyUp handle-key-up})
        (dom/ul nil (render-todos todos))
        (dom/div #js {:className "controls"}
          (dom/span nil (str "Items left:" (store/items-left)))
          (dom/a #js {:href "#active" :onClick active-items} "Active")
          (dom/a #js {:href "#completed" :onClick completed-items} "Completed")
          (dom/a #js {:href "#all" :onClick all-items} "All"))))))


(def reconciler
  (om/reconciler {:state store/state}))

(om/add-root! reconciler
  App (gdom/getElement "app"))

(defn on-js-reload [])
  ; (let [elem (gdom/getElement "input")]
  ;   (when elem (.focus elem)))

  ;; optionally touch your store/state to force rerendering depending on
  ;; your application
  ;; (swap! store/state update-in [:__figwheel_counter] inc)
