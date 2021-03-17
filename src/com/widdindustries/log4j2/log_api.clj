(ns com.widdindustries.log4j2.log-api
  (:require [com.widdindustries.log4j2.log-impl :as impl])
  (:import [org.apache.logging.log4j.message Message MapMessage]
           [org.apache.logging.log4j Logger LogBuilder Level LogManager Marker]
           [org.apache.logging.log4j.util Supplier]
           (java.util Map)))

(set! *warn-on-reflection* true)

(defmacro fatal-builder [] `(impl/log-builder Level/FATAL))
(defmacro error-builder [] `(impl/log-builder Level/ERROR))
(defmacro warn-builder [] `(impl/log-builder Level/WARN))
(defmacro info-builder [] `(impl/log-builder Level/INFO))
(defmacro debug-builder [] `(impl/log-builder Level/DEBUG))
(defmacro trace-builder [] `(impl/log-builder Level/TRACE))

(defn log
  ([^LogBuilder builder thing]
   (cond
     (instance? String thing) (.log builder ^String thing)
     (instance? Message thing) (.log builder ^Message thing)
     (instance? Map thing) (.log builder ^Message (MapMessage. ^Map thing))
     (instance? Supplier thing) (.log builder ^Supplier thing)

     :else (.log builder ^Object thing)))
  ([^LogBuilder builder ^String thing [x & _xs :as args]]
   (println args)
   (if (instance? Supplier x)
     (.log builder ^String thing #^"[Lorg.apache.logging.log4j.util.Supplier;" (into-array Supplier args))
     (.log builder ^String thing #^"[Ljava.lang.Object;" (into-array Object args)))))

(defn with-location [^LogBuilder builder]
  (.withLocation builder))

(defn with-marker [^LogBuilder builder ^Marker marker]
  (.withMarker builder marker))

(defn with-throwable [^LogBuilder builder ^Throwable throwable]
  (.withThrowable builder throwable))

(defmacro fatal 
  ([thing] `(log (impl/log-builder Level/FATAL) ~thing))
  ([thing & more] `(log (impl/log-builder Level/FATAL) ~thing ~more)))

(defmacro error 
  ([thing] `(log (impl/log-builder Level/ERROR) ~thing))
  ([thing & more] `(log (impl/log-builder Level/ERROR) ~thing ~more)))

(defmacro warn 
  ([thing] `(log (impl/log-builder Level/WARN) ~thing))
  ([thing & more] `(log (impl/log-builder Level/WARN) ~thing ~more)))

(defmacro info 
  ([thing] `(log (impl/log-builder Level/INFO) ~thing))
  ([thing & more] `(log (impl/log-builder Level/INFO) ~thing [~@more])))

(defmacro debug 
  ([thing] `(log (impl/log-builder Level/DEBUG) ~thing))
  ([thing & more] `(log (impl/log-builder Level/DEBUG) ~thing [~@more])))

(defmacro trace 
  ([thing] `(log (impl/log-builder Level/TRACE) ~thing))
  ([thing & more] `(log (impl/log-builder Level/TRACE) ~thing [~@more])))


(comment
   
  (info "hello")
  (info "hello {}" :foo)
  (-> (info-builder)
      (with-location)
      (with-throwable *e)
      (log "foo"))


  (macroexpand '(info "hello {}" :foo))

  )


