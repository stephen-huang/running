(ns running.mq.main-receive
  (:use running.mq.receive running.mq.rabbitmq))


(defn receive-mutilple [queue-name handler]
  (doseq [message (message-seq queue-name)]
    (handler message)))

;receive messages one by one
(defn -main []
  (with-rabbit ["localhost"]
    ;(next-message-from "hello")
    (receive-mutilple "hello" println)))

;receive messages in pair
(defn- print-two-messages [messages]
  (println (clojure.string/join "::" messages)))

(defn -main1 []
  (with-rabbit ["localhost"]
    (let [message-pairs (partition 2 (message-seq "hello"))]
      (doseq [message-pair message-pairs]
        (print-two-messages message-pair)))))