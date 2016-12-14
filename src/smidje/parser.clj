(ns smidje.parser
  (:require [smidje.arrows :refer :all]
            [smidje.cljs-generator.test-builder :as cljsbuilder]))

(declare generate)

(defn- parse-equals
  [forms]
  (let [call-form (nth forms 0)
        arrow (nth forms 1)
        expected-form (nth forms 2)]
    {:call-form            call-form
     :arrow                arrow
     :expected-result      expected-form
     :expected-result-form `'~expected-form}))

(defn- is-arrow
  [form]
  (or (= form '=>)
      (= form '=not=>)))

(defn parse
  [forms]
  (loop [result [] input forms]
    ; TODO: check for provided mocks
    ; TODO: assertions must be before provided mocks
    ; TODO: error messages on bad syntax
    (if (and (> (count input) 2)
             (is-arrow (second input)))
      (recur (conj result (parse-equals input)) (drop 3 input))
      result)))

(defmacro fact
  [& _]
  (let [name (if (string? (second &form))
               (clojure.string/replace (second &form) #"[^\w\d]+" "-")
               (second &form))
        fact-forms (drop 2 &form)]
    (-> {:tests [{:name name
         :assertions (parse fact-forms)}]}
        (generate))))

(defn generate [testmap]
  (cljsbuilder/generate-tests testmap))

(comment
  (macroexpand
    '(fact "what a fact"
           (+ 1 1) => 2
           (+ 2 2) =not=> 3))
)
