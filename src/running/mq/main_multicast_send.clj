(ns running.mq.main-multicast-send
  (:use running.mq.send running.mq.rabbitmq running.mq.config))

(defn -main []
  (println "multicasting...")
  (with-rabbit ["localhost"]
    (send-message "fanex" "test-key" "Hello multicast" true))
  (println "done sending"))