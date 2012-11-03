(ns running.pworker.worker
  (:use running.mq.send running.mq.receive running.mq.rabbitmq running.pworker.common)
  (:import (java.util UUID)))

(defn all-complete? [swarm-request]
  (every? #(% :complete?) swarm-request))

(defn disconnect-worker [[channel q-name]]
  (.queueDelete channel q-name))

(defn disconnect-all [swarm-requests]
  (doseq [req swarm-requests]
    (req :disconnect)))

(defn wait-until-completion [swarm-requests timeout]
  (loop [all-complete (all-complete? swarm-requests)
         elapsed-time 0]
    (if (> elapsed-time timeout)
      (do
        (disconnect-all swarm-requests)
        (throw (RuntimeException. (str "remote worker timeout exceeded " timeout " ms"))))
      (if (not all-complete)
        (do
          (Thread/sleep 100)
          (recur (all-complete? swarm-requests) (+ elapsed-time 100)))))))

(defmacro from-swarm [swarm-requests & expr]
  `(do
     (wait-until-completion ~swarm-requests 5000)
     ~@expr))

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

(defn dispatch-worker [worker-name args worker-ref]
  (let [return-q-name (str (UUID/randomUUID))
        request-object (request-envelope worker-name args return-q-name)
        worker-transport (update-on-response worker-ref return-q-name)]
    ;(println "sending request" (str request-object))
    (send-message WORKER-QUEUE request-object)
    worker-transport))

(defn attribute-from-response [worker-data attribute-name]
  (if (= worker-init-value worker-data)
    (throw (RuntimeException. "work not complete")))
  (if (not (= :success (keyword (worker-data :status))))
    (throw (RuntimeException. "work have erros")))
  (worker-data attribute-name))

(defn on-swarm [worker-name args]
  (let [worker-data (ref worker-init-value)
        worker-transport (dispatch-worker worker-name args worker-data)]
    (fn [accessor]
      (condp = accessor
        :complete? (not (= worker-init-value @worker-data))
        :value (attribute-from-response @worker-data :value)
        :status (@worker-data :status)
        :disconnect (disconnect-worker worker-transport)))))

(defmacro worker-runner [worker-name should-return worker-args]
  `(fn ~worker-args
     (if ~should-return (on-swarm ~worker-name ~worker-args))))

(defmacro defworker [service-name args & exprs]
  `(let [worker-name# (keyword '~service-name)]
     (dosync
       (alter workers assoc worker-name# (fn ~args (do ~@exprs)))
       (def ~service-name (worker-runner worker-name# true ~args)))))


