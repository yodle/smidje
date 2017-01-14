 (ns second
   (:require [smidje.core-test :refer-macros [fact tabular]]
             [cljs.test :refer-macros [deftest testing is]]
             [smidje.cljs-generator.test-builder]))

(defmethod cljs.test/report [::default :smidje-fail] [m]
  (println m)
  (cljs.test/inc-report-counter! :pass))

(fact "othertest"
                              (+ 1 1) => 2)
