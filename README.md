# clojure.log4j2

When clojure.tools.logging is not enough


[![Clojars Project](https://img.shields.io/clojars/v/com.widdindustries/clojure.log4j2.svg)](https://clojars.org/com.widdindustries/clojure.log4j2)

# Features

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

The same limitation also exists in pedestal.log

## But what about...

### Timbre

I haven't used that personally. It might be great, others seem less keen - see Clojureverse thread below for example.

### Mulog

Feature-wise that seems to be the same as log4j2, so to consider that I'd be looking for some
compelling reasons to move away from what I consider the safe choice of log4j2.

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

;clojure maps wrapped in MapMessage object
(log/info {"foo" "bar"})

; varargs for formatted string only
(log/info "hello {} there" :foo)

; builder - include throwable|marker|location
(-> (log/info-builder)
    (log/with-location)
    (log/with-throwable *e)
    ; finally log string or Message etc
    (log/log {"foo" "bar"}))
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

