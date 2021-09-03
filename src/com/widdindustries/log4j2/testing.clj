(ns com.widdindustries.log4j2.testing
  (:require [com.widdindustries.log4j2.config :as config]
            [com.widdindustries.log4j2.log-impl :as log-impl]
            [com.widdindustries.log4j2.log-api :as log])
  (:import (org.apache.logging.log4j.core.appender AbstractAppender)
           (org.apache.logging.log4j.core.config Property)
           (org.apache.logging.log4j.core LogEvent)
           (org.apache.logging.log4j.message MapMessage)))

(def appender-name "clj-memory")

(defn logevent->data [^LogEvent event]
  {:mdc         (try (.getContextMap event)
                     (catch Exception _e))
   :logger      (.getLoggerName event)
   :message     (let [message (.getMessage event)]
                  (if (instance? MapMessage message)
                    (.getData ^MapMessage message)
                    message))
   :level       (log/level->kw (.getLevel event))
   :throwable   (.getThrown event)
   :thread-name (.getThreadName event)})

(defn memory-appender [state]
  (proxy [AbstractAppender] [appender-name nil nil true, Property/EMPTY_ARRAY]
    (append [^LogEvent event]
      (state (logevent->data event)))))

(defn setup-recording-context
  "creates & starts a context where log messages are sent to a record-event fn"
  ([] (setup-recording-context
        (let [state (atom [])]
          (->
            (fn [event] (swap! state conj event))
            (with-meta {:state state})))))
  ([record-event]
   (let [builder (config/builder)
         context (-> builder
                     (.add (.newRootLogger builder org.apache.logging.log4j.Level/INFO))
                     (config/start))]
     (config/add-appender-to-running-context
       (memory-appender record-event) context)
     (-> record-event 
         (vary-meta (fn [m] (assoc m :context context)))))))

(defn context-state [record-event]
  (-> record-event meta :state))


(comment
  (config/stop (log-impl/context))
  (def state (-> (setup-recording-context) context-state))
  (log/info {"foo" "bar"})
  @state

  )




