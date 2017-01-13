 (ns second
   (:require [smidje.core-test :refer-macros [fact tabular]]
             [cljs.test :refer-macros [deftest testing is]]
             [smidje.cljs-generator.test-builder]))

(fact "othertest"
                              (+ 1 1) => 2)
