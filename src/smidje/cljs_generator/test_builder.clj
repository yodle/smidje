(ns smidje.cljs-generator.test-builder
    (:require [smidje.arrows :refer [arrow-set]]
              [smidje.cljs-generator.mocks :refer [generate-stateful-mock]]))

(defn do-arrow [arrow]
      (cond
        (= arrow '=>) 'cljs.core/=
        (= arrow '=not>) 'cljs.core/not=
        (= arrow '=not=>) 'cljs.core/not=
        :else (throw (Exception. (format "Unknown arrow given: %s | Valid arrows: %s"
                                         arrow
                                         arrow-set)))))

(defn generate-mock-binding [mocks-atom]
  (let [mock-config-var (gensym "mock-config")
        function-name-var (gensym "function-name")
        mock-var (gensym "mock")]
  `(cljs.core/fn [mock-data-map#]
     (cljs.core/let [{~mock-config-var :return
                     ~function-name-var :mock-function} mock-data-map#
                     ~mock-var ~(generate-stateful-mock mock-config-var)]
       (cljs.core/swap! ~mocks-atom #(cljs.core/conj % (:mock-state-atom ~mock-var)))
       [~function-name-var (:mock-function ~mock-var)]))))

(defn generate-mock-bindings [provided mocks-atom]
  `(doall (cljs.core/into [] (cljs.core/reduce cljs.core/conj (cljs.core/map ~(generate-mock-binding mocks-atom) ~provided)))))

(defn generate-single-assert [assertion]
  (let [{arrow#           :arrow
         test-function#   :call-form
         expected-result# :expected-result} assertion]
    `(cljs.test/is (~(do-arrow arrow#) ~test-function# ~expected-result#))))

(defn generate-assertion [assertion]
      (let [{provided#  :provided} assertion
             mocks-atom (gensym)]
           `(cljs.core/let [~mocks-atom (cljs.core/atom [])]
              (cljs.core/with-redefs ~(generate-mock-bindings provided# mocks-atom)
                ~(generate-single-assert assertion)))))

(defn generate-test [test-definition]
  (let [assertions# (:assertions test-definition)
        name#       (:name test-definition)]
    `(cljs.test/deftest ~(symbol name#)
       ~@(map generate-assertion assertions#))))

(defn generate-tests [test-runtime]
  (let [tests# (:tests test-runtime)]
     `(do ~@(map generate-test tests#))))

(defmacro testmacro [test-runtime]
  (generate-tests test-runtime))
