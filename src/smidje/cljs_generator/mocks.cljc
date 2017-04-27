(ns smidje.cljs-generator.mocks
  (:require [clojure.test :refer [is]]
            [smidje.symbols :refer [anything => =throws=>]]))

(defn return-or-throw [{:keys [arrow result] :as m}]
    (cond
      (= arrow =>) result
      (= arrow =throws=>) (throw result)
      :else #?(:clj (throw (Exception. "unknown arrow in provided"))
               :cljs (throw (js/Error "unknown arrow in provided")))))

(defn conditional= [value1 value2]
  (cond
    (= value1 value2)    true
    (= value1 anything)  true
    (= value2 anything)  true
    :else                false))

(defn conditional-collection-match [expected actual]
  (when (and (= (count expected) (count actual))
        (every? true? (map conditional= expected actual)))
    actual))

(defn conditional-keymatch
  "given two keys this will return the conditional-key if it matches the exact-key or nil
   keys are equal if:
   1) they are exactly the same
   2) if the keys are a vector or list where for each element in exact-key the corresponding element in
   conditional-key is either the same or :anything"
  [exact-key conditional-key]
  (cond
    (= exact-key conditional-key)                       conditional-key
    (and (coll? exact-key) (coll? conditional-key))     (conditional-collection-match exact-key conditional-key)
    :else nil))

(defn find-matching-key
  "this function returns the key from mock-config that matches request-params or nill
  it will treat :anything as wildcard when validating that the key is valid for example
  [1 2] will math the key in {[1 :anything] ..value..} "
  [request-params mock-config]
    (some (fn [[conditional-key _]] (conditional-keymatch request-params conditional-key)) mock-config))

(defn generate-mock-function
  "generates a mock given a mock-config-atom object of the form
  {<function-key>
    {:mock-config
      {<arg>
        {:result [<values to return>]
         :arrow  <arrow>}}}}"
  [function-key mock-config-atom]
  (fn [& params]
    (let [mock-config    (get-in @mock-config-atom [function-key :mock-config])
          clean-params   (or params [])
          request-key    (find-matching-key clean-params mock-config)
          request-result (get mock-config request-key)]
      (swap! mock-config-atom update-in [function-key :calls clean-params] (fnil inc 0))
      (when request-result
        (return-or-throw request-result)))))

(defn validate-no-unexpected-calls [function mock-info]
  (doseq [actual-call-param (keys (:calls mock-info))]
    (is (some (fn [[key value]] (conditional-keymatch actual-call-param key)) (:mock-config mock-info))
        (str function " called with unexpected args " actual-call-param))))

(defn validate-mock-called-with-expected-args [function mock-info]
  (doseq [expected-call-param (keys (:mock-config mock-info))]
    (let [matching-key (find-matching-key expected-call-param (:calls mock-info))]
      (is (>= (or (get (:calls mock-info) matching-key) 0) 1)
          (str function " expected to be called with " expected-call-param " but never invoked")))))

(defn validate-mocks [mocks-atom]
  (doseq [[function mock] @mocks-atom]
    (validate-mock-called-with-expected-args function mock)
    (validate-no-unexpected-calls function mock)))