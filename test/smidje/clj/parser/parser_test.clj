(ns smidje.clj.parser.parser-test
  (:require [midje.sweet :as m]
            [smidje.clj.intermediate-maps :as im]
            [smidje.parser.parser :as parser :refer :all])
  (:use     [midje.util :only [expose-testables]]))

(expose-testables smidje.parser.parser)

; parse is called from fact function, after it pops off the name.
; given the fact, call parse with it and get the map
(def simple-addition-fact
  '((+ 1 1) => 2))

(m/fact "simple addition"
        (parser/parse simple-addition-fact) => [im/simple-successful-addition-assertion])

(def simple-addition-unequal-fact
  '((+ 1 1) =not=> 3))

(m/fact "simple addition unequal"
        (parser/parse simple-addition-unequal-fact) => [im/simple-addition-successful-not-assertion])

(m/fact "throws validator recognizes right hand expected exception"
  (throws-form? '(throws)) => true
  (throws-form? '(throws Exception)) => true
  (throws-form? '(normal-return-path)) => false
  (throws-form? 2) => false)

(m/fact "provided-form?"
        (#'smidje.parser.parser/provided-form? ()) => false
        (#'smidje.parser.parser/provided-form? "NOTAPROVIDEDFORM") => false
        (#'smidje.parser.parser/provided-form? '(stillnota provided form)) => false
        (#'smidje.parser.parser/provided-form? '(:provided form not)) => false
        (#'smidje.parser.parser/provided-form? '(provided form)) => true)

(m/fact "has-provided-form?"
        (#'smidje.parser.parser/has-provided-form? simple-addition-fact) => false
        (#'smidje.parser.parser/has-provided-form? '((+ 1 1) => 2 (* 1 1) => 1)) => false
        (#'smidje.parser.parser/has-provided-form? '((+ 1 1) => 2 provided (* 1 1) => 1)) => false
        (#'smidje.parser.parser/has-provided-form? '((+ 1 1) => 2 (provided (* 1 1) => 1) (- 3 1 ) => 2)) => true)

(m/fact "gen-provided-sym generates metaconst"
 (#'smidje.parser.parser/gen-provided-sym 'fn1 'fn2)  =>  '..fn1->fn2001.. (m/provided (gensym "fn1->fn2")  m/=> 'fn1->fn2001))


(m/fact "unnest-provided"
        (#'smidje.parser.parser/unnest-provided simple-addition-fact) => [simple-addition-fact]
        (set  (#'smidje.parser.parser/unnest-provided '((a 1 (b 2)) => 3))) =>  #{'((a 1 ..a->b01..) => 3) '((b 2) => ..a->b01..)}
            (m/provided (gensym "a->b") m/=> 'a->b01)
            (set  (#'smidje.parser.parser/unnest-provided '((a 1 (b (c 4)  2)) => 3))) =>
              #{'((a 1 ..a->b01..) => 3)
                '((b ..b->c02.. 2) => ..a->b01..)
                '((c 4) => ..b->c02..)}
            (m/provided (gensym "a->b") m/=> 'a->b01 (gensym "b->c") m/=> 'b->c02))

(m/fact "seperate-provided-forms"
        (#'smidje.parser.parser/seperate-provided-forms simple-addition-fact) => [simple-addition-fact]
        (set  (#'smidje.parser.parser/seperate-provided-forms
               '((+ 1 1) => 2 :times 1 (* 2 3) => 6 :times 2 :except 3 (/ 4 2) => 2))) => '#{((+ 1 1) => 2 :times 1)
                                                                                      ((/ 4 2) => 2)
                                                                                     ((* 2 3) => 6 :times 2 :except 3)})

(m/fact "build-provided-map"
        (#'smidje.parser.parser/build-provided-map simple-addition-fact) => {:mock-function '+
                                                                      :paramaters '(1 1)
                                                                      :arrow '=>
                                                                      :result 2}
        (#'smidje.parser.parser/build-provided-map '((add 2 3) => 5 :times 1 :except 4)) => {:mock-function 'add
                                                                                      :paramaters '(2 3)
                                                                                      :arrow '=>
                                                                                      :result 5
                                                                                      :times 1
                                                                                      :except 4})

(m/fact "provided validator recognizes provided forms correctly"
        (provided-form? '(provided)) => true                ; recognizes provided form
        (provided-form? '(normal-return-path)) => false     ; does not recognize other forms as provided
        (provided-form? 2) => false)                        ; does not recognize non-forms as provided form
