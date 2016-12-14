(ns smidje.cljs-generator.test-builder)

(defn generate-assertion [assertion]
  (let [test-function#   (:function-under-test assertion)
        expected-result# (:expected-result assertion)]
    `(cljs.test/is (cljs.core/= ~test-function# ~expected-result#))))

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