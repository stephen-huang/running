(ns running.mq.receive
  (:use running.mq.rabbitmq running.mq.config)
  (:import (com.rabbitmq.client QueueingConsumer))
  (:import (java.util.UUID)))

(defn delivery-from [channel consumer]
  (let [delivery (.nextDelivery consumer)]
    (.basicAck channel (.. delivery getEnvelope getDeliveryTag) false)
    (String. (.getBody delivery))))

(defn consumer-for
  ([channel queue-name]
    (consumer-for channel DEFAULT-EXCHANGE-NAME DEFAULT-EXCHANGE-TYPE queue-name queue-name))
  ([channel exchange-name exchange-type queue-name routing-key]
    (let [consumer (QueueingConsumer. channel)]
      (.exchangeDeclare channel exchange-name exchange-type)
      (.queueDeclare channel queue-name false false false nil)
      (.queueBind channel queue-name exchange-name routing-key)
      (.basicConsume channel queue-name consumer)
      consumer)))

(defn random-queue-name []
  (str (java.util.UUID/randomUUID)))

(defn next-message-from
  ([queue-name]
    (next-message-from DEFAULT-EXCHANGE-NAME DEFAULT-EXCHANGE-TYPE queue-name queue-name))
  ([exchange-name exchange-type routing-key]
    (next-message-from exchange-name exchange-type (random-queue-name) routing-key))
  ([exchange-name exchange-type queue-name routing-key]
    (with-open [channel (.createChannel *rabbit-connection* )]
      (let [consumer (consumer-for channel exchange-name exchange-type queue-name routing-key)]
        (delivery-from channel consumer)))))

(defn- lazy-message-seq [channel consumer]
  (lazy-seq
    (let [message (delivery-from channel consumer)]
      (cons message (lazy-message-seq channel consumer)))))

(defn message-seq
  ([queue-name]
    (message-seq DEFAULT-EXCHANGE-NAME DEFAULT-EXCHANGE-TYPE queue-name queue-name))
  ([exchange-name exchange-type routing-key]
    (message-seq exchange-name exchange-type (random-queue-name ) routing-key))
  ([exchange-name exchange-type queue-name routing-key]
    (println "waiting for message...")
    (let [channel (.createChannel *rabbit-connection* )
        consumer (consumer-for channel exchange-name exchange-type queue-name routing-key)]
      (lazy-message-seq channel consumer))))





