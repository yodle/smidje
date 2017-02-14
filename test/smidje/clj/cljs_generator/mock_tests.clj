(ns smidje.clj.cljs-generator.mock-tests
  (:require [midje.sweet :refer :all]
            [clojure.test :refer [do-report]]
            [smidje.cljs-generator.mocks :refer :all]))

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
                          :arrow  :=>}) => ..result.. :times 1))))

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
