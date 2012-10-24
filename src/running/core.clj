(ns running.core
  ;(:use clojure.contrib.io)
  (:import
  (java.io BufferedReader)
  (java.io Reader))
  )

(defn parse_line [line]
  (let [tokens (.split (.toLowerCase line) " ")]
    (map #(vector % 1) tokens)))

(defn combine [mapped]
  (->> (apply concat mapped)
       (group-by first)
       (map (fn [[k v]]
              {k (map second v)}))
       (apply merge-with conj)
    ))

(defn read-lines [file-name]
  (with-open [rdr (clojure.java.io/reader file-name)]
    (doall (line-seq rdr))))

(defn sum [[k v]]
  {k (apply + v)})

(defn reduce-parsed-lines [values]
  (apply merge (map sum values)))

(defn word-count [file-name]
  (->> (read-lines file-name)
  (map parse_line)
  (combine)
  (reduce-parsed-lines)))

;;general function
(defn map-reduce [mapper reducer args-seq]
  (->> (map mapper args-seq)
    (combine)
    reducer))

(defn -main []
  ;(word-count "words.txt")
  (map-reduce parse_line reduce-parsed-lines (read-lines "words.txt"))
  )
