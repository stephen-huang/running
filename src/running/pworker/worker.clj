(ns running.pworker.worker)

(def works (ref {}))

(defmacro [service-name args & exprs]
  `(let [worker-name# (keyword '~service-name)]
     (dosync
       (alter works assoc worker-name# (fn ~args (do ~@exprs)))
       (def ~service-name (worker-runner worker-name# ~args)))))

(defmacro worker-runner [work-name worker-args]
  `(fn ~worker-args
     (on-swarm ~worker-name ~worker-args)))

(def worker-init-value :__work_init__)
(defn on-swarm [worker-service args]
  (let [worker-data (ref worker-init-value)
        worker-transport (dispatch-work worker-service args worker-data)]
    (fn [accessor]
      (condp = accessor
        :complete? (not (= worker-init-value @worker-data))
        :value (attribute-from-response @worker-data :value)
        :status (@worker-data :status)
        :disconnect (disconnect-worker worker-transport)))))