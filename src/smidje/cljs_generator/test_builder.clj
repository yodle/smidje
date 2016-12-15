(ns smidje.cljs-generator.test-builder
    (:require [smidje.arrows :refer [arrow-set]]
              [smidje.cljs-generator.mocks :refer [generate-mock-function]]))

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
  (into [] (reduce conj (map (generate-mock-binding mocks-atom) provided))))

(defn generate-assertion [assertion]
      (let [{test-function#   :call-form
             expected-result# :expected-result
             arrow#           :arrow
             provided#        :provided} assertion
             mocks-atom       (gensym)]
           `(cljs.core/let [~mocks-atom (cljs.core/atom ~provided#)]
              (cljs.core/with-redefs ~(generate-mock-bindings provided# mocks-atom)
                                   (cljs.test/is (~(do-arrow arrow#) ~test-function# ~expected-result#))))))

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