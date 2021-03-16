(ns com.widdindustries.log4j2.log-impl
  (:import [org.apache.logging.log4j Logger LogBuilder Level LogManager]
           (org.apache.logging.log4j.spi LoggerContext)))

(set! *warn-on-reflection* true)

(defn log-builder* ^LogBuilder [^Logger logger ^Level level]
  (.atLevel logger level))

(defn context [] (LogManager/getContext false))

(defn get-logger [^LoggerContext context logger-ns]
  (.getLogger context ^String (str logger-ns)))

(defmacro log-builder [^Level level]
  `(let [logger-factory# (context)
         logger# (get-logger logger-factory# (str ~*ns*))]
     (log-builder* logger# ~level)))


