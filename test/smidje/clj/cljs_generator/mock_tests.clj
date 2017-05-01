(ns smidje.clj.cljs-generator.mock-tests
  (:require [midje.sweet :refer :all]
            [clojure.test :refer [do-report]]
            [smidje.cljs-generator.mocks :refer :all]
            [smidje.core :as c]))

(facts
  "generate-mock-function"

  (fact
    "returns a function"
    (generate-mock-function ..key.. ..atom..) => fn?)

  (fact
    "returns expected result and adds call count"
    (let [function 'func
          mock-atom (atom {function
                           {:mock-config
                            {[] {:result 1
                                 :arrow :=>}}}})]
      ((generate-mock-function function mock-atom)) => 1
      (get-in @mock-atom [function :calls []]) => 1))

  (fact
    "returns nil for call not in config and increments call count"
    (let [function 'func
          mock-atom (atom {function
                           {:mock-config
                            {[] {:result 1
                                 :arrow :=>}}
                            :calls
                            {[1] 1}}})]
      ((generate-mock-function function mock-atom) 1) => nil
      (get-in @mock-atom [function :calls [1]]) => 2))

  (fact
    "calls return or throws"
    (let [function 'func
          mock-atom (atom {function
                           {:mock-config
                            {[1] {:result 1
                                  :arrow  :=>}}}})]
      ((generate-mock-function function mock-atom) 1) => ..result..
      (provided
        (return-or-throw {:result 1
                          :arrow  :=>}) => ..result.. :times 1)))
  (fact
    "handles anything in mock"
    (let [function 'func
          mock-atom (atom {function
                           {:mock-config
                            {[c/anything] {:result 1
                                 :arrow :=>}}
                            }})]
      ((generate-mock-function function mock-atom) 2) => 1
      (get-in @mock-atom [function :calls [2]]) => 1)))

