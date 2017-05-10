# localstack-s3

Illustrates a mismatch between Localstack S3 and Amazon S3.

## Prerequisites

* Java
* [Leiningen](https://leiningen.org/#install)

## Usage

Run using Leiningen:

    $ AWS_ACCESS_KEY=x AWS_SECRET_ACCESS_KEY=x \
    lein run somebucket

Build jar-file:

    $ lein uberjar

Run jar-file:

    $ AWS_ACCESS_KEY=x AWS_SECRET_ACCESS_KEY=x \
    java -jar target/localstack-s3-0.1.0-SNAPSHOT-standalone.jar somebucket

## Examples

### Testing against Localstack S3

When running against Localstack S3, it results in the following exception:

```
$ AWS_ACCESS_KEY_ID=x AWS_SECRET_ACCESS_KEY=x lein run somebucket
17-05-10 11:22:52 ulsamac.local INFO [localstack-s3.core:32] - Checking access to S3 bucket somebucket
17-05-10 11:22:56 ulsamac.local ERROR [localstack-s3.core:49] - Problem accessing S3 bucket somebucket
...
com.amazonaws.services.s3.AmazonS3Client.putObject  AmazonS3Client.java: 1728

com.amazonaws.SdkClientException: Unable to verify integrity of data upload.
Client calculated content hash (contentMD5: 90TFvSlO7zvr2gTw1+tYFw== in base 64)
didn't match hash (etag: 0b3e0734ee13fa0b17f9a9348942c9da in hex) calculated by
Amazon S3.  You may need to delete the data stored in Amazon S3.
(metadata.contentMD5: null, md5DigestStream: com.amazonaws.services.s3.internal.MD5DigestCalculatingInputStream@57f0bfc3,
bucketName: somebucket, key: testing.json)
```

Indeed, looking at the object stored on Localstack S3, we see some extra information:

```
$ aws --endpoint-url http://localhost:4572 s3 cp s3://somebucket/testing.json /tmp/
download: s3://somebucket/testing.json to ../../../../../../tmp/testing.json

$ cat /tmp/testing.json
17;chunk-signature=4496e1378818b2a425c8cffc0a0de8eb1c5be53cf2e45b6f1a8bcd68ba8ec33c
{"test": {"object": 1}}
0;chunk-signature=8a48aa93521b20d2ce453fa4d7936e68afccc5a9a28b6d43ba7c794eee180e07

```

### Testing against Amazon S3

By commenting out (using `;`) the `:endpoint` key in the credentials map, and providing 
working AWS credentials and a valid bucket name on the command line, the 
same code can be run against Amazon S3:

```clojure
(defn check-s3-bucket [bucket-name]
  (let [written-json "{\"test\": {\"object\": 1}}"
        object-key "testing.json"
        credentials {:access-key (env :aws-access-key-id)
                     :secret-key (env :aws-secret-access-key)

                     ; commented out localstack below, ie using Amazon
                     ; :endpoint   "http://localhost:4572"
                     }]
```

Providing valid AWS credentials as environment variables:

```
$ AWS_ACCESS_KEY=AKIAI... AWS_SECRET_ACCESS_KEY='9kv8r...' lein run somebucketthatisnottaken
17-05-10 11:33:24 ulsamac.local INFO [localstack-s3.core:32] - Checking access to S3 bucket somebucketthatisnottaken
All is good
```

Looking at the object stored, we see that it's correct:

```
$ AWS_ACCESS_KEY=AKIAI... AWS_SECRET_ACCESS_KEY='9kv8r...' aws s3 cp s3://somebucketthatisnottaken/testing.json /tmp/testing-from-aws.json
download: s3://somebucketthatisnottaken/testing.json to ../../../../../../tmp/testing-from-aws.json

$ cat /tmp/testing-from-aws.json
{"test": {"object": 1}}
```
