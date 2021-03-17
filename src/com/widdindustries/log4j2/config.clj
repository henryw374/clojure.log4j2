(ns com.widdindustries.log4j2.config
  "some snippets for setting up logging programmatically. 
  optional to use, and not meant to be comprehensive"
  (:require [com.widdindustries.log4j2.log-impl :as log-impl])
  (:import [org.apache.logging.log4j LogManager Logger Level]
           (org.apache.logging.log4j.core.config.builder.api ConfigurationBuilderFactory ConfigurationBuilder)
           (org.apache.logging.log4j.core.appender ConsoleAppender ConsoleAppender$Target)
           (org.apache.logging.log4j.core.config Configurator)
           (org.apache.logging.log4j.core LoggerContext Appender)
           [org.apache.logging.log4j.status StatusLogger]))

(def status-logger (StatusLogger/getLogger))

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

(defn get-loggers
  ([] (get-loggers (log-impl/context)))
  ([context]
   (->> (.getLoggers (.getConfiguration context))
        keys
        (map (fn [logger-name]
               (.getLogger context logger-name))))))

(comment 
  (.getLoggers (log-impl/context))
  (get-loggers)
  )

(defn get-appenders 
  ([] (get-appenders (log-impl/context)))
  ([^LoggerContext context]
   (->> (get-loggers context)
        (mapcat (fn [l] (.getAppenders l))))))

(defn remove-all-appenders
  ([] (remove-all-appenders (log-impl/context)))
  ([^LoggerContext context]
   (doseq [[n _] (get-appenders context)]
     (.info status-logger (str "removing.." n))
     (.removeAppender ^Logger logger ^String n))))

(defn add-appender-to-running-context
  ([appender] (add-appender-to-running-context appender (log-impl/context)))
  ([^Appender appender ^LoggerContext context]
   (.start appender)
   (-> (.getConfiguration context)
       (.addAppender appender))
   (let [appender-from-ctx (-> (.getConfiguration context)
                               (.getAppender (.getName appender)))]
     (doseq [logger (get-loggers context)]
       (.info status-logger (str "adding appender to " (.getName logger)))
       (.addAppender logger appender-from-ctx)))
   (.updateLoggers context)))


(defn context->data
  ([] (context->data (log-impl/context)))
  ([context]
   (->> (get-loggers context)
        (map (fn [l]
               [(.getName l) (.getAppenders l)]))
        (into {}))))