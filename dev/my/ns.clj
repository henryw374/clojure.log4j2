(ns my.ns
  (:require [com.widdindustries.log4j2.log-api :as log])
  (:import [org.apache.logging.log4j.message MapMessage]))

;log string
(log/info "hello")

;log a Message (data)
(log/info (MapMessage. {"foo" "bar"}))

; varargs formatted string
(log/info "hello {} there" :foo)

; builder - include throwable|marker|location
(-> (log/info-builder)
    (log/with-location)
    (log/with-throwable *e)
    (log/log "foo"))

(ns my.ns
  (:require [com.widdindustries.log4j2.config :as config]))

; the equivalent of having magic xml file on classpath
(let [builder (config/builder)
      std-out-appender-name "Stdout"]
  (-> builder
      (.add (config/std-out-appender builder std-out-appender-name
              "%date %level %logger %message%n%throwable"))
      (.add (config/root-logger builder org.apache.logging.log4j.Level/INFO std-out-appender-name))
      (.add (config/logger builder Level/DEBUG std-out-appender-name "my.ns"))
      ;(.writeXmlConfiguration  System/out)
      (config/start)))
