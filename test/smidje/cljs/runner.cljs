(ns runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [macro-test]
              [second]))

(doo-tests 'macro-test
           'second)
