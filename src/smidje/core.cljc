(ns smidje.core
  (:require #?(:clj [smidje.cljs-generator.test-builder :as cljs-builder])
            #?(:clj [smidje.parser.parser :as parser])
            #?(:cljs [cljs.test :refer-macros [deftest is]])
            [smidje.cljs-generator.mocks :as mocks]
            [smidje.symbols :as symbols]))

;Namespaced symbols to differentiate between smidje syntax and user variables
(def anything symbols/anything)

(defmacro fact [& args]
  (-> (parser/parse-fact &form)
      cljs-builder/generate-tests))

(defmacro tabular [& _]
  (parser/tabular* &form))

;------functions exposed for use by generated code-------

(defn generate-mock-function [function-key mocks-atom]
  (mocks/generate-mock-function function-key mocks-atom))

(defn validate-mocks [mocks-atom]
  (mocks/validate-mocks mocks-atom))
