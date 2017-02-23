(ns smidje.cljs-generator.mocks
  (:require [clojure.test :refer [is]]))

(defn return-or-throw [{:keys [arrow result] :as m}]
    (cond
      (= arrow :=>) result
      (= arrow :=throws=>) (throw result)
      :else #?(:clj (throw (Exception. "unknown arrow in provided"))
               :cljs (throw (js/Error "unknown arrow in provided")))))

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
        (return-or-throw (get mock-config clean-params))))))

(defn validate-no-unexpected-calls [function mock-info]
  (doseq [actual-call-param (keys (:calls mock-info))]
    (is (contains? (:mock-config mock-info) actual-call-param)
        (str function " called with unexpected args " actual-call-param))))

(defn validate-mock-called-with-expected-args [function mock-info]
  (doseq [expected-call-param (keys (:mock-config mock-info))]
    (let [mock-config (:mock-config mock-info)
          times-called (or (get (:calls mock-info) expected-call-param) 0)
          call-info (get mock-config expected-call-param)
          times-info (:times call-info)
          function-string (str "(" function " " expected-call-param ")")]
        (cond
          ; default with no :times defined: require one or more
          (nil? times-info)
          (is (>= times-called 1)
              (str function-string " expected to be called, but never invoked"))
          ; optional number of times: no validation
          (= times-info :optional) nil
          ; exact call count specified
          (integer? times-info)
          (is (= times-called times-info)
              (str function-string " expected to be called " times-info " times; was called " times-called " times"))
          ; range of acceptable values specified
          (contains? times-info :range)
          (let [[min max] (:range times-info)]
            (is (<= min times-called max)
                (str function-string " expected to be called "
                     min " to " max " times, was called " times-called " time(s)")))
            ; shouldn't get here, but just in case
          :else
          (throw (RuntimeException. "unsupported use of :times in (provided)"))))))

(defn validate-mocks [mocks-atom]
  (doseq [[function mock] @mocks-atom]
    (validate-mock-called-with-expected-args function mock)
    (validate-no-unexpected-calls function mock)))