(ns running.mq.rabbitmq
  (:import (com.rabbitmq.client ConnectionFactory)))

(defn new-connection [host]
  (.newConnection
    (doto (ConnectionFactory.)
      (.setHost host)
      )
    ))

(def ^:dynamic *rabbit-connection*)

(defmacro with-rabbit [[mq-host] & exprs]
  `(with-open [connection# (new-connection ~mq-host)]
     (binding [*rabbit-connection* connection#]
       (do ~@exprs))))

