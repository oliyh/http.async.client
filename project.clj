(defproject http.async.client "0.6.0-SNAPSHOT"
  :name             "http.async.client"
  :description      "Asynchronous HTTP Client for Clojure"
  :url              "http://neotyk.github.com/http.async.client/"
  :source-paths ["src/clj"]
  :java-source-paths ["src/jvm"]
  :dependencies     [[org.clojure/clojure "1.5.1"]
                     [com.ning/async-http-client "1.8.4"]]
  :min-lein-version "2.0.0"
  :plugins [[codox "0.6.1"]
            [lein-difftest "1.3.3"
             :exclusions [org.clojure/clojure org.clojure/clojure-contrib]]]
  :profiles {:dev
             {:resource-paths ["test-resources"],
              :dependencies
              [[org.eclipse.jetty/jetty-server "7.1.4.v20100610"]
               [org.eclipse.jetty/jetty-security "7.1.4.v20100610"]
               [http-kit "2.1.16"]
               [log4j "1.2.13"]
               [org.slf4j/slf4j-log4j12 "1.6.4"]]}}
  ;; :repositories {"snapshots" "http://oss.sonatype.org/content/repositories/snapshots/"}
  :codox {:output-dir "doc"}
  :autodoc {:web-src-dir "http://github.com/neotyk/http.async.client/blob/"
            :web-home "http://neotyk.github.com/http.async.client/autodoc/"
            :copyright "Copyright 2012 Hubert Iwaniuk"}
  :licence {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo})
