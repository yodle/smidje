(ns smidje.cljs-generator.mocks
  (:require [clojure.test :refer [is]]))

(defn generate-mock-function
  "generates a mock given a mock-config-atom object of the form
  {<function-key>
    {:mock-config
      {<arg>
        {:result [<values to return>]
         :arrow  <arrow>}}}}"
  [function-key mock-config-atom]
  (fn [& params]
    (let [mock-config (get-in @mock-config-atom [function-key :mock-config])
          clean-params (or params [])]
      (swap! mock-config-atom update-in [function-key :calls clean-params] (fnil inc 0))
      (when (contains? mock-config clean-params)
        (get-in mock-config [clean-params :result])))))

(defn validate-no-unexpected-calls [function mock-info]
  (doseq [actual-call-param (keys (:calls mock-info))]
    (is (contains? (:mock-config mock-info) actual-call-param)
        (str function " called with unexpected args " actual-call-param))))

(defn validate-mock-called-with-expected-args [function mock-info]
  (doseq [expected-call-param (keys (:mock-config mock-info))]
    (is (>= (or (get (:calls mock-info) expected-call-param) 0) 1)
        (str function " expected to be called with " expected-call-param " but never invoked"))))

(defn validate-mocks [mocks-atom]
  (doseq [[function mock] @mocks-atom]
    (validate-mock-called-with-expected-args function mock)
    (validate-no-unexpected-calls function mock)))