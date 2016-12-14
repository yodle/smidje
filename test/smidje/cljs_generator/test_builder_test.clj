(ns smidje.cljs-generator.test-builder-test
  (:require [midje.sweet :refer :all]
            [smidje.cljs-generator.test-builder :refer :all]))

(def test-metadata
  {:smidje/namespace 'smidje.core-test
   :smidje/file ""})

(def simple-addition-assertion-1
  {:expected-result-form '2
   :expected-result 2
   :arrow '=>
   :call-form '(+ 1 1)
   :function-under-test (clojure.core/fn [] (+ 1 1))
   :position 1})

(def simple-addition-assertion-2
  {:expected-result-form '3
   :expected-result 3
   :arrow '=>
   :call-form '(+ 1 1 1)
   :function-under-test (clojure.core/fn [] (+ 1 1 1))
   :position 2})

(def single-expect-match-map
  (merge
   test-metadata
   {:smidje/source '(fact (+ 1 1) => 2)
    :tests `[:description "addition is simple"
             :assertions [~simple-addition-assertion-1]]}))

(def single-expect-match-cljs
  '(cljs.test/deftest
     (cljs.test/is (clojure.core/= (clojure.core/+ 1 1) 2))))

(fact "a single expect match arrow fact"
      (generate-tests single-expect-match-map) => single-expect-match-cljs)

(def multiple-expect-match-map
  (merge
   test-metadata
   {:smidje/source '(fact (+ 1 1) => 2 (+ 1 1 1) => 3)
    :tests `[:description "more addition testing"
             :assertions [~simple-addition-assertion-1
                          ~simple-addition-assertion-2]]}))

(def multiple-expect-match-cljs
  '(cljs.test/deftest
     (cljs.test/is (clojure.core/= (clojure.core (+ 1 1) 2)))
     (cljs.test/is (clojure.core/= (clojure.core (+ 1 1 1)) 3))))

(fact "multiple assertion fact"
      (generate-tests multiple-expect-match-map) => multiple-expect-match-cljs)


(comment

  (fact "addition is simple"
        (+ 1 1) => 2)

  (require '[clojure.test :refer :all])

  (deftest simple-addition (is (= (+ 1 1) 2)))

  ;; macroexpand-1 on (fact)
 (midje.checking.facts/creation-time-check
  (midje.data.compendium/record-fact-existence!
   (clojure.core/with-meta
     (clojure.core/fn
      []
      (midje.parsing.util.wrapping/midje-wrapped
       (midje.data.prerequisite-state/with-installed-fakes
         (midje.parsing.1-to-explicit-form.parse-background/background-fakes)
         (midje.parsing.util.wrapping/midje-wrapped
          (midje.checking.checkables/check-one
           (clojure.core/merge
            {:description (midje.data.nested-facts/descriptions),
             :expected-result-form '2,
             :check-expectation :expect-match,
             :midje.parsing.lexical-maps/a-midje-checkable-map? true,
             :function-under-test (clojure.core/fn [] (+ 1 1)),
             :expected-result 2,
             :position (pointer.core/line-number-known 1),
             :namespace clojure.core/*ns*}
            {:arrow '=>, :call-form '(+ 1 1)}
            (clojure.core/hash-map
             :position
             (pointer.core/line-number-known 1)))
           [])))))
     (clojure.core/merge  ;; metadata on the (fn)
                          {:midje/guid "520731e4809094365927da73754cf79df0484001",
                           :midje/source '(fact (+ 1 1) => 2),
                           :midje/namespace 'user,
                           :midje/file "/tmp/form-init2248583821137990902.clj",
                           :midje/line 1}
                          {:midje/top-level-fact? true})))))
