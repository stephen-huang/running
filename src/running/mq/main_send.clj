(ns running.mq.main-send
  (:use running.mq.send running.mq.rabbitmq))

(defn -main []
  (with-rabbit ["localhost"]
    (send-message "hello" "Hello World "))
  (println "done sending"))