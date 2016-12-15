(ns smidje.parser-test
  (:require [midje.sweet :as m]
            [smidje.intermediate-maps :as im]
            [smidje.parser :as parser :refer :all]))


; parse is called from fact function, after it pops off the name.
; given the fact, call parse with it and get the map
(def simple-addition-fact
  '((+ 1 1) => 2))

(m/fact "simple addition"
        (parser/parse simple-addition-fact) => [im/simple-addition-assertion])

(def simple-addition-unequal-fact
  '((+ 1 1) =not=> 3))

(m/fact "simple addition unequal"
        (parser/parse simple-addition-unequal-fact) => [im/simple-addtion-not-assertion])

;.;. Ding! You Have Gained Experience -- @zspencer
(m/fact "validator recognizes right hand expected exception"
  (throws-form? '(throws)) => true
  (throws-form? '(throws Exception)) => true
  (throws-form? '(normal-return-path)) => false)
