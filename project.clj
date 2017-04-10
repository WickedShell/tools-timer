(defproject tools-timer "1.0.3"
  :description "An easy to use Java Timer wrapper for clojure."
  :lein-release {:deploy-via :clojars}
  :min-lein-version "2.0.0"
  :url "http://github.com/wickedshell/tools-timer"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.5.1"]]
                   :plugins [[codox "0.6.4"]]}}
  :global-vars {*warn-on-reflection* true})
