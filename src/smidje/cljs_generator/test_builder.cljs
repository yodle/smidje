(ns smidje.cljs-generator.test-builder
    (:require [smidje.parser.arrows :refer [arrow-set]]
              [cljs.test :refer-macros [deftest testing is run-tests]]
              ;[smidje.parser.checkers :refer [truthy falsey TRUTHY FALSEY truth-set]]
              ;[smidje.cljs-generator.mocks :refer [generate-mock-function]]
              ;[smidje.cljs-generator.cljs-syntax-converter :refer [clj->cljs]]
              )
  )

(defn do-arrow [arrow]
      (cond
        (= arrow '=>) =
        (= arrow '=not=>) not=
        :else (throw (js/Error (str "Unknown arrow given: " arrow " | Valid arrows: " arrow-set)))))

(defn do-truth-test [form]
  (cond
    (= form 'truthy) true
    (= form 'TRUTHY) true
    (= form 'falsey) false
    (= form 'FALSEY) false
    :else (throw (js/Error (str "Unknown truth testing expression: " form " | Valid expressions: " truth-set)))))

(defn generate-mock-binding [mocks-atom]
  (fn [mock-data-map]
     (let [[function-key {function :function}] mock-data-map
           mock-function (generate-mock-function function-key mocks-atom)]
       [function mock-function])))

(defn generate-mock-bindings [provided mocks-atom]
  (into [] (reduce concat (map (generate-mock-binding mocks-atom) provided))))

(defn generate-mock-map [provided]
  (reduce
    (fn[current-map addition]
      (let [{mock-config :return
             function    :mock-function} addition
             function-key (str function)]
        (merge current-map {function-key {:mock-config mock-config
                                          :function function}})) )
    {}
    provided))

(defn generate-single-assert [assertion]
  (let [{arrow#           :arrow
         test-function#   :call-form
         expected-result# :expected-result} assertion]
    (cond
       (fn? expected-result#) (is ((do-arrow arrow#) (expected-result# test-function#) true))
       :else (is ((do-arrow arrow#) test-function# expected-result#)))))

(defn generate-assertion [assertion]
  (let [{provided#  :provided} assertion
        mock-map#  (generate-mock-map provided#)
        mocks-atom (gensym)]
    (let [mocks-atom (atom mock-map#)]
                    (with-redefs ~(generate-mock-bindings mock-map# mocks-atom)
                      (generate-single-assert assertion)))))

(defn generate-truth-test [truth-test-definition]
  (let [truth-type# (:truth-testing truth-test-definition)
        test-function# (:call-form truth-test-definition)]
    (is (= (boolean test-function#) (do-truth-test truth-type#)))))

(defn generate-expected-exception [exception-definition]
  (let [expected-exception (:throws-exception exception-definition)
        call-form (:call-form exception-definition)]
    (is (thrown? (symbol expected-exception) call-form))))

(defn generate-right-hand
  [assertion]
  (cond
    (:truth-testing assertion) (generate-truth-test assertion)
    (:throws-exception assertion) (generate-expected-exception assertion)
    :else (generate-assertion assertion)))

(defn run-test [test-definition]
  (let [assertions# (:assertions test-definition)
        name#       (:name test-definition)]
    (testing name#
      (doall (map generate-right-hand assertions#)))))

(defn generate-tests [test-runtime]
  (let [tests# (:tests test-runtime)]
    (doall (map run-test tests#))))

;({:type :pass, :expected 2, :actual (2), :message nil})
;({:file test_builder_test.clj, :line 9, :type :fail, :diffs ([3 (2 3)]), :expected 2, :actual (3), :message nil})
;({:type :pass, :expected true, :actual (true), :message nil})
;({:file test_builder_test.clj, :line 9, :type :fail, :diffs ([true (false true)]), :expected false, :actual (true), :message nil})
;({:type :pass, :expected (not= (+ 1 1) 3), :actual (#object[clojure.core$not_EQ_ 0x3448a248 clojure.core$not_EQ_@3448a248] 2 3), :message nil})
;({:file test_builder_test.clj, :line 9, :type :fail, :expected (not= (+ 1 1) 2), :actual (not (not= 2 2)), :message nil})


;
;(defn process-test-data [data]
;  (println data))