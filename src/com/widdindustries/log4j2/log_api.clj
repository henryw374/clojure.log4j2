(ns com.widdindustries.log4j2.log-api
  (:require [com.widdindustries.log4j2.log-impl :as impl]
            [clojure.string :as str])
  (:import [org.apache.logging.log4j.message Message MapMessage]
           [org.apache.logging.log4j LogBuilder Level Marker LogManager]
           [org.apache.logging.log4j.util Supplier]
           (java.util Map)))

(set! *warn-on-reflection* true)

(defmacro fatal-builder [] `(impl/log-builder Level/FATAL))
(defmacro error-builder [] `(impl/log-builder Level/ERROR))
(defmacro warn-builder [] `(impl/log-builder Level/WARN))
(defmacro info-builder [] `(impl/log-builder Level/INFO))
(defmacro debug-builder [] `(impl/log-builder Level/DEBUG))
(defmacro trace-builder [] `(impl/log-builder Level/TRACE))

(defn create-map-message [^Map m]
  (let [message (MapMessage. (count m))]
    (doto message
      (.putAll
        ; by creating an intermediate map, this is more wasteful than necessary
        ; but needed until https://github.com/apache/logging-log4j2/pull/477 is addressed
        (into
          {}
          (map (fn [[k v]]
                 [(name k) v]))
          m)))))

(defn log
  ([^LogBuilder builder thing]
   (cond
     (instance? String thing) (.log builder ^String thing)
     (instance? Message thing) (.log builder ^Message thing)
     (instance? Map thing) (.log builder ^Message (create-map-message ^Map thing))
     (instance? Supplier thing) (.log builder ^Supplier thing)
     :else (.log builder ^Object thing)))
  ([^LogBuilder builder ^String thing varargs]
   (if (instance? Supplier (first varargs))
     (.log builder ^String thing #^"[Lorg.apache.logging.log4j.util.Supplier;" (into-array Supplier varargs))
     (.log builder ^String thing #^"[Ljava.lang.Object;" (into-array Object varargs)))))

(defn with-location [^LogBuilder builder]
  (.withLocation builder))

(defn with-marker [^LogBuilder builder ^Marker marker]
  (.withMarker builder marker))

(defn with-throwable [^LogBuilder builder ^Throwable throwable]
  (.withThrowable builder throwable))

(defmacro fatal
  ([thing] `(log (impl/log-builder Level/FATAL) ~thing))
  ([thing & more] `(log (impl/log-builder Level/FATAL) ~thing [~@more])))

(defmacro error
  ([thing] `(log (impl/log-builder Level/ERROR) ~thing))
  ([thing & more] `(log (impl/log-builder Level/ERROR) ~thing [~@more])))

(defmacro warn
  ([thing] `(log (impl/log-builder Level/WARN) ~thing))
  ([thing & more] `(log (impl/log-builder Level/WARN) ~thing [~@more])))

(defmacro info
  ([thing] `(log (impl/log-builder Level/INFO) ~thing))
  ([thing & more] `(log (impl/log-builder Level/INFO) ~thing [~@more])))

(defmacro debug
  ([thing] `(log (impl/log-builder Level/DEBUG) ~thing))
  ([thing & more] `(log (impl/log-builder Level/DEBUG) ~thing [~@more])))

(defmacro trace
  ([thing] `(log (impl/log-builder Level/TRACE) ~thing))
  ([thing & more] `(log (impl/log-builder Level/TRACE) ~thing [~@more])))

(defn set-level
  "FYI the root logger name is the empty string. or you can refer to it via LogManager/ROOT_LOGGER_NAME"
  [logger-name level]
  (let [ctx ^org.apache.logging.log4j.core.LoggerContext (impl/context)]
    (-> ctx
        (.getConfiguration)
        (.getLoggerConfig (str logger-name))
        (.setLevel (Level/valueOf (str/upper-case (name level)))))
    (.updateLoggers ctx)))