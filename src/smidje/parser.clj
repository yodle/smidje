(ns smidje.parser
  (:require [smidje.arrows :refer :all]
            [smidje.cljs-generator.test-builder :as cljsbuilder]
            [clojure.walk :refer [prewalk prewalk-demo stringify-keys]]))

(declare generate)
(declare fact)

(def provided "provided")
(def target-form-not-found -1)
(def no-name-fact "<John Doe Fact>")
(def no-name-tabular "<John Doe Tabular>")

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

(defn- deconstruct-forms [forms]
       {:call-form (nth forms 0)
        :arrow (nth forms 1)
        :expected-form (nth forms 2)})

(defn- parse-equals
  [forms]
   (let [{call-form     :call-form
          arrow         :arrow
          expected-form :expected-form} (deconstruct-forms forms)]
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

(defn- macro-name-position
       "Get position of target macro in from sequence"
       [form]
       (if (string? (second form)) 1 target-form-not-found))

(defn- macro-name
       "Get name of target macro in form sequence"
       [form]
       (let [target-form (second form)]
            (if (string? target-form)
              (clojure.string/replace target-form #"[^\w\d]+" "-")
              target-form)))

(defn swap-symbol-for-value [fact-template-group symbol-value-map]

      (println "(stringify-keys symbol-value-map)" (stringify-keys symbol-value-map))
      (println "(keys (stringify-keys symbol-value-map)" (keys (stringify-keys symbol-value-map)))
      (prewalk (fn [expr]

                   (let [
                         ]
                        (if (and (= \? (first (str expr)))
                                 (symbol? expr)
                                 (contains?  symbol-value-map expr))
                          (symbol-value-map expr)           ;(select-keys symbol-value-map [expr])
                          expr)))
               fact-template-group))


(defn substitute [fact-template-groups symbol-value-map result]
      (if (= 0 (count fact-template-groups))
        result
        (recur (rest fact-template-groups )
               (rest symbol-value-map)
               (conj result (swap-symbol-for-value (first fact-template-groups) (first symbol-value-map))))))

(defn symbol-value-mapper [fact-table-value-groups fact-table-binding-groups result]
      (if (= 0 (count fact-table-value-groups))
        result
        (recur (rest fact-table-value-groups)
               (rest fact-table-binding-groups)
               (conj result (zipmap (first fact-table-binding-groups) (first fact-table-value-groups))))))

(defn- gen-facts
       "Use fact template to build all the facts in the fact table"
       [fact-template fact-table-binding fact-table-values]
       (let [group-count (count fact-table-binding)
             fact-table-value-groups (partition group-count fact-table-values)
             fact-table-binding-groups (repeat group-count fact-table-binding)
             fact-template-groups (repeat group-count fact-template)
             symbol-value-map (symbol-value-mapper fact-table-value-groups fact-table-binding-groups (sequence ()))]
            (println "fact-table-value-groups" fact-table-value-groups)     ; (1 2 3) (3 4 7) (9 10 19))
            (println "fact-table-binding-groups" fact-table-binding-groups) ; ((?a ?b ?c) (?a ?b ?c) (?a ?b ?c))
            (println "fact-template-groups" fact-template-groups)           ; (((+ ?a ?b) => ?c) ((+ ?a ?b) => ?c) ((+ ?a ?b) => ?c))
            (println "symbol-value-map" symbol-value-map)                 ; {?a 9, ?b 10, ?c 19} {?a 3, ?b 4, ?c 7} {?a 1, ?b 2, ?c 3})
            (substitute fact-template-groups symbol-value-map (sequence ()))))

(defn- tabular-config
       "Used by fact and tabular to determine name and postions of next valid nested forms"
       [forms default-name]
       (let [name-position# (macro-name-position forms)
             nested-form-position# (if (= name-position# target-form-not-found) 1 2)]
            {:macro-name-positon   name-position#
             :nested-form-position nested-form-position#
             :macro-info           (drop nested-form-position# forms)
             :macro-name           (if (= name-position# target-form-not-found)
                                     default-name
                                     (macro-name forms))}))
(defn- tabular-fact-config
       "Used by fact and tabular to determine name and postions of next valid nested forms"
       [forms default-name]
       (let [name-position# (macro-name-position (first forms))
             nested-form-position# (if (= name-position# target-form-not-found) 1 2)]
            {:macro-name-positon   name-position#
             :nested-form-position nested-form-position#
             :macro-info           (drop (- nested-form-position# 1) forms)
             :macro-name           (if (= name-position# target-form-not-found)
                                     default-name
                                     (macro-name (first forms)))}))

(defmacro tabular
          [& _]
          (let [;Get Tabular Information
                {tabular-name-position# :macro-name-positon
                 fact-position#         :nested-form-position ; also determine how many forms we need to drop to get to fact template and fact table
                 tabular-name           :macro-name
                 tabular-info           :macro-info} (tabular-config &form no-name-tabular)

                ; Support one layer deep nesting of fact in tabular
                fact          (nth tabular-info 0)

                ;Get Fact Information
                {fact-name-position#     :macro-name-positon
                 fact-template-position# :nested-form-position ; also determine how many forms we need to drop to get to fact template and fact table
                 fact-name               :macro-name
                 fact-table              :macro-info} (tabular-fact-config tabular-info no-name-fact)

                ;Get Fact Table Information
                fact-template  (drop fact-template-position# fact)
                fact-table-values (drop-while symbol? fact-table)
                fact-table-binding (take-while symbol? fact-table)

                _ (println "&form" &form)                                            ; (tabular tabularname (fact factname (+ ?a ?b) => ?c) ?a ?b ?c 1 2 3 3 4 7 9 10 19)
                _ (println "tabular-name-position#" tabular-name-position#)          ; 1
                _ (println "fact-position#" fact-position#)                          ; 2
                _ (println "tabular-name" tabular-name)                              ; tabularname
                _ (println "tabular-info" tabular-info)                              ; ((fact factname (+ ?a ?b) => ?c) ?a ?b ?c 1 2 3 3 4 7 9 10 19)
                _ (println "fact-name-position#" fact-name-position#)                ; 1
                _ (println "fact-template-position#" fact-template-position#)        ; 2
                _ (println "fact" fact)                                              ; (fact factname (+ ?a ?b) => ?c)
                _ (println "fact-name" fact-name)                                    ; factname
                _ (println "fact-template" fact-template)                            ; ((+ ?a ?b) => ?c)
                _ (println "fact-table" fact-table)                                  ; (?a ?b ?c 1 2 3 3 4 7 9 10 19)
                _ (println "fact-table-binding" fact-table-binding)                  ; (?a ?b ?c)
                _ (println "fact-table-values" fact-table-values)                    ; (1 2 3 3 4 7 9 10 19)
                facts (gen-facts fact-template fact-table-binding fact-table-values)
                _ (println "facts" facts)]
               (concat `(fact ~fact-name) (apply concat facts))))

(defmacro fact
  [& _]
    ; TODO: Cover case for fact with no name
    ; TODO: Cover case for nested fact/tabular
  (let [name (macro-name &form)
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

(defn cool []
  (macroexpand
    '(tabular "tabularname"
              (fact "factname"
                    (+ ?a ?b) => ?c)
              ?a ?b ?c
              1 2 3
              3 4 7
              9 10 19)))
(defn cool1 []
  (macroexpand-1
    '(tabular "tabularname"
              (fact "factname"
                    (+ ?a ?b) => ?c)
              ?a ?b ?c
              1 2 3
              3 4 7
              9 10 19)))
