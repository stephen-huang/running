(ns running.pworker.myworker
  (:use running.pworker.worker))

(defworker long-computation-one [x y]
  (Thread/sleep 1000)
  (* x y))

(defworker long-computation-two [a b c]
  (Thread/sleep 2000)
  (+ a b c))

(defworker expensive-audit-log [z]
  (println "expensive audit log" z)
  (Thread/sleep 4000))


