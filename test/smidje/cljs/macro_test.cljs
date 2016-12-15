(ns smidje.cljs.macro-test
    (:require [cljs.test :refer [deftest is]]
              [cljs.test :refer [deftest is]])
    (:require-macros [smidje.parser :refer [fact]]
                     [smidje.cljs-generator.test-builder :refer [testmacro]]))

(defn bar []
  1)

(defn foo []
  (bar))


(testmacro {:tests [
                    {:name "providedtest",
                     :assertions
                           [{:call-form (foo),
                             :expected-result 2
                             :arrow =>
                             :provided {:bar {:mock-config {nil {:result 3
                                                       :calls 2
                                                       :arrow =>}}
                                              :function bar}}}]}]})


;(do
;  (cljs.test/deftest providedtest
;    (cljs.core/let [G__7904 (cljs.core/atom [])]
;      (cljs.core/with-redefs
;                               (cljs.core/into []
;                                               (cljs.core/reduce
;                                                 cljs.core/conj
;                                                 (cljs.core/map
;                                                   (cljs.core/fn [mock-data-map__7886__auto__]
;                                                     (cljs.core/let [{mock-config7905 :return, function-name7906 :mock-function} mock-data-map__7886__auto__
;                                                                     mock7907 (cljs.core/let [mock-state-var7908 (cljs.core/atom {:mock-config mock-config7905})]
;                                                                                                            {:mock-state-atom mock-state-var7908,
;                                                                                                             :mock-function (cljs.core/fn
;                                                                                                                              [& params__7869__auto__]
;                                                                                                                              (cljs.core/swap! mock-state-var7908
;                                                                                                                                               (cljs.core/fn [value__7870__auto__]
;                                                                                                                                                 (cljs.core/update value__7870__auto__
;                                                                                                                                                                   :calls
;                                                                                                                                                                   (fn* [p1__7868__7871__auto__]
;                                                                                                                                                                        (cljs.core/merge-with
;                                                                                                                                                                          cljs.core/+
;                                                                                                                                                                          p1__7868__7871__auto__
;                                                                                                                                                                          {params__7869__auto__ 1})))))
;                                                                                                                              (if
;                                                                                                                                (cljs.core/contains? (cljs.core/get (cljs.core/deref mock-state-var7908) :mock-config) params__7869__auto__)
;                                                                                                                                (cljs.core/get-in (cljs.core/deref mock-state-var7908) [:mock-config params__7869__auto__ :result])
;                                                                                                                                nil))})]
;                                                       (cljs.core/swap! G__7904
;                                                                        (fn* [p1__7885__7887__auto__]
;                                                                             (cljs.core/conj
;                                                                               p1__7885__7887__auto__
;                                                                               (:mock-state-atom mock7907))))
;                                                       [function-name7906 (:mock-function mock7907)]))
;                                                   [{:mock-function bar, :return {nil {:result 2, :calls 2, :arrow =>}}}])))
;                             (cljs.test/is (cljs.core/= (foo) 2))))))



;(defn m [] (do (cljs.core/into []
;                (cljs.core/reduce
;                  cljs.core/conj
;                  (cljs.core/map
;                    (cljs.core/fn [mock-data-map__7886__auto__]
;                      (cljs.core/let [{mock-config7905 :return, function-name7906 :mock-function} mock-data-map__7886__auto__
;                                      mock7907 (cljs.core/let [mock-state-var7908 (cljs.core/atom {:mock-config mock-config7905})]
;                                                 {:mock-state-atom mock-state-var7908,
;                                                  :mock-function   (cljs.core/fn
;                                                                     [& params__7869__auto__]
;                                                                     (cljs.core/swap! mock-state-var7908
;                                                                                      (cljs.core/fn [value__7870__auto__]
;                                                                                        (cljs.core/update value__7870__auto__
;                                                                                                          :calls
;                                                                                                          (fn* [p1__7868__7871__auto__]
;                                                                                                               (cljs.core/merge-with
;                                                                                                                 cljs.core/+
;                                                                                                                 p1__7868__7871__auto__
;                                                                                                                 {params__7869__auto__ 1})))))
;                                                                     (if
;                                                                       (cljs.core/contains? (cljs.core/get (cljs.core/deref mock-state-var7908) :mock-config) params__7869__auto__)
;                                                                       (cljs.core/get-in (cljs.core/deref mock-state-var7908) [:mock-config params__7869__auto__ :result])
;                                                                       nil))})]
;                        [function-name7906 (:mock-function mock7907)]))
;                    [{:mock-function bar, :return {nil {:result 2, :calls 2, :arrow =>}}}])))))
;
;(println (m))

;(testmacro {:tests [
;                    {:name "mytest",
;                     :assertions
;                           [{:call-form (+ 1 1),
;                             :expected-result 2}]}]})

;(fact "name"
;  (+ 1 1) => 2
;  (+ 1 3) =not=> 2
;  (+ 1 3) =not> 2)

;(fact "name"
;      (foo) => 2
;      (provided
;        (bar)=>2))