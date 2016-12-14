(ns smidje.parser
  (:require [smidje.arrows :refer :all]))

(declare generate)

(defn- parse
  [forms]
  (let [call-form (nth forms 1)
        arrow (nth forms 2)
        expected-form (nth forms 3)]
    {:call-form call-form
     :arrow     arrow
     :expected-result expected-form}))

(defmacro fact
  [& _]
  ; arrow checking
  ; evaluation of call-form

  (-> (parse &form)
      (generate)))

(defn generate [_ &]
  (println "Implement me!"))
