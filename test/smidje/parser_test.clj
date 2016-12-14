(ns smidje.parser-test
  (:require [midje.sweet :as m]
            [smidje.intermediate-maps :as im]
            [smidje.parser :as parser :refer :all]))


; given the fact, call parse with it and get the map

(def simple-addition-fact
  '(fact "simple addition"
         (+ 1 1 => 2)))

(m/fact "simple addition"
      (parser/parse simple-addition-fact) => im/simple-addition-assertion)

