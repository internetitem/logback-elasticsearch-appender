Logback Elasticsearch Appender
===============================

[![Build Status](https://travis-ci.org/internetitem/logback-elasticsearch-appender.svg?branch=master)](https://travis-ci.org/internetitem/logback-elasticsearch-appender)

Send log events directly from Logback to Elasticsearch. Logs are delivered asynchronously (i.e. not on the main thread) so will not block execution of the program. Note that the queue backlog can be bounded and messages *can* be lost if Elasticsearch is down and either the backlog queue is full or the producer program is trying to exit (it will retry up to a configured number of attempts, but will not block shutdown of the program beyond that). For long-lived programs, this should not be a problem, as messages should be delivered eventually.

This software is dual-licensed under the EPL 1.0 and LGPL 2.1, which is identical to the [Logback License](http://logback.qos.ch/license.html) itself.

Usage
=====
Include slf4j and logback as usual.

In your `pom.xml` (or equivalent), add:
 
     <dependency>
        <groupId>com.internetitem</groupId>
        <artifactId>logback-elasticsearch-appender</artifactId>
        <version>1.2</version>
     </dependency>

In your `logback.xml`:

        <appender name="ELASTIC" class="com.internetitem.logback.elasticsearch.ElasticsearchAppender">
            <url>http://yourserver/_bulk</url>
            <index>logs-%date{yyyy-MM-dd}</index>
            <type>tester</type>
            <loggerName>es-logger</loggerName> <!-- optional -->
            <errorLoggerName>es-error-logger</errorLoggerName> <!-- optional -->
            <connectTimeout>30000</connectTimeout> <!-- optional (in ms, default 30000) -->
            <errorsToStdErr>false</errorsToStdErr> <!-- optional (default false) -->
            <includeCallerData>false</includeCallerData> <!-- optional (default false) -->
            <logsToStdErr>false</logsToStdErr> <!-- optional (default false) -->
            <maxQueueSize>104857600</maxQueueSize> <!-- optional (default 104857600) -->
            <maxRetries>3</maxRetries> <!-- optional (default 3) -->
            <readTimeout>30000</readTimeout> <!-- optional (in ms, default 30000) -->
            <sleepTime>250</sleepTime> <!-- optional (in ms, default 250) -->
            <rawJsonMessage>false</rawJsonMessage> <!-- optional (default false) -->
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
            <headers>
                <header>
                    <name>Content-Type</name>
                    <value>text/plain</value>
                </header>
            </headers>
        </appender>
        
        <root level="info">
            <appender-ref ref="FILELOGGER" />
            <appender-ref ref="ELASTIC" />
        </root>
    
        <logger name="es-error-logger" level="INFO" additivity="false">
            <appender-ref ref="FILELOGGER" />
        </logger>
    
        <logger name="es-logger" level="INFO" additivity="false">
            <appender name="ES_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
                <!-- ... -->
                <encoder>
                    <pattern>%msg</pattern> <!-- This pattern is important, otherwise it won't be the raw Elasticsearch format anyomre -->
                </encoder>
            </appender>
        </logger>



Configuration Reference
=======================

 * `url` (required): The URL to your Elasticsearch bulk API endpoint
 * `index` (required): Name if the index to publish to (populated using PatternLayout just like individual properties - see below)
 * `type` (optional): Elasticsearch `_type` field for records
 * `sleepTime` (optional, default 250): Time (in ms) to sleep between attempts at delivering a message
 * `maxRetries` (optional, default 3): Number of times to attempt retrying a message on failure. Note that subsequent log messages reset the retry count to 0. This value is important if your program is about to exit (i.e. it is not producing any more log lines) but is unable to deliver some messages to ES
 * `connectTimeout` (optional, default 30000): Elasticsearch connect timeout (in ms)
 * `readTimeout` (optional, default 30000): Elasticsearch read timeout (in ms)
 * `includeCallerData` (optional, default false): If set to `true`, save the caller data (identical to the [AsyncAppender's includeCallerData](http://logback.qos.ch/manual/appenders.html#asyncIncludeCallerData))
 * `errorsToStderr` (optional, default false): If set to `true`, any errors in communicating with Elasticsearch will also be dumped to stderr (normally they are only reported to the internal Logback Status system, in order to prevent a feedback loop)
 * `logsToStderr` (optional, default false): If set to `true`, dump the raw Elasticsearch messages to stderr
 * `maxQueueSize` (optional, default 104,857,600 = 200MB): Maximum size (in characters) of the send buffer. After this point, *logs will be dropped*. This should only happen if Elasticsearch is down, but this is a self-protection mechanism to ensure that the logging system doesn't cause the main process to run out of memory. Note that this maximum is approximate; once the maximum is hit, no new logs will be accepted until it shrinks, but any logs already accepted to be processed will still be added to the buffer
 * `loggerName` (optional): If set, raw ES-formatted log data will be sent to this logger
 * `errorLoggerName` (optional): If set, any internal errors or problems will be logged to this logger
 * `rawJsonMessage` (optional, default false): If set to `true`, the log message is interpreted as pre-formatted raw JSON message. 

The fields `@timestamp` and `message` are always sent and can not currently be configured. Additional fields can be sent by adding `<property>` elements to the `<properties>` set.

 * `name` (required): Key to be used in the log event
 * `value` (required): Text string to be sent. Internally, the value is populated using a Logback PatternLayout, so all [Conversion Words](http://logback.qos.ch/manual/layouts.html#conversionWord) can be used (in addition to the standard static variable interpolations like `${HOSTNAME}`).
 * `allowEmpty` (optional, default `false`): Normally, if the `value` results in a `null` or empty string, the field will not be sent. If `allowEmpty` is set to `true` then the field will be sent regardless

Logback Access
==============

Included is also an Elasticsearch appender for Logback Access. The configuration is almost identical, with the following two differences:

 * The Appender class name is `com.internetitem.logback.elasticsearch.ElasticsearchAccessAppender`
 * The `value` for each `property` uses the [Logback Access conversion words](http://logback.qos.ch/manual/layouts.html#logback-access).

