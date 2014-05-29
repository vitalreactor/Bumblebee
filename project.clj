(defproject com.vitalreactory/bumblebee "0.1.0-SNAPSHOT"
  :description "Tools for applying time-series based queries to API results"
  :url "http://github.com/vitalreactor/bumblebee"
  :license {:name "MIT License"
            :url ""}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.jayway.jsonpath/json-path "0.9.1"]
                 [org.clojure/data.json "0.2.4"]
                 [javax.xml.xquery/xqj-api "1.0"]
                 [io.zorba/zorba-xqj "3.0.0"]
                 [io.zorba/zorba-api "3.0.0"]]
  :repositories [["xqj.net" "http://xqj.net/maven"]]
  :aliases {"test" "midje"}
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.1"]]}}
  :jvm-opts ["-Djava.library.path=/opt/local/share/java/"])
