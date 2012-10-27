
(defn power [base n]
  (loop [ r 1 x 1] (if (> x n) r (recur (* r base) (inc x)))))
  
;(print (power 2 1))

(defn fac [^long n] 
  (if (<= n 1 ) 1 (*' n (fac (dec n)))))
    
;(print (fac 24))

;servers
(def N 24)
(println (str "total servers:" N))

; 
(def u (/ 60 0.07))
(println (str "departure rate:" u))

;arrival rate
(def a 40)
(println (str "arrival rate:" a))

;utilization
(def p (/ a u))
(println (str "utilization:" p))

;Probability of having no vehicles
(def P0
  (let [r1 (loop [r 0.0 x 0] (if (>= x N) r (recur (+ r (/ (power p x) (fac x)) ) (inc x))) )
    r2 (/ (power p N) (* (fac N) (- 1 (/ p N))))]
    (/ 1 (+ r1 r2))))
    
(println (str "Probability of having no vehicles:" P0))

;Probability of having n vehicles
;if n <= N
(defn Pn1 [n]
  (/ (* (power p n) P0) (fac n) ))
  
;(println (Pn1 2))

;if n >= N
(defn Pn2 [n]
  (/ (* (power p n) P0) (* (fac N) (power N (- n N))) ))

;(println (Pn2 5))


;Average length of queue
(def Q 
  (* (/ (* P0 (power p (inc N)))  (* N (fac N)))
      (/ 1 (power (- 1 (/ p N)) 2) )
  ))
  
(println (str "Average length of queue:" Q))

;Average time waiting in queue
(def W 
  (- (/ (+ p Q) a)  (/ 1 u) ))
  
(println (str "Average time waiting in queue(seconds):" (* W 60)))

;Average time spent in system
(def T 
  (/ (+ p Q) a ))
  
(println (str "Average time spent in system(seconds):" (* T 60)))




