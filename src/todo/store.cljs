(ns todo.store)


(def initial-state {:todos [] :filter-fn identity})

(defonce state (atom initial-state))

(defn- next-id
  "Gets the next id based on the last id"
  []
  (->> (sort-by :id (:todos @state)) last :id inc))

;; ACTIONS

(defn remove-todo
  "Removes 'todo' from the application state"
  [todo]
  (swap! state update-in [:todos]
    #(filterv (partial not= todo) %)))

(defn add-todo
  "Adds a new todo to the list of todos"
  [text]
  (swap! state update-in [:todos] #(conj % {:text text :id (next-id)})))


(defn- toggle-if-same
  [todo item]
  (if (= item todo)
    (update-in item [:completed] not (:completed item))
    item))

(defn toggle-todo
  "Toggles the todo's 'completed' property"
  [todo]
  (swap! state update-in [:todos] #(mapv (partial toggle-if-same todo) %)))

(defn set-filter
  [f]
  (swap! state assoc-in [:filter-fn] f))


;; UTILITIES

(defn items-left
  "The amount of items to do"
  []
  (->> (:todos @state)
       (filter #(not (:completed %)))
       count))

(defn filtered-todos
  "Apply the 'filter-fn' and return a new todo list"
  []
  (->> (:todos @state)
       (filter (:filter-fn @state))))

(defn ^:export reset [] reset! state initial-state)