(facts
  "mock validation"
  (fact
    "validates each function in mock-config"
    (let [mock-atom (atom {:func1 :mock1
                           :func2 :mock2})]
      (validate-mocks mock-atom) => anything
      (provided
        (validate-mock-called-with-expected-args :func1 :mock1) => nil :times 1
        (validate-mock-called-with-expected-args :func2 :mock2) => nil :times 1
        (validate-no-unexpected-calls :func1 :mock1) => nil :times 1
        (validate-no-unexpected-calls :func2 :mock2) => nil :times 1)))

  (fact
    "validate-mock-called-with-expected-args true when expected call made"
    (let [function 'func
          mock-config {:mock-config
                       {[] {:result 1}}
                       :calls
                       {[]  1
                        [1] 1}}]
      (validate-mock-called-with-expected-args function mock-config) => anything
      (provided
        (do-report (as-checker #(= :pass (:type %)))) => nil :times 1)))

  (fact
    "validate-mock-called-with-expected-args false when expected call not made"
    (let [function 'func
          mock-config {:mock-config
                       {[] {:result 1}}
                       :calls
                       {[1] 1}}]
      (validate-mock-called-with-expected-args function mock-config) => anything
      (provided
        (do-report (as-checker #(= :fail (:type %)))) => nil :times 1)))

  (fact
    "validate-mock-called-with-expected-args passes when indicated :times calls made"
    (let [function 'func
          mock-config {:mock-config {[] {:result ..foo.. :times 2}},
                       :calls       {[] 2}}]
      (validate-mock-called-with-expected-args function mock-config) => anything
      (provided
        (do-report (as-checker #(= :pass (:type %)))) => nil :times 1))
    (let [function 'func
          mock-config {:mock-config {[] {:result ..foo.. :times 0}}}]
      (validate-mock-called-with-expected-args function mock-config) => anything
      (provided
        (do-report (as-checker #(= :pass (:type %)))) => nil :times 1)))

  (fact
    "validate-mock-called-with-expected-args fails when indicated :times calls not made"
    (let [function 'func
          mock-config {:mock-config {[] {:result ..foo.. :times 3}},
                       :calls       {[] 2}}]
      (validate-mock-called-with-expected-args function mock-config) => anything
      (provided
        (do-report (as-checker #(= :fail (:type %)))) => nil :times 1)))

  (fact
    "validate-mock-called-with-expected-args passes when indicated :times range calls made"
    (let [function 'func
          mock-config {:mock-config {[] {:result ..foo.. :times {:range [5 6]}}},
                       :calls       {[] 5}}]
      (validate-mock-called-with-expected-args function mock-config) => anything
      (provided
        (do-report (as-checker #(= :pass (:type %)))) => nil :times 1))
    (let [function 'func
          mock-config {:mock-config {[] {:result ..foo.. :times {:range [2 7]}}},
                       :calls       {[] 7}}]
      (validate-mock-called-with-expected-args function mock-config) => anything
      (provided
        (do-report (as-checker #(= :pass (:type %)))) => nil :times 1)))

  (fact
    "validate-mock-called-with-expected-args fails when indicated :times range calls made"
    (let [function 'func
          mock-config {:mock-config {[] {:result ..foo.. :times {:range [2 5]}}},
                       :calls       {[] 1}}]
      (validate-mock-called-with-expected-args function mock-config) => anything
      (provided
        (do-report (as-checker #(= :fail (:type %)))) => nil :times 1))
    (let [function 'func
          mock-config {:mock-config {[] {:result ..foo.. :times {:range [2 5]}}},
                       :calls       {[] 7}}]
      (validate-mock-called-with-expected-args function mock-config) => anything
      (provided
        (do-report (as-checker #(= :fail (:type %)))) => nil :times 1)))

  (fact
    "validate-mock-called-with-expected-args does nothing when optional :times specified"
    (let [function 'func
          mock-config {:mock-config {[] {:result ..foo.. :times :optional}},
                       :calls       {[] 7}}]
      (validate-mock-called-with-expected-args function mock-config) => anything
      (provided
        (do-report anything) => nil :times 0)))

  (fact
    "validate-no-unexpected-calls true when no unexpected calls"
    (let [function 'func
          mock-config {:mock-config
                       {[] {:result 1}
                        [1] {:result 2}}
                       :calls
                       {[] 1}}]
      (validate-no-unexpected-calls function mock-config) => anything
      (provided
        (do-report (as-checker #(= :pass (:type %)))) => nil :times 1)))

  (fact
    "validate-no-unexpected-calls false when unexpected call made"
    (let [function 'func
          mock-config {:mock-config
                       {[] {:result 1}}
                       :calls
                       {[1] 1}}]
      (validate-no-unexpected-calls function mock-config) => anything
      (provided
        (do-report (as-checker #(= :fail (:type %)))) => nil :times 1))))

(facts
  "return or throw"

  (fact
    "returns result"
    (return-or-throw {:arrow :=> :result ..result..}) => ..result..)

  (fact
    "throws exception"
    (return-or-throw {:arrow :=throws=> :result (Exception. "my exception")}) => (throws Exception "my exception"))

  (fact
    "throws exception on unknown arrow"
    (return-or-throw {:arrow :fake :result ..result..}) => (throws Exception "unknown arrow in provided")))

(facts
  "conditional="
  (fact
    "true for identical values"
    (conditional= :v1 :v1) => true)
  (fact
    "false for different values"
    (conditional= :v1 :v2) => false)
  (fact
    "true for a value and anything"
    (conditional= :value c/anything) => true)
  (fact
    "true for a value and anything"
    (conditional= c/anything :value) => true))

(facts
  "conditional-keymatch"
  (fact
    "returns key if identical"
    (conditional-keymatch :key :key) => :key)
  (fact
    "returns conditional-collection-match if collections"
    (conditional-keymatch ..c1.. ..c2..) => ..c3..
    (provided
      (conditional-collection-match ..c1.. ..c2..) => ..c3..))
  (fact
    "returns nil if different"
    (conditional-keymatch :key1 :key2) => nil))

(facts
  "conditional-collection-match"
  (fact
    "vectors match exactly"
    (conditional-collection-match [1 2 3] [1 2 3]) => [1 2 3])
  (fact
    "vectors match with anything"
    (conditional-collection-match [1 c/anything 3] [1 2 3]) => [1 2 3])
  (fact
    "lists match exactly"
    (conditional-collection-match '(1 2 3) '(1 2 3)) => '(1 2 3))
  (fact
    "lists match with anything"
    (conditional-collection-match `(1 ~c/anything 3) `(1 2 3)) => '(1 2 3)))

(facts
  "find matching key"
  (fact
    "matches exact"
    (find-matching-key [:1 :2] {[:1 :2] ..mock-info..}) => [:1 :2])
  (fact
    "matches anything in request"
    (find-matching-key [c/anything c/anything] {[:1 :2] ..mock-info..}) => [:1 :2])
  (fact
    "matches anything in mock-config"
    (find-matching-key [:1 :2] {[c/anything c/anything] ..mock-info..}) => [c/anything c/anything])
  (fact
    "matches mixed"
    (find-matching-key [:1 c/anything] {[c/anything :2] ..mock-info..}) => [c/anything :2]))
