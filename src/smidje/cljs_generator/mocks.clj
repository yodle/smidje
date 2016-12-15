(ns smidje.cljs-generator.mocks)

(defn generate-mock-function [mock-config-atom]
  `(cljs.core/let [atom-value# (cljs.core/deref ~mock-config-atom)]
     (cljs.core/fn[& params#]
       ;(swap! ~mock-config-atom)
       (if
         (cljs.core/contains? (cljs.core/get atom-value# :mock-config) params#)
         (cljs.core/get-in atom-value# [:mock-config params# :result])
         (throw (js/Error. (cljs.core/str "mock called with undefined params " params#)))))))

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
