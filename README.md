Logback Elasticsearch Appender
===============================

Send log events directly from Logback to Elasticsearch. Logs are delivered asynchronously (i.e. not on the main thread) so will not block execution of the program. Note that the queue backlog is unbounded and messages *can* be lost if Elasticsearch is down and the producer program is trying to exit (it will retry up to a configured number of attempts, but will not block shutdown of the program). For long-lived programs, this should not be a problem, as messages should be delivered eventually.

This software is dual-licensed under the EPL 1.0 and LGPL 2.1, which is identical to the [Logback License](http://logback.qos.ch/license.html) itself.

Usage
=====
Include slf4j and logback as usual.

In your logback.xml:

        <appender name="ELASTIC" class="com.internetitem.logback.elasticsearch.ElasticsearchAppender">
            <url>http://yourserver/_bulk</url>
            <index>indexname</index>
            <type>tester</type>
            <debug>false</debug>
            <properties>
                <property>
                    <name>host</name>
                    <value>${HOSTNAME}</value>
                    <allowEmpty>false</allowEmpty>
                </property>
                <property>
                    <name>severity</name>
                    <value>%level</value>
                </property>
                <property>
                    <name>thread</name>
                    <value>%thread</value>
                </property>
                <property>
                    <name>stacktrace</name>
                    <value>%ex</value>
                </property>
                <property>
                    <name>logger</name>
                    <value>%logger</value>
                </property>
            </properties>
        </appender>

Configuration Reference
=======================

 * `url` (required): The URL to your Elasticsearch bulk API endpoint
 * `index` (required): Name if the index to publish to
 * `type` (optional): Elasticsearch `_type` field for records
 * `debug` (optional, default `false`): If set to `true`, the raw JSON is dumped to System.err and *not* send to Elasticsearch
 * `sleepTime` (optional, default 250): Time (in ms) to sleep between attempts at delivering a message
 * `maxRetries` (optional, default 3): Number of times to attempt retrying a message on failure. Note that subsequent log messages reset the retry count to 0. This value is important if your program is about to exit (i.e. it is not producing any more log lines) but is unable to deliver some messages to ES
 * `connectTimeout` (optional, default 30000): Elasticsearch connect timeout (in ms)
 * `readTimeout` (optional, default 30000): Elasticsearch read timeout (in ms)
 * `includeCallerData` (optional, default false): If set to `true`, save the caller data (identical to the [AsyncAppender's includeCallerData](http://logback.qos.ch/manual/appenders.html#asyncIncludeCallerData))
 * `errorsToStderr` (optional, default false): If set to `true`, any errors in communicating with Elasticsearch will also be dumped to stderr
 * `maxQueueSize` (optional, default 104,857,600 = 200MB): Maximum size (in characters) of the send buffer. After this point, *logs will be dropped*. This should only happen if Elasticsearch is down, but this is a self-protection mechanism to ensure that the logging system doesn't cause the main process to run out of memory. Note that this maximum is approximate; once the maximum is hit, no new logs will be accepted until it shrinks, but any logs already accepted to be processed will still be added to the buffer

The fields `@timestamp` and `message` are always sent and can not currently be configured. Additional fields can be sent by adding `<property>` elements to the `<properties>` set.

 * `name` (required): Key to be used in the log event
 * `value` (required): Text string to be sent. Internally, the value is populated using a Logback PatternLayout, so all [Conversion Words](http://logback.qos.ch/manual/layouts.html#conversionWord) can be used (in addition to the standard static variable interpolations like `${HOSTNAME}`).
 * `allowEmpty` (optional, default `false`): Normally, if the `value` results in a `null` or empty string, the field will not be sent. If `allowEmpty` is set to `true` then the field will be sent regardless

 
