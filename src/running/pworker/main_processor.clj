(ns running.pworker.main-processor
  (:use running.mq.rabbitmq running.pworker.common running.pworker.processor running.pworker.myworker))

(defn -main []
  (with-rabbit ["localhost"]
    (start-handler-process WORKER-QUEUE)))