(ns smidje.cljs.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [smidje.cljs.macro-test]))

(doo-tests 'smidje.cljs.macro-test)
