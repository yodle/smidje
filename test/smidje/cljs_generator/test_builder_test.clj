(ns smidje.cljs-generator.test-builder-test
  (:require [midje.sweet :refer :all]
            [smidje.intermediate-maps :as im]
            [smidje.cljs-generator.test-builder :refer :all]))

(defn single-expect-match-cljs [simple-addition-fn]
  `(do
     (cljs.test/deftest ~(symbol "addition is simple")
       (cljs.test/is (cljs.core/= ~simple-addition-fn 2)))))

(fact "a single expect match arrow fact"
      (generate-tests (im/single-expect-match-map ..simple-addition..))
      => (single-expect-match-cljs ..simple-addition..))

(defn multiple-expect-match-cljs [simple-addition-fn ternary-addition-fn]
  `(do
     (cljs.test/deftest ~(symbol "more addition testing")
       (cljs.test/is (cljs.core/= ~simple-addition-fn 2))
       (cljs.test/is (cljs.core/= ~ternary-addition-fn 3)))))

(fact "multiple assertion fact"
      (generate-tests (im/multiple-expect-match-map ..simple-addition.. ..ternary-addition..))
      => (multiple-expect-match-cljs ..simple-addition.. ..ternary-addition..))

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
