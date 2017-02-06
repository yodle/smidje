(ns smidje.cljs-generator.test-builder
  (:require [smidje.parser.arrows :refer [arrow-set]]
            [smidje.parser.checkers :refer [truthy falsey TRUTHY FALSEY truth-set]]
            [smidje.cljs-generator.cljs-syntax-converter :refer [clj->cljs]]
            [clojure.test :refer [deftest is]]))

(defn do-arrow [arrow]
  (cond
    (= arrow '=>) '=
    (= arrow '=not=>) 'not=
    :else (throw (Exception. (format "Unknown arrow given: %s | Valid arrows: %s"
                                     arrow
                                     arrow-set)))))

(defn do-truth-test [form]
  (cond
    (= form 'truthy) true
    (= form 'TRUTHY) true
    (= form 'falsey) false
    (= form 'FALSEY) false
    :else (throw (Exception. (format "Unknown truth testing expression: %s | Valid expressions: %s"
                                     form truth-set)))))

(defn generate-mock-binding [mocks-atom]
  (fn [mock-data-map]
    (let [[function-key {function :function}] mock-data-map
          mock-function-template `(smidje.core/generate-mock-function ~function-key ~mocks-atom)]
      [function mock-function-template])))

(defn generate-mock-bindings [provided mocks-atom]
  (into [] (reduce concat (map (generate-mock-binding mocks-atom) provided))))

(defn generate-mock-map [provided]
  (reduce
    (fn [current-map addition]
      (let [{mock-config :return
             function    :mock-function} addition
            function-key (str function)]
        (merge current-map {function-key {:mock-config mock-config
                                          :function    function}})))
    {}
    provided))

(defn generate-single-assert [assertion]
  (let [{arrow#           :arrow
         test-function#   :call-form
         expected-result# :expected-result} assertion]
    `(cond
       (fn? ~expected-result#) (is (~(do-arrow arrow#) (~expected-result# ~test-function#) true))
       :else (is (~(do-arrow arrow#) ~test-function# ~expected-result#)))))

(defn generate-truth-test [truth-test-definition]
  (let [truth-type# (:truth-testing truth-test-definition)
        test-function# (:call-form truth-test-definition)]
    `(is (= (boolean ~test-function#) ~(do-truth-test truth-type#)))))

(defn generate-expected-exception [exception-definition]
  (let [expected-exception (:throws-exception exception-definition)
        call-form (:call-form exception-definition)]
    `(is (~'thrown? ~(symbol expected-exception) ~call-form))))

(defn generate-assertion
  [assertion]
  (cond
    (:truth-testing assertion) (generate-truth-test assertion)
    (:throws-exception assertion) (generate-expected-exception assertion)
    :else (generate-single-assert assertion)))

(defn list-contains? [list object]
  (some #(= object %) list))

(defn parse-metaconstant-functions [metaconstants mock-map]
  (into
    {}
    (filter
      (fn [[keystring :as mock]]
        (when (list-contains? metaconstants (keyword keystring))
          mock))
      mock-map)))

(defn generate-wrapped-assertion [metaconstants assertion]
  (let [{provided# :provided} assertion
        complete-mock-map (generate-mock-map provided#)
        mocks-atom (gensym "mocks-atom")
        unbound-mocks (parse-metaconstant-functions metaconstants complete-mock-map)
        bound-mocks (apply dissoc complete-mock-map (keys unbound-mocks))]
    `(let ~(into [] (concat [mocks-atom `(atom ~complete-mock-map)]
                            (generate-mock-bindings unbound-mocks mocks-atom)))
       (with-redefs ~(generate-mock-bindings bound-mocks mocks-atom)
         ~(generate-assertion assertion)
          (smidje.core/validate-mocks ~mocks-atom)))))

(defn generate-metaconstant-bindings [metaconostants]
   (->> (map
          (fn [metaconstant]
            [(symbol (name metaconstant))
             `(fn [])])
          metaconostants)
        (flatten)
        (into [])))

(defn generate-test [test-definition]
  (let [{assertions# :assertions
         name# :name
         metaconstants# :metaconstants} test-definition]
    `(deftest ~(symbol name#)
       (let ~(generate-metaconstant-bindings metaconstants#)
         ~@(map
             (partial generate-wrapped-assertion metaconstants#)
             assertions#)))))

(defn generate-tests [test-runtime]
  (let [tests# (:tests test-runtime)]
    (clj->cljs `(do ~@(map generate-test tests#)))))
