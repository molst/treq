(defproject treq "0.2-SNAPSHOT"
  :description "Library for converting edn trees of requests into function calls."
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :url "https://github.com/molst/treq"
  :scm {:name "git" :url "https://github.com/molst/treq"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.1.7"]
                 [torpo "0.2-SNAPSHOT"]
                 [shaky "0.2-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]}})