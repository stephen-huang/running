(ns running.pworker
  (:use running.mq.rabbitmq)
  (:use running.mq.send)
  (:use running.mq.receive))

(defn handle-request-message [req-str]
  (try
    (let [req (read-string req-str)
          worker-name (req :worker-name)
          worker-args (req :worker-args)
          return-q (req :return-q)
          worker-handler (@workers worker-name)]
      (if (not (nil? worker-handler))
        (do
          (println "Processing:" worker-name "with-args:" worker-args)
          (process-request worker-handler worker-args return-q))))
    (catch Exception e)))

(defn process-request [worker-handler worker-args return-q]
  (future
    (with-rabbit ["localhost"]
      (let [response-envelope (response-for worker-handler worker-args)]
        (if return-q (send-message return-q response-envelope))))))

(defn response-for [worker-handler worker-args]
  (try
    (let [value (apply worker-handler worker-args)]
      {:value value :status :success})
    (catch Exception e
      {:status :error})))

(defn start-handler-process []
  (doseq [request-message (message-seq WORKER-QUEUE)]
    (handle-request-message request-message)))

(defn -main []
  (with-rabbit ["localhost"]
    (start-handler-process )))