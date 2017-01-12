(ns smidje.cljs-generator.cljs-syntax-converter
  (:require [clojure.string :refer [split join]]))

(def namespace-map
  {"clojure.core" "cljs.core"
   "clojure.test" "cljs.test"})

(defn- generate-namespaced-symbol
  "this will return a symbol with a new namespace applied if a mapping exists
  otherwise this returns the symbol with it's existing namespace. for example
  clojure.test/is -> cljs.test/is
  my.namespace/func -> my.namespace/func"
  [[namespace function]]
  (let [new-namespace (or (get namespace-map namespace) namespace)]
      (symbol (join "/" [new-namespace function]))))

(defn- convert-symbol
  "given a symbol this will return a clojurescript compatible symbol for example
  clojure.test/is -> cljs.test/is
  func -> func"
  [symbol]
  (let [parsed-symbol (split (str symbol) #"/")
        symbol-count  (count parsed-symbol)]
    (cond
      (= symbol-count 1) symbol
      (= symbol-count 2) (generate-namespaced-symbol parsed-symbol)
      :else (throw (Exception. (str "could not convert symbol " symbol))))))

(defn clj->cljs
  "given a code snippet it will return a clojurescript compatible snippet"
  [code]
  (cond
    (seq? code) (map clj->cljs code)
    (vector? code) (into [] (map clj->cljs code))
    (symbol? code) (convert-symbol code)
    :else code))
