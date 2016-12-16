(ns smidje.cljs-generator.test-builder
    (:require [smidje.arrows :refer [arrow-set]]
              [smidje.cljs-generator.mocks :refer [generate-mock-function]]))

(defmulti generate-right-hand
  (fn [assertion]
    (if (:throws-exception assertion)
      :generate-expected-exception
      :generate-assertion)))

(defn do-arrow [arrow]
      (cond
        (= arrow '=>) 'cljs.core/=
        (= arrow '=not>) 'cljs.core/not=
        (= arrow '=not=>) 'cljs.core/not=
        :else (throw (Exception. (format "Unknown arrow given: %s | Valid arrows: %s"
                                         arrow
                                         arrow-set)))))

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
    `(cljs.test/is (~(do-arrow arrow#) ~test-function# ~expected-result#))))

(defn generate-assertion [assertion]
  (let [{provided#  :provided} assertion
        mock-map#  (generate-mock-map provided#)
        mocks-atom (gensym)]
    `(cljs.core/let [~mocks-atom (cljs.core/atom ~mock-map#)]
                    (cljs.core/with-redefs ~(generate-mock-bindings mock-map# mocks-atom)
                      ~(generate-single-assert assertion)))))

(defn generate-expected-exception [exception-definition]
  (let [expected-exception (:throws-exception exception-definition)
        call-form (:call-form exception-definition)]
    `(cljs.test/is (cljs.test/thrown? ~(symbol expected-exception) ~call-form))))

(defn generate-test [test-definition]
  (let [assertions# (:assertions test-definition)
        name#       (:name test-definition)]
    `(cljs.test/deftest ~(symbol name#)
       ~@(map generate-right-hand assertions#))))

(defn generate-tests [test-runtime]
  (let [tests# (:tests test-runtime)]
     `(do ~@(map generate-test tests#))))

(defmacro testmacro [test-runtime]
  (generate-tests test-runtime))

(defmethod generate-right-hand :generate-assertion
  [assertion]
  (generate-assertion assertion))

(defmethod generate-right-hand :generate-expected-exception
  [assertion]
  (generate-expected-exception assertion))
