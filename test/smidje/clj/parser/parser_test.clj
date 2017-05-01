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
        (parser/parse-assertions simple-addition-fact) => [im/simple-successful-addition-assertion])

(def simple-addition-unequal-fact
  '((+ 1 1) =not=> 3))

(m/fact "simple addition unequal"
        (parser/parse-assertions simple-addition-unequal-fact) => [im/simple-addition-successful-not-assertion])

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

(m/fact "separate-provided-forms"
        (#'smidje.parser.parser/separate-provided-forms simple-addition-fact) => [simple-addition-fact]
        (set  (#'smidje.parser.parser/separate-provided-forms
               '((+ 1 1) => 2 :times 1 (* 2 3) => 6 :times 2 :except 3 (/ 4 2) => 2))) => '#{((+ 1 1) => 2 :times 1)
                                                                                      ((/ 4 2) => 2)
                                                                                     ((* 2 3) => 6 :times 2 :except 3)})

(m/fact "adjust-range"
        (adjust-range '((+ 1 1) => 2 :times 1 :except 3)) m/=> '((+ 1 1) => 2 :times 1 :except 3)
        (adjust-range '((+ 1 1) => 2 :times (range) :except 3)) m/=> '((+ 1 1) => 2 :times :optional :except 3)
        (adjust-range '((+ 1 1) => 2 :times (range 3) :except 3)) m/=> '((+ 1 1) => 2 :times {:range [1 3]} :except 3)
        (adjust-range '((+ 1 1) => 2 :times (range 2 10) :except 3)) m/=> '((+ 1 1) => 2 :times {:range [2 10]} :except 3)
        (adjust-range '((+ 1 1) => 2 :times (range 1 2 3))) m/=> (m/throws RuntimeException #"more than two arguments")
        (adjust-range '((+ 1 1) => 2 :times (range -1 3))) m/=> (m/throws RuntimeException #"-1 must be greater than 0")
        (adjust-range '((+ 1 1) => 2 :times (range 1 -3))) m/=> (m/throws RuntimeException #"-3 must be greater than 0")
        (adjust-range '((+ 1 1) => 2 :times (range 3 1))) m/=> (m/throws RuntimeException #"3 must be less than or equal to 1")
        (adjust-range '((+ 1 1) => 2 :times (range -1))) m/=> (m/throws RuntimeException #"must be greater than 0"))

(m/fact "build-provided-map"
        (#'smidje.parser.parser/build-provided-map simple-addition-fact) => {:mock-function '+
                                                                      :paramaters '(1 1)
                                                                      :arrow :=>
                                                                      :result 2}
        (#'smidje.parser.parser/build-provided-map '((add 2 3) => 5 :times 1 :except 4)) => {:mock-function 'add
                                                                                      :paramaters '(2 3)
                                                                                      :arrow :=>
                                                                                      :result 5
                                                                                      :times 1
                                                                                      :except 4})

(m/fact "provided validator recognizes provided forms correctly"
        (provided-form? '(provided)) => true                ; recognizes provided form
        (provided-form? '(normal-return-path)) => false     ; does not recognize other forms as provided
        (provided-form? 2) => false)                        ; does not recognize non-forms as provided form

(m/tabular
  (m/fact "`metaconstant-name?` returns expected truthiness"
          (metaconstant-name? ?input) => ?result)
  ?input    ?result
  "..foo.." m/truthy
  "--foo--" m/truthy
  "--foo.." m/FALSEY
  "..foo--" m/FALSEY
  "-foo-"   m/FALSEY
  "foo"     m/FALSEY
  "__foo__" m/FALSEY)

(m/tabular
  (m/fact "`metaconstant?` returns expected truthiness"
          (metaconstant? ?input) => ?result)
  ?input    ?result
  '--foo--  m/truthy
  '..bar..  m/truthy
  "--foo--" m/FALSEY
  nil       m/FALSEY
  '=>       m/FALSEY
  'foo      m/FALSEY)

(m/facts "about `parse-metaconstants`"
  (m/fact "returns an empty map for non-metaconstant single input"
          (parse-metaconstants ..form..) => {}
          (m/provided
            (#'smidje.parser.parser/metaconstant? ..form..) m/=> false))
  (m/fact "returns list of expected consants given list"
          (parse-metaconstants '(this "returns" nothing)) => {}

          (parse-metaconstants '(hi ..hello.. "..there..")) => {'..hello.. ..gensym..}
          (m/provided
            (gensym "smidje->mc->dot->hello->") m/=> ..gensym..)

          (parse-metaconstants '..foo..) => {'..foo.. ..gensym..}
          (m/provided
            (gensym "smidje->mc->dot->foo->") m/=> ..gensym..)

          (parse-metaconstants '(duplicates ..are.. --not-- --not-- --not-- a problem))
          => {'..are.. ..gensym1..
              '--not-- ..gensym2..}
          (m/provided
            (gensym "smidje->mc->dot->are->") m/=> ..gensym1..
            (gensym "smidje->mc->dash->not->") m/=> ..gensym2..)

          (parse-metaconstants '(--also-- (..it.. (--will--) --flatten--)))
          => {'--also--    ..gensym1..
              '..it..      ..gensym2..
              '--will--    ..gensym3..
              '--flatten-- ..gensym4..}
          (m/provided
            (gensym "smidje->mc->dash->also->") m/=> ..gensym1..
            (gensym "smidje->mc->dot->it->") m/=> ..gensym2..
            (gensym "smidje->mc->dash->will->") m/=> ..gensym3..
            (gensym "smidje->mc->dash->flatten->") m/=> ..gensym4..)

          (parse-metaconstants '(mixed --flattened-- (inputs "--cause--" (--no-- problems) ..either..)))
          => {'--flattened-- ..gensym1..
              '--no--        ..gensym2..
              '..either..    ..gensym3..}
          (m/provided
            (gensym "smidje->mc->dash->flattened->") m/=> ..gensym1..
            (gensym "smidje->mc->dash->no->") m/=> ..gensym2..
            (gensym "smidje->mc->dot->either->") m/=> ..gensym3..)

          (parse-metaconstants '((something --foo-- ..input..) => ..result.. (provided (--foo-- ..input..) => ..result)))
          => {'--foo--    ..gensym1..
              '..input..  ..gensym2..
              '..result.. ..gensym3..}
          (m/provided
            (gensym "smidje->mc->dash->foo->") m/=> ..gensym1..
            (gensym "smidje->mc->dot->input->") m/=> ..gensym2..
            (gensym "smidje->mc->dot->result->") m/=> ..gensym3..))
  (m/fact "returns list of expected consants given map"
           (parse-metaconstants {:this "returns" :nothing "too"}) => {}

           (parse-metaconstants {:hi '..hello..}) => {'..hello.. ..gensym..}
           (m/provided
             (gensym "smidje->mc->dot->hello->") m/=> ..gensym..)

           (parse-metaconstants {:duplicates '..are.. :not '..are.. :a '--not-- :problem '--not--})
           => {'..are.. ..gensym1..
               '--not-- ..gensym2..}
           (m/provided
             (gensym "smidje->mc->dot->are->") m/=> ..gensym1..
             (gensym "smidje->mc->dash->not->") m/=> ..gensym2..)

           (parse-metaconstants {:val    '--also--
                                 :nested {:val    '..it..
                                          :nested {:val    '--will--
                                                   :nested {:val '--flatten--}}}})
           => {'--also--    ..gensym1..
               '..it..      ..gensym2..
               '--will--    ..gensym3..
               '--flatten-- ..gensym4..}
           (m/provided
             (gensym "smidje->mc->dash->also->") m/=> ..gensym1..
             (gensym "smidje->mc->dot->it->") m/=> ..gensym2..
             (gensym "smidje->mc->dash->will->") m/=> ..gensym3..
             (gensym "smidje->mc->dash->flatten->") m/=> ..gensym4..)))

(m/fact "`replace-metaconstants` replaceces metaconstant symbols with values in symbol table"
        (replace-metaconstants {} "foo") => "foo"

        (replace-metaconstants {} '(nothing to see here)) => '(nothing to see here)

        (replace-metaconstants
          {'--foo-- ..gensym1.. '..foo.. ..gensym2..}
          '(this --foo-- ..foo..))
        => '(this ..gensym1.. ..gensym2..)

        (replace-metaconstants
          {'--mc1-- ..gensym1.. '..mc1.. ..gensym2.. '..mc2.. ..gensym3..}
          '(--mc1-- ..mc1.. (--mc1-- "..mc1.." ..mc2.. (..mc2.. "foo") ..mc1..)))
        => '(..gensym1.. ..gensym2.. (..gensym1.. "..mc1.." ..gensym3.. (..gensym3.. "foo") ..gensym2..)))