(ns running.mq.main-multicast-receive
  (:use running.mq.receive running.mq.rabbitmq running.mq.config))

(defn -main []
  (println "waiting for broadcast...")
  (with-rabbit ["localhost"]
    ;(println (next-message-from "fanex" FAN-EXCHANE-TYPE "test-key"))
    (doseq [message (message-seq "fanex" FAN-EXCHANE-TYPE "test-key")]
      (println message))
    ))

