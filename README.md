# clojure.log4j2

sugar for using log4j2 from clojure

[![Clojars Project](https://img.shields.io/clojars/v/com.widdindustries/clojure.log4j2.svg)](https://clojars.org/com.widdindustries/clojure.log4j2)

# Features (of log4j2 mainly)

* legacy Java logging libs work, so JUL,JCL,log4j etc etc all get logged with the same config
* log data (clojure maps & [more](https://logging.apache.org/log4j/2.x/manual/messages.html)). and optionally strings
* use [builder api](https://logging.apache.org/log4j/2.x/manual/logbuilder.html) avoid varargs/positional-args confusion in API with Throwable etc
* bit of help with programmatic config, as alternative to xml files
* clojure.tools.logging still works.. so you don't have to move off it in one go

# Rationale

'Log data, not strings' is a thing, but of all the Java Logging frameworks, [log4j2](https://logging.apache.org/log4j/2.x/) is the 
only one that actually does that. In all logging frameworks you can format messages as
json or something and get `{level: "INFO", message "bar"}`, but what is different with log4j2
is that you can log arbitrary data and have that data passed as-is to appenders, such as 
[nosql appenders](https://logging.apache.org/log4j/2.x/manual/appenders.html#NoSQLAppender). So the console
appender might format that data as JSON, but the mongo appender will persist a mongo object 
created directly from the data, not a string representation of it.

[clojure.tools.logging](https://github.com/clojure/tools.logging) can route log statements
through many java logging systems including log4j2. However, the args to log
functions are stringified before calling the underlying impl so it is not suitable for logging data.

The same limitation (that log events have been stringified before 
reaching appenders) also exists in pedestal.log and [cambium](https://github.com/cambium-clojure)

## But what about...

### Timbre

Feature-wise this seem similar. It's not uncommon to hear reports of people moving off it for various reasons though - see Clojureverse thread below for example.

### Mulog

Feature-wise that seems to be the same as log4j2, so to consider that I'd be looking for some
compelling reasons to move away from what I consider the safe choice (haha!) of log4j2.

# Usage

## Logging 

```clojure
(ns my.ns
  (:require [com.widdindustries.log4j2.log-api :as log])
  (:import [org.apache.logging.log4j.message MapMessage]))

;clojure maps wrapped in MapMessage object - top level keys must be Named (string, keyword, symbol etc)
(log/info {"foo" "bar"})

;log a string
(log/info "hello")

;log a Message - this is how you 'log data'
(log/info (MapMessage. {"foo" "bar"}))

; varargs arity is for formatted string only
(log/info "hello {} there" :foo)

; builder - include throwable|marker|location
(-> (log/info-builder)
    (log/with-location)
    (log/with-throwable *e)
    ; finally log string or Message etc
    (log/log {"foo" "bar"}))

; change log level to trace
(log/set-level 'my.ns :trace)

```
## Config

Configure log4j2 with xml etc as you like, but if you prefer
to do it programmatically, there is a little sugar in this lib that might help a bit.

Configure logger before logging

```clojure
(ns my.ns
  (:require [com.widdindustries.log4j2.config :as config]))

; the equivalent of having magic xml file on classpath
(defn setup-logging []
  (let [builder (config/builder)
        std-out-appender-name "Stdout"]
    (-> builder
        (.add (config/std-out-appender builder std-out-appender-name
                "%date %level %logger %message%n%throwable"))
        (.add (config/root-logger builder org.apache.logging.log4j.Level/INFO std-out-appender-name))
        (.add (config/logger builder Level/DEBUG std-out-appender-name "my.ns"))
        ;(.writeXmlConfiguration  System/out)
        (config/start))))

```

## Testing 

In order to test your log messages 

```clojure 
(ns foo 
(:require [com.widdindustries.log4j2.testing :as testing])
(def state (-> (testing/setup-recording-context) testing/context-state))
(log/info {"foo" "bar"})
@state

=> 

[{:mdc {},
  :logger "com.widdindustries.log4j2.testing",
  :message {"foo" "bar"},
  :level #object[org.apache.logging.log4j.Level 0x40f1e3ff "INFO"],
  :throwable nil,
  :thread-name "nREPL-session-c7822df9-316d-4368-bb06-cdc3e407508e"}]
```

### Plugins

To create your own Appenders & etc Log4j2 allows the creation of [Plugins](https://logging.apache.org/log4j/2.x/manual/plugins.html)
which are annotated Java classes. I'm not sure how those would be possible to create with Clojure, but
the alternative is to use programmatic configuration and pass log4j2 instances of special Appenders
etc created via clojure's `proxy` for example.

## Release

create a git tag.

`make install VERSION=your-tag` (this installs in ~/.m2 - check that things look ok)

`make deploy VERSION=your-tag`  - you need to have set up clojars credentials as per https://github.com/applied-science/deps-library

`git push origin new-tag-name`

# References

* https://clojureverse.org/t/how-do-you-personally-do-logging/4299
* https://www.reddit.com/r/Clojure/comments/5deqpt/the_ultimate_guide_on_modern_logging/
* https://lambdaisland.com/blog/2020-06-12-logging-in-clojure-making-sense-of-the-mess


Copyright © 2021 [Widd Industries](https://widdindustries.com/about/)

