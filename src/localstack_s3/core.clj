(ns localstack-s3.core
  (:require [amazonica.core :refer [defcredential]]
            [amazonica.aws.s3 :as s3]
            [environ.core :refer [env]]
            [taoensso.timbre :as log])
  (:import (java.io ByteArrayInputStream))
  (:gen-class))

(defn write-to-s3 [bucket-name object-key json-string]
  (let [bytes (.getBytes json-string "UTF-8")]
    (s3/put-object :bucket-name bucket-name
                   :key object-key
                   :input-stream (ByteArrayInputStream. bytes)
                   :metadata {:content-length (count bytes)
                              :content-type   "application/json; charset=utf-8"})))

(defn read-from-s3 [bucket-name object-key]
  (-> (s3/get-object bucket-name object-key)
      :input-stream
      slurp))

(defn check-s3-bucket [bucket-name]
  (let [written-json "{\"test\": {\"object\": 1}}"
        object-key "testing.json"
        credentials {:access-key (env :aws-access-key-id)
                     :secret-key (env :aws-secret-access-key)
                     :endpoint   "http://localhost:4572"
                     }]
    (log/info "Checking access to S3 bucket" bucket-name)
    (defcredential credentials)
    (try
      (s3/set-s3client-options :path-style-access true)
      (let [bucket-names (->> (s3/list-buckets)
                              (map :name)
                              set)]
        (when-not (contains? bucket-names bucket-name)
          (s3/create-bucket bucket-name)))
      (write-to-s3 bucket-name object-key written-json)
      (let [read-json (read-from-s3 bucket-name object-key)]
        (assert (= read-json written-json)
                (format "S3 object read from bucket %s at key %s: '%s', not same as written: '%s'"
                        bucket-name object-key read-json written-json)))
      true
      (catch Exception e
        (log/error e "Problem accessing S3 bucket" bucket-name)
        false))))

(defn -main
  [& args]
  (when-not (seq args)
    (println "Error: Requires a bucket name as an argument")
    (System/exit 1))

  (let [bucket-name (first args)]
    (when-not (check-s3-bucket bucket-name)
      (System/exit 1))
    (println "All is good")))
