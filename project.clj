(defn ver [] (-> "smidje.version" slurp .trim))

(defproject smidje (ver)
  :description "A little library for cljs testing and mocking inspired by Midje"
  :url "https://github.com/munk/smidje"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]

  :profiles {:dev {:dependencies [[midje "1.8.3" :exclusions [org.clojure/clojure]]
                                  [org.clojure/tools.nrepl "0.2.12"]
                                  [midje-junit-formatter "0.1.0-SNAPSHOT"]
                                  [org.clojure/clojurescript "1.9.229"]]
                   :plugins      [[lein-doo "0.1.6"]
                                  [lein-cljsbuild "1.1.3"]
                                  [lein-midje "3.2"]]}}

  :cljsbuild {:builds [{:id           "test"
                        :source-paths ["src/smidje" "test/smidje/cljs"]
                        :compiler     {:output-to     "resources/public/js/compiled/test.js"
                                       :main          runner
                                       :optimizations :none
                                       :foreign-libs  [{:provides ["cljsjs.react"]
                                                        :file     "https://cdnjs.cloudflare.com/ajax/libs/react/15.3.2/react-with-addons.js"
                                                        :file-min "https://cdnjs.cloudflare.com/ajax/libs/react/15.3.2/react-with-addons.js"}]}}]})
