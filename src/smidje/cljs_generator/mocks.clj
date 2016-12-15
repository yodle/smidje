(ns smidje.cljs-generator.mocks)

(defn generate-mock
  "generates a mock given a mock-data object of the form
  {<param-list> {:result <return value>
                          :calls  <expected times called>
                          :arrow  '=>'}}"
  [mock-config]
  '(cljs.core/let [mock-state# (cljs.core/atom {:mock-config mock-config})]
    (fn[& params#]
      (cljs.core/if
        (cljs.core/contains? (get @mock-state# :mock-config) params#)
        (cljs.core/get-in @mock-state# [:mock-config params# :result])
        (cljs.core/throw (Exception. (str "mock called with " params# " but no response was configured")))))))