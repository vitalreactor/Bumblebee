(defproject com.vitalreactory/api_query "0.1.0-SNAPSHOT"
  :description "Tools for applying ad-hoc queries to API results"
  :url "http://github.com/vitalreactor/api_query"
  :license {:name "MIT License"
            :url ""}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.jayway.jsonpath/json-path "0.9.1"]
                 [org.clojure/data.json "0.2.4"]]
  :aliases {"test" "midje"}
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.1"]]}})
