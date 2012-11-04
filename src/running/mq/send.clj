(ns running.mq.send
  (:use running.mq.rabbitmq running.mq.config))

(defn send-message
  ([routing-key message-object]
    (send-message DEFAULT-EXCHANGE-NAME routing-key message-object false))
  ([exchange-name routing-key message-object is-multicast]
    (with-open [channel (.createChannel *rabbit-connection* )]
      (.exchangeDeclare channel exchange-name (if is-multicast "fanout" "direct"))
      ;(.queueDeclare channel routing-key)
      (.queueDeclare channel routing-key false false false nil)
      (.basicPublish channel exchange-name routing-key nil (.getBytes (str message-object))))))


