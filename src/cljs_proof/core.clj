(ns cljs-proof.core)

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(ns cljs-proof.test-framework
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer :all]))

(defn done []
  )

(defmacro async [done body]
  body)

(defmacro describe
  [test-name & body]
  `(deftest ~(gensym)
    (~(symbol 'testing) ~test-name
      (~(symbol 'async) ~(symbol 'done)
       (~(symbol 'go)
        ~body
        ~(symbol 'done))))))

(defn make-mocks
  [])

(comment
  (macroexpand '(describe "some test"
                          (is (= 1 2))))

  (describe "a test"
            (is (= 1 1)))

  (G__28154)

  (G__28271))

