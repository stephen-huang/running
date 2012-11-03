(ns running.pworker.processor
  (:use running.mq.rabbitmq running.mq.send running.mq.receive running.pworker.common))

(defn response-for [worker-handler worker-args]
  (try
    (let [value (apply worker-handler worker-args)]
      {:value value :status :success})
    (catch Exception e
      (println e)
      {:status :error})))

(defn process-request [worker-handler worker-args return-q]
  (future
    (with-rabbit ["localhost"]
      (let [response-envelope (response-for worker-handler worker-args)]
        (if return-q (send-message return-q response-envelope))))))

(defn handle-request-message [req-str]
  (try
    (let [req (read-string req-str)
          worker-name (req :worker-name)
          worker-args (req :worker-args)
          return-q (req :return-q)
          worker-handler (@workers worker-name)]
      ;(println "handler:" worker-handler)
      (if (not (nil? worker-handler))
        (do
          (println "Processing:" worker-name "with-args:" worker-args)
          (process-request worker-handler worker-args return-q))))
    (catch Exception e (println e))))

(defn start-handler-process [q-name]
  (doseq [request-message (message-seq q-name)]
    (do
      ;(println "received message:" (str request-message))
      (handle-request-message request-message))
    ))

