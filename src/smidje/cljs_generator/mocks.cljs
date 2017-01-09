(ns smidje.cljs-generator.mocks)

(defn generate-mock-function
  "generates a mock given a mock-config-atom object of the form
  {<function-key>
    {:mock-config
      {<arg>
        {:result [<values to return>]
         :arrow  <arrow>}}}}"
  [function-key mock-config-atom]
  (fn [& params#]
     (let [mock-config# (get-in @~mock-config-atom [function-key :mock-config])
           clean-params# (or params# [])]
       (swap! mock-config-atom update-in [function-key :calls clean-params#] (fnil inc 0))
       (if
         (contains? mock-config# clean-params#)
         (get-in mock-config# [clean-params# :result])
         nil))))