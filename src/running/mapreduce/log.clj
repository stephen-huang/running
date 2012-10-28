(ns running.mq.log
  (:use clojure.contrib.io))

(defn record-start? [log-line]
  (.startsWith log-line "Processing"))

(defn next-log-record [log-lines]
  (let [head (first log-lines)
        body (take-while (complement record-start?) (rest log-lines))]
    (remove nil? (conj body head))))

(defn lazy-request-seq [log-lines]
  (lazy-seq
    (let [record (next-log-record log-lines)]
      (if (empty? record)
        nil
        (cons (remove empty? record)
              (lazy-request-seq (drop (count record) log-lines)))))))

(defn request-seq [file-name]
  (->> (read-lines file-name)
    (drop 2)
    (lazy-request-seq)))

(defn -main []
  (def r1 (request-seq "log.txt"))
  (println (str "total records:"(count r1)))
  (println (first r1)))