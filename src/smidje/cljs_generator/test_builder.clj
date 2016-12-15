(ns smidje.cljs-generator.test-builder
    (:require [smidje.arrows :refer [arrow-set]]
              [smidje.cljs-generator.mocks :refer [generate-mock]]))

(defn do-arrow [arrow]
      (cond
        (= arrow '=>) 'cljs.core/=
        (= arrow '=not>) 'cljs.core/not=
        (= arrow '=not=>) 'cljs.core/not=
        :else (throw (Exception. (format "Unknown arrow given: %s | Valid arrows: %s"
                                         arrow
                                         arrow-set)))))

(defn generate-mock-binding [mock-data-map]
  (let [{mock-config# :return
         function-name# :mock-function} mock-data-map]
    [function-name# (generate-mock mock-config#)]))

(defn generate-mock-bindings [provided]
  (into [] (reduce conj (map generate-mock-binding provided))))

(defn generate-assertion [assertion]
      (let [{test-function#   :call-form
             expected-result# :expected-result
             arrow#           :arrow
             provided#        :provided} assertion]
           `(cljs.core/with-redefs ~(generate-mock-bindings provided#)
                                   (cljs.test/is (~(do-arrow arrow#) ~test-function# ~expected-result#)))))

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