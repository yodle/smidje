(ns smidje.cljs-generator.mocks)

(defn generate-mock-function
  "generates a mock given a mock-data object of the form
  {<param-list> {:result <return value>
                 :calls  <expected times called>
                 :arrow  '=>}}"
  [function mock-config-atom]
  `(cljs.core/fn [& params#]
     (let [mock-config# (cljs.core/get-in (cljs.core/deref ~mock-config-atom) [~function :mock-config])
           clean-params# (or params# [])]
       (cljs.core/swap! ~mock-config-atom cljs.core/update-in [~function :calls clean-params#] (cljs.core/fnil cljs.core/inc 0))
       ;(cljs.core/println (cljs.core/deref ~mock-config-atom))
       (if
         (cljs.core/contains? mock-config# clean-params#)
         (cljs.core/get-in mock-config# [clean-params# :result])
         nil))))