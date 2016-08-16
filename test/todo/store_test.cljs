(ns todo.store-test
  (:require
   [cljs.test :refer-macros [deftest testing is use-fixtures]]
   [todo.store :as store]))

(use-fixtures :each
 {:before #(reset! store/state {:todos [] :filter-fn identity})})

(deftest add-todo-test
  (do (store/add-todo "hey")
      (is (= (count (:todos @store/state)) 1))))

(deftest remove-todo-test
  (do (store/add-todo "hey")
      (store/remove-todo (get-in @store/state [:todos 0]))
      (is (= (count (:todos @store/state)) 0))))

(deftest toggle-todo-test
  (do (store/add-todo "hey toggle me")
      (store/toggle-todo (get-in @store/state [:todos 0]))
      (is (= (get-in @store/state [:todos 0 :completed]) true))
      (store/toggle-todo (get-in @store/state [:todos 0]))
      (is (= (get-in @store/state [:todos 0 :completed]) false))))

(deftest clear-completed-test
  (do (dotimes [n 5] (store/add-todo (str "todo" n)))
      (store/toggle-todo (get-in @store/state [:todos 0]))
      (store/toggle-todo (get-in @store/state [:todos 3]))
      (store/clear-completed)
      (is (= (count (:todos @store/state)) 3))))

(deftest items-left-test
  (do (dotimes [n 5] (store/add-todo (str "todo" n)))
      (store/toggle-todo (get-in @store/state [:todos 0]))
      (store/toggle-todo (get-in @store/state [:todos 2]))
      (is (= (store/items-left) 3))))
