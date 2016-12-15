(ns smidje.cljs-generator.mocks)

(defn generate-mock-function [mock-config-atom]
  `(cljs.core/fn[& params#]
       (cljs.core/swap! ~mock-config-atom
                        (cljs.core/fn[value#]
                          (cljs.core/update value# :calls #(cljs.core/merge-with cljs.core/+ % {params# 1}))))
       (if
         (cljs.core/contains? (cljs.core/get (cljs.core/deref ~mock-config-atom) :mock-config) params#)
         (cljs.core/get-in (cljs.core/deref ~mock-config-atom) [:mock-config params# :result])
         nil)))

(defn generate-stateful-mock
  "generates a mock given a mock-data object of the form
  {<param-list> {:result <return value>
                 :calls  <expected times called>
                 :arrow  '=>}}"
  [mock-config]
  (let [mock-state-var (gensym "mock-state-var")]
    `(cljs.core/let [~mock-state-var (cljs.core/atom {:mock-config ~mock-config})]
     { :mock-function ~(generate-mock-function mock-state-var)
       :mock-state-atom ~mock-state-var})))
