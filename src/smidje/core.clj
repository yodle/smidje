(ns smidje.core
  (:require [smidje.cljs-generator.test-builder :as cljs-builder]
            [smidje.parser.parser :as parser]))

(defmacro fact [& args]
  (-> (parser/parse-fact &form)
      cljs-builder/generate-tests))