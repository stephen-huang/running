(ns running.pworker.main-worker
  (:use running.mq.rabbitmq running.pworker.worker running.mq.send running.pworker.common)
  (:use running.pworker.myworker))

(defn -main []
  (println "dispatching...")
  (with-rabbit ["localhost"]
    (let [one (long-computation-one 5 6433333)
          two (long-computation-two 1 2 3)
          ]
      (println (:status one))
      (from-swarm [one two]
        (println "one" (one :value))
        (println "two" (two :value))
        )
      ))
  (println "done"))

