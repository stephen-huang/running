(ns running.pworker.common)

(def workers (ref {}))
(def WORKER-QUEUE "worker_queue")
(def worker-init-value :__work_init__)

