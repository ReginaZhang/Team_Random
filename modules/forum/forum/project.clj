(defproject forum "0.1.0-SNAPSHOT" 
  :description "Forum for Health Overflow"
  :url "https://github.com/ReginaZhang/Team_Random"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [mysql/mysql-connector-java "5.1.25"]
                 [ring/ring-json "0.3.1"]
                 [bidi "1.18.7"]]
  :plugins [[lein-ring "0.9.3"]
            [lein-cljsbuild "1.0.5"]]
  :ring {:handler forum.core/forum
         :auto-reload? true
         :auto-refresh? true}
  :resource-paths ["static/js" "static"]
  :source-paths ["src/clj"]
  :cljsbuild {:builds
              [{:source-paths ["src/cljs/"],
                :id "main",
                :compiler
                {:optimizations :whitespace,
                 :output-to "static/js/cljs.js",
                 :pretty-print true}}]}
  :main ^:skip-aot forum.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
