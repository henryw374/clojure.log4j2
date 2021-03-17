(ns com.widdindustries.log4j2.config
  "some snippets for setting up logging programmatically. 
  optional to use, and not meant to be comprehensive"
  (:require [com.widdindustries.log4j2.log-impl :as log-impl])
  (:import [org.apache.logging.log4j LogManager Logger Level]
           (org.apache.logging.log4j.core.config.builder.api ConfigurationBuilderFactory ConfigurationBuilder)
           (org.apache.logging.log4j.core.appender ConsoleAppender ConsoleAppender$Target)
           (org.apache.logging.log4j.core.config Configurator)
           (org.apache.logging.log4j.core LoggerContext Appender)))

(defn builder [& [config-name status-level]]
  (-> (ConfigurationBuilderFactory/newConfigurationBuilder)
      (.setStatusLevel (or status-level Level/WARN))
      (.setConfigurationName (or config-name "CljConfig"))))

(defn start ^LoggerContext [^ConfigurationBuilder builder]
  (Configurator/initialize (ClassLoader/getSystemClassLoader) (.build builder) nil))

(defn stop [^LoggerContext context]
  (Configurator/shutdown context))

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
    (.addAttribute "additivity", false)))

(defn get-appenders 
  ([] (get-appenders (log-impl/context)))
  ([^LoggerContext context]
   (let [config (-> context
                    (.getConfiguration))
         logger (-> config (.getRootLogger))]
     (-> logger
         (.getAppenders)))))

(defn remove-all-appenders
  ([] (remove-all-appenders (log-impl/context)))
  ([^LoggerContext context]
   (doseq [[n _] (get-appenders context)]
     (println "removing.." n)
     (.removeAppender logger n))))

(defn get-loggers [context]
  (-> (vec (.getLoggers context))
      (conj (.getRootLogger context))))

(defn add-appender-to-running-context
  ([appender] (add-appender-to-running-context appender (log-impl/context)))
  ([^Appender appender ^LoggerContext context]
   (do
     (.start appender)
     (-> (.getConfiguration context)
         (.addAppender appender))
     (doseq [logger (get-loggers context)]
       (.addAppender logger (-> (.getConfiguration context)
                           (.getAppender (.getName appender)))))
     (.updateLoggers context))))

(defn context->data
  ([] (context->data (log-impl/context)))
  ([context]
   (->> (get-loggers context)
        (map (fn [l]
               [(.getName l) (.getAppenders l)]))
        (into {}))))