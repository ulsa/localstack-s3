(defproject localstack-s3 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [amazonica "0.3.95"
                  :exclusions [com.amazonaws/aws-java-sdk
                               com.amazonaws/amazon-kinesis-client]]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.124"]
                 [commons-logging "1.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [environ "1.1.0"]]
  :main ^:skip-aot localstack-s3.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
