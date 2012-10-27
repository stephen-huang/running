(ns rabbit-test.send
  (:use rabbit-test.core))

(def Q_name "hello")



;(defn -main []
;  (let[con (new-connection "localhost")
;      channel (.createChannel con)
;      message "Hello World"]
;      (.queueDeclare channel Q_name false false false nil)
;      (.basicPublish channel "" Q_name  nil (.getBytes message))
;      (println (str "[x] Sent '" message "'")))
;  )
(defn send-message [routing-key message-object]
  (with-open [channel (.createChannel *rabbit-connection* )]
    (.basicPublish channel "" routing-key nil (.getBytes (str message-object)))))

(defn send-mutilple [] ())

(defn -main []
  (with-rabbit ["localhost"]
    (send-message "hello" "Hello World "))
  (println "done sending"))