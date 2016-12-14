(defproject cljs-proof "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]

  :profiles {:dev {:dependencies [[midje "1.8.3"]
                                  [org.clojure/clojurescript "1.9.229"]]
                   :plugins      [[lein-doo "0.1.6"]
                                  [lein-cljsbuild "1.1.3"]]}}

  :cljsbuild {:builds [{:id           "test"
                        :source-paths ["src/smidje" "test/smidje/cljs"]
                        :compiler     {:output-to     "resources/public/js/compiled/test.js"
                                       :main          smidje.cljs.runner
                                       :optimizations :none
                                       :foreign-libs  [{:provides ["cljsjs.react"]
                                                        :file     "https://cdnjs.cloudflare.com/ajax/libs/react/15.3.2/react-with-addons.js"
                                                        :file-min "https://cdnjs.cloudflare.com/ajax/libs/react/15.3.2/react-with-addons.js"}]}}]})