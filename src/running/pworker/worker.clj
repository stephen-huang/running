(ns running.pworker.worker
  (:use running.mq.send)
  (:use running.mq.receive)
  (:use running.mq.rabbitmq))

(def works (ref {}))
(def WORKER-QUEUE "worker_queue")
(def worker-init-value :__work_init__)

(defmacro [service-name args & exprs]
  `(let [worker-name# (keyword '~service-name)]
     (dosync
       (alter works assoc worker-name# (fn ~args (do ~@exprs)))
       (def ~service-name (worker-runner worker-name# ~args)))))

(defmacro worker-runner [work-name worker-args]
  `(fn ~worker-args
     (on-swarm ~worker-name ~worker-args)))

(defn on-swarm [worker-service args]
  (let [worker-data (ref worker-init-value)
        worker-transport (dispatch-work worker-service args worker-data)]
    (fn [accessor]
      (condp = accessor
        :complete? (not (= worker-init-value @worker-data))
        :value (attribute-from-response @worker-data :value)
        :status (@worker-data :status)
        :disconnect (disconnect-worker worker-transport)))))



(defn dispatch-worker [worker-name args worker-ref]
  (let [return-q-name (str (UUID/randomUUID))
        request-object (request-envelope worker-name args return-q-name)
        worker-transport (update-on-response worker-ref return-q-name)]
    (send-message WORKER-QUEUE request-object)
    worker-transport))

(defn request-envelope
  ([worker-name args]
    {:worker-name worker-name :worker-args args})
  ([worker-name args return-q-name]
    (assoc (request-envelope worker-name args) :return-q return-q-name)))

(defn update-on-response [worker-ref return-q-name]
  (let [channel (.createChannel *rabbit-connection*)
        consumer (consumer-for channel return-q-name)
        on-response (fn [response-message]
                        (dosync
                          (ref-set worker-ref (read-string response-message))
                          (.queueDelete channel return-q-name)
                          (.close channel)))]
    (future (on-response (delivery-from channel consumer)))
    [channel return-q-name]))

