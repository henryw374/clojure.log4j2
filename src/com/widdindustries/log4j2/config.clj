(ns com.widdindustries.log4j2.config
  "some snippets for setting up logging programmatically. 
  optional to use, and not meant to be comprehensive"
  (:import [org.apache.logging.log4j LogManager Logger Level]
           (org.apache.logging.log4j.core.config.builder.api ConfigurationBuilderFactory ConfigurationBuilder)
           (org.apache.logging.log4j.core.appender ConsoleAppender ConsoleAppender$Target)
           (org.apache.logging.log4j.core.config Configurator)
           (org.apache.logging.log4j.core LoggerContext)))

(defn builder [& [config-name status-level]]
  (-> (ConfigurationBuilderFactory/newConfigurationBuilder)
      (.setStatusLevel (or status-level Level/WARN))
      (.setConfigurationName (or config-name "CljConfig"))))

(defn start ^LoggerContext [^ConfigurationBuilder builder]
  (Configurator/initialize (ClassLoader/getSystemClassLoader) (.build builder) nil))

(defn std-out-appender [builder appender-name pattern]
  (-> builder
      (.newAppender appender-name, "CONSOLE")
      (.addAttribute "target" ConsoleAppender$Target/SYSTEM_OUT)
      (.add (-> (.newLayout builder "PatternLayout")
                (.addAttribute "pattern", pattern)))))

(defn root-logger [builder level ref]
  (-> (.newRootLogger builder level)
      (.add (.newAppenderRef builder ref))))

(defn logger [builder level ref logger-name]
  (->
    (.newLogger builder logger-name level)
    (.add (.newAppenderRef builder ref))
    ;(.addAttribute "additivity", false)
    ))

(comment
  (let [builder (builder)
        std-out-appender-name "Stdout"]
    (-> builder
        (.add (std-out-appender builder std-out-appender-name
                "%date %level %logger %message%n%throwable"))
        (.add (root-logger builder Level/INFO std-out-appender-name))
        (.add (logger builder Level/INFO std-out-appender-name "org.apache.logging.log4j"))
        (.writeXmlConfiguration  System/out)
        ;(start)
        ))

  (-> (LogManager/getContext true)
      (.getConfiguration)
      (.getLoggers))

  (def logger (LogManager/getLogger "com.widdindustries.config"))
  (.log logger Level/INFO "hello")
  (.log logger Level/ERROR (MapMessage. {"foo" "bar"}))

  )
;builder.add(
;             builder.newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
;             .addAttribute("level", Level.DEBUG));


;appenderBuilder.add(builder.newFilter("MarkerFilter", Filter.Result.DENY, Filter.Result.NEUTRAL)
;                     .addAttribute("marker", "FLOW"));


;builder.add(builder.newLogger("org.apache.logging.log4j", Level.DEBUG)
;             .add(builder.newAppenderRef("Stdout")).addAttribute("additivity", false))




