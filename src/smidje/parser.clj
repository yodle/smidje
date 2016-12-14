(ns smidje.parser
  (:require [smidje.arrows :refer :all]
            [smidje.cljs-generator.test-builder :as cljsbuilder]))

(declare generate)

(def provided "provided")

(defn- provided-form?
  [form]
  (and (seq? form)
       (= (first form) 'provided)))

(defn- has-provided-form? [input]
  (and (> (count input) 3)
       (provided-form? (nth input 3))))

(defn- parse-provided
  [forms]
  (if (has-provided-form? forms)
      {:provided (nth forms 3)}
      {}))

(defn- parse-equals
  [forms]
  (let [call-form (nth forms 0)
        arrow (nth forms 1)
        expected-form (nth forms 2)]
    (merge 
    {:call-form            call-form
     :arrow                arrow
     :expected-result      expected-form
     :expected-result-form `'~expected-form}
     (parse-provided forms))))


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
      (recur (conj result (parse-equals input)) (drop (if (has-provided-form? input) 4 3 ) input))
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
