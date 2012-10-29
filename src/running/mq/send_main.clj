(ns running.mq.send_main
  (:use running.mq.send))

(defn -main []
  (with-rabbit ["localhost"]
    (send-message "hello" "Hello World "))
  (println "done sending"))