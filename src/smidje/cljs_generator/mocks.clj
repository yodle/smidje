(ns smidje.cljs-generator.mocks)

(defn generate-mock
  "generates a mock given a mock-data object of the form
  {<param-list> {:result <return value>
                          :calls  <expected times called>
                          :arrow  '=>'}}"
  [mock-config]
  `(cljs.core/let [mock-state# (cljs.core/atom {:mock-config ~mock-config})]
    (fn[& params#]
      (if
        (cljs.core/contains? (cljs.core/get (cljs.core/deref mock-state#) :mock-config) params#)
        (cljs.core/get-in (cljs.core/deref mock-state#) [:mock-config params# :result])
        (throw (js/Error. (cljs.core/str "mock called with undefined params " params#)))))))