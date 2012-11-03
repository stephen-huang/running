(ns running.mq.receive
  (:use running.mq.rabbitmq)
  (:import (com.rabbitmq.client QueueingConsumer)))

(defn delivery-from [channel consumer]
  (let [delivery (.nextDelivery consumer)]
    (.basicAck channel (.. delivery getEnvelope getDeliveryTag) false)
    (String. (.getBody delivery))))

(defn consumer-for [channel queue-name]
  (let [consumer (QueueingConsumer. channel)]
    (.queueDeclare channel queue-name false false false nil)
    (.basicConsume channel queue-name consumer)
    consumer))

;(defn next-message-from [queue-name]
;  (with-open [channel (.createChannel *rabbit-connection* )]
;    (let [consumer (consumer-for channel queue-name)]
;      (delivery-from channel consumer))))

(defn- lazy-message-seq [channel consumer]
  (lazy-seq
    (let [message (delivery-from channel consumer)]
      (cons message (lazy-message-seq channel consumer)))))

(defn message-seq [queue-name]
  (println "waiting for message...")
  (println *rabbit-connection* )
  (let [channel (.createChannel *rabbit-connection* )
        consumer (consumer-for channel queue-name)]
    (lazy-message-seq channel consumer)))





