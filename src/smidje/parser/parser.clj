(ns smidje.parser.parser
  (:require [smidje.parser.arrows :refer [arrow-set]]
            [clojure.walk :refer [prewalk]]
            [smidje.parser.checkers :refer [throws truthy TRUTHY falsey FALSEY]]))

(def provided "provided")

(defn- is-arrow
  [form]
  (or (= form '=>)
      (= form '=not=>)))

(defn- ^{:testable true} provided-form?
  [form]
  (and (seq? form)
       (= (first form) 'provided)))

(defn- has-provided-form? [input]
  (and (> (count input) 3)
       (provided-form? (nth input 3))))

(defn- gen-provided-sym
  [fn1 fn2]
  (symbol (str ".." (gensym (str fn1 "->" fn2)) ".." )))

(defn- unnest-provided
  [provided]
  (let [match (first provided)]
    (if (some list? match)
      (let [provided-fn (first match)]
        (loop [provided-flattened [provided-fn] flattened-paramaters [] r (rest match)]
          (cond
            (empty? r) (conj flattened-paramaters (into []  (conj (rest provided ) (flatten  provided-flattened))))

            (and (list? (first r))
                 (not-empty (first r)))
               (let [sub-provide (first r)
                     sub-fn (first sub-provide)
                     metaconst (gen-provided-sym provided-fn sub-fn)]
                 (recur (conj provided-flattened metaconst) (into flattened-paramaters (unnest-provided [sub-provide '=> metaconst])) (rest r)))

            :else (recur (conj provided-flattened (first r)) flattened-paramaters (rest r) )
            )))
      [provided]
      )))

(defn- separate-provided-forms
  [forms]
  (loop [result [] current-form (into [] (take 3 forms)) input (drop 3 forms)]
    (cond
      (empty? input) (conj result current-form)

      (and (> (count input) 2)
           (list? (first input))
           (is-arrow (second input)))
        (recur (conj result current-form) (into []  (take 3 input)) (drop 3 input))

      :else (recur result (conj current-form (first input)) (rest input)))))

(defn- replace-range
  [form]
  (let [[_ x1 x2] form]
    (condp = (count form)
      1 :optional
      2 (if (< x1 1)
          (throw (RuntimeException. (str "invalid (range) argument: " x1 ", must be greater than 0")))
          {:range [1 x1]})
      3 (cond
          (< x1 1) (throw (RuntimeException. (str "invalid (range) argument: " x1 " must be greater than 0")))
          (< x2 1) (throw (RuntimeException. (str "invalid (range) argument: " x2 " must be greater than 0")))
          (> x1 x2) (throw (RuntimeException. (str "invalid (range) argument: " x1 " must be less than or equal to " x2)))
          :else {:range [x1 x2]})
      (throw (RuntimeException. "invalid (range) form: more than two arguments")))))

(defn- ^{:testable true} adjust-range
  [form]
  (loop [result [] input form]
    (cond
      (empty? input) result
      (and (= (first input) :times)
           (> (count input) 1)
           (list? (second input))
           (= (first (second input)) 'range))
        (recur
          (conj result (first input) (replace-range (second input)))
          (drop 2 input))
      :else (recur
              (conj result (first input))
              (rest input)))))

(defn- aggregate-paramater-maps
  [paramater-maps]
  (apply hash-map (mapcat
                   (fn [x] [(:paramaters x)
                            (dissoc x :paramaters :mock-function)])
                   paramater-maps)))

(defn- build-provided-map
  [provided]
  (merge
   (apply hash-map (drop 3 provided))
   {:mock-function (first (first provided))
    :paramaters (into [] (rest (first provided)))
    :arrow (keyword (second provided))
    :result (nth provided 2)}))

(defn- parse-provided
  [forms]
  (if (has-provided-form? forms)
    {:provided (->> (nth forms 3)
                    rest
                    separate-provided-forms
                    (map adjust-range)
                    (mapcat unnest-provided)
                    (map build-provided-map)
                    (group-by :mock-function)
                    (map (fn [x]
                           {:mock-function (first x)
                            :return (aggregate-paramater-maps (second x))})))}
    {}))

(defn- truth-testing-form? [input]
  (and (not (seq? input))
       (boolean (some #(= input %) ['truthy 'TRUTHY 'falsey 'FALSEY]))))

(defn throws-form?
  [form]
  (and (seq? form)
       (= (first form) 'throws)))

(defn- parse-truth-testing
  [form]
  {:truth-testing form})

(defn- parse-throws
  [form]
  (merge
    ; TODO: validate that second argument is an exception type
    ; TODO: validate optional third argument is a string
    {:throws-exception (second form)}
    (when (> (count form) 2)
      {:throws-message (nth form 2)})))

(defn- parse-expected
  [form]
  (cond
    (truth-testing-form? form) (parse-truth-testing form)
    (throws-form? form) (parse-throws form)
    :else {:expected-result form}))

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
      (parse-expected expected-form)
      (parse-provided forms))))

(defn parse-assertions
  [forms]
  (loop [result []
         input forms]
    ; TODO: check for provided mocks
    ; TODO: assertions must be before provided mocks
    ; TODO: error messages on bad syntax
    (if (and (> (count input) 2)
             (is-arrow (second input)))
      (recur
        (conj result (parse-equals input))
        (drop (if (has-provided-form? input) 4 3) input))
      result)))

(defn- ^{:testable true} metaconstant-name? [element-name]
  (or (re-matches #"^\.\..+\.\.$" element-name)
      (re-matches #"^--.+--$" element-name)))

(defn- ^{:testable true} metaconstant? [element]
  (and (symbol? element)
       (metaconstant-name? (name element))))

(defn- pre-parse-metaconstants
  "Returns a list of unique metaconstants symbols in the form.
   The form can be a single element or a variably-nested list."
  [form]
  (->> (vector form)
       (flatten)
       (filter metaconstant?)
       (distinct)))

(defn- gen-metaconstant-sym
  "Generates a unique, bindable symbol for the metaconstant symbol."
  [mc]
  (let [mc-name (name mc)
        mc-length (count mc-name)
        mc-undecorated-name (subs mc-name 2 (- mc-length 2))
        mc-prefix (if (= (subs mc-name 0 2) "..") "dot" "dash")]
    (gensym (str "smidje->mc->" mc-prefix "->" mc-undecorated-name "->"))))

(defn parse-metaconstants
  "Takes a form and returns a map of unique metaconstants in the form mapped to bindable names generated for each."
  [form]
  (let [metaconstants (pre-parse-metaconstants form)
        swapped (map gen-metaconstant-sym metaconstants)]
    (zipmap metaconstants swapped)))

(defn- replace-metaconstant
  [mc-lookup form]
  (if (metaconstant? form)
    (get mc-lookup form)
    form))

(defn- ^{:testable true} replace-metaconstants
  "Takes a metaconstant->symbol look-up map and a variably nested form. Returns the form with metaconstant values
   replaced by the values in the look-up map."
  [mc-lookup form]
  (if-not (seq? form)
    (replace-metaconstant mc-lookup form)
    (clojure.walk/walk (partial replace-metaconstants mc-lookup) identity form)))

(defn- macro-name
       "Get name of target macro in form sequence"
       [form]
       (let [target-form (second form)]
            (if (string? target-form)
              (clojure.string/replace target-form #"[^\w\d]+" "-")
              target-form)))

(defn tabular*
  "Creates tests from a table of example cases

   (tabular \"test-name\"
     (fact \"fact-name\"
       (+ ?a ?b) => ?c)
     ?a ?b ?c
     1  1  2
     2  3  5)"
  [form]
  (let [[macro test-name & table-forms] form]
    (let [[fact & table] table-forms
          [fact-macro fact-name & fact-form] fact
          full-name (str test-name " : " fact-name)
          symbols (take-while symbol? table)
          bindings (->> (drop-while symbol? table)
                        (partition (count symbols)))
          binding-maps (map zipmap (repeat symbols) bindings)
          walk-fns (map (fn [symbol-table]
                          (fn [expr]
                            (if (contains? symbol-table expr)
                              (get symbol-table expr)
                              expr)))
                        binding-maps)
          substituted-facts (map prewalk walk-fns (repeat fact-form))]
      (concat `(smidje.core/fact ~full-name) (apply concat substituted-facts)))))

(defn parse-fact
  [form]
  (let [name (if (string? (second form))
               (clojure.string/replace (second form) #"[^\w\d]+" "-")
               (second form))
        fact-forms (drop 2 form)
        metaconstants (parse-metaconstants fact-forms)
        adjusted-forms (replace-metaconstants metaconstants fact-forms)]
    (-> {:tests [{:name       name
                  :assertions (parse-assertions adjusted-forms)
                  :metaconstants (clojure.set/map-invert metaconstants)}]})))
