(ns smidje.cljs-generator.mocks)

(defn generate-mock-function
  "generates a mock given a mock-data object of the form
  {<param-list> {:result <return value>
                 :calls  <expected times called>
                 :arrow  '=>}}"
  [function mock-config-atom]
  `(cljs.core/fn [& params#]
     (let [mock-config# (cljs.core/get-in (cljs.core/deref ~mock-config-atom) [~function :mock-config])]
       ;(cljs.core/swap! ~mock-config-atom
       ;                 (cljs.core/fn[value#]
       ;                   (cljs.core/update value# :calls #(cljs.core/merge-with cljs.core/+ % {params# 1}))))
       (if
         (cljs.core/contains? mock-config# params#)
         (cljs.core/get-in mock-config# [params# :result])
         nil))))