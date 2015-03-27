(ns wigh-figh.pattern
  (:use [overtone.live :only [apply-by]]))

(defn- pat-gen [start dur xs]
  (let [num-xs (count xs)
        step-length (/ dur num-xs)
        start-times (map #(+ start (* step-length %)) (range num-xs))
        x-times (map vector start-times (repeat num-xs step-length) xs)
        zs (map #(zipmap [:start :dur :pat] %) x-times)]
    (mapcat #(if (vector? (% :pat))
               (pat-gen (% :start) (% :dur) (% :pat))
               (cond (= 1 (% :pat)) (list (% :start))
                     (>= 0 (% :pat)) (list)
                     :else (pat-gen (% :start) (% :dur) (repeat (% :pat) 1)) )) zs)))

(defn pattern [pat] #(pat-gen 0 % pat))

(defn rot
  ([xs]
   (conj (vec (rest xs)) (first xs)))
  ([xs n]
   (nth (iterate rot xs) n)))

(defn rot-rec
  ([xs]
   (map #(if (vector? %) (vec (rot-rec %)) %) (vec (rot xs))))
  ([xs n]
   (nth (iterate rot-rec xs) n)))

(defn sequencer [time num-beats measure-length gen]
  (let [next-time (+ measure-length time)]
    (doseq [[seq-gen trigger-f] @gen]
     (try 
       (let [trigger-times (take-while #(< % num-beats) (seq-gen num-beats))]
         (doseq [trig-time trigger-times]
           (apply-by (+ time (* (/ measure-length num-beats) trig-time)) trigger-f)))
       (catch Exception e (prn "caught exception from sequence gen"))))
    (apply-by next-time #'sequencer [next-time num-beats measure-length gen])))
