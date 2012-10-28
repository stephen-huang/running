(ns running.mq.receive
  (:use running.mq.rabbitmq)
  ;(:use clojure.contrib str-utils)
  ;(use '[clojure.contrib.str-utils :only (str-join)])
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

(defn next-message-from [queue-name]
  (with-open [channel (.createChannel *rabbit-connection* )]
    (let [consumer (consumer-for channel queue-name)]
      (delivery-from channel consumer))))

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

(defn receive-mutilple [queue-name handler]
  (doseq [message (message-seq queue-name)]
    (handler message)))

(defn- print-two-messages [messages]
  (println (clojure.string/join "::" messages)))

;receive messages one by one
(defn -main1 []
  (with-rabbit ["localhost"]
    ;(next-message-from "hello")
    (println (receive-mutilple "hello" println))))

;receive messages in pair
(defn -main []
  (with-rabbit ["localhost"]
    (let [message-pairs (partition 2 (message-seq "hello"))]
      (doseq [message-pair message-pairs]
        (print-two-messages message-pair)))))