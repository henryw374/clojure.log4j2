# clojure.log4j2

When clojure.tools.logging is not enough

# Goals

* use Java logging lib, so JUL,JCL,log4j etc etc all get logged with the same config
* log data. and optionally strings
* avoid varargs/positional-args confusion in API with Throwable etc
* bit of help with programmatic config, as alternative to xml files

& more a 'nice to have' - but I don't want to have to move 
away from clojure.tools.logging overnight

# Rationale

'Log data, not strings' is a thing, but of all the Java Logging frameworks, [log4j2](https://logging.apache.org/log4j/2.x/) is the 
only one that actually does that. In all logging frameworks you can format messages as
json or something and get `{level: "INFO", message "bar"}`, but what is different with log4j2
is that you can log arbitrary data and have that data passed as-is to appenders. So the console
appender might format that data as JSON, but the mongo appender will persist a mongo object 
created directly from the data, not a string representation of it.

[clojure.tool.logging](https://github.com/clojure/tools.logging) can route log statements
through many java logging systems including log4j2. However, the args to log
functions are stringified before calling the underlying impl so it is not suitable for logging data.

The same limitation also exists in pedestal.log

# Usage

## Logging 

```clojure
(ns my.ns
  (:require [com.widdindustries.log4j2.log-api :as log])
  (:import [org.apache.logging.log4j.message MapMessage]))

;log string
(log/info "hello")

;log a Message - this is how you 'log data'
(log/info (MapMessage. {"foo" "bar"}))

; varargs formatted string
(log/info "hello {} there" :foo)

; builder - include throwable|marker|location
(-> (log/info-builder)
    (log/with-location)
    (log/with-throwable *e)
    ; finally log string or Message etc
    (log/log "foo"))
```

## Config

This is optional. You can configure log4j2 with xml etc as you like, but if you prefer
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

# References

* https://clojureverse.org/t/how-do-you-personally-do-logging/4299
* https://www.reddit.com/r/Clojure/comments/5deqpt/the_ultimate_guide_on_modern_logging/
* https://lambdaisland.com/blog/2020-06-12-logging-in-clojure-making-sense-of-the-mess


Copyright © 2021 [Widd Industries](https://widdindustries.com/about/)

