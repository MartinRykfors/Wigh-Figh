(ns wigh-figh.pattern
  (:use [overtone.live :only [apply-by]]))

(defmulti pattern-unit (fn [x start dur] (class x)))

(defmethod pattern-unit java.lang.Long
  [x start dur]
  (->> x
       (range)
       (map #(/ % x))
       (map #(* % dur))
       (map #(+ start %))
       (vec)))

(defmethod pattern-unit clojure.lang.PersistentVector
  [xs start dur]
  (let [num-xs (count xs)
        step-length (/ dur num-xs)
        start-times (map #(+ start (* step-length %)) (range num-xs))
        x-times (map vector start-times xs)
        zs (map #(zipmap [:start :pat] %) x-times)]
    (mapcat #(pattern-unit (% :pat) (% :start) step-length) zs)))

(defmethod pattern-unit nil [_ _ _] [])

(defmethod pattern-unit clojure.lang.PersistentHashSet
  [xs start dur]
  (let [index (rand-int (count xs))
        choice (-> xs
                   (vec)
                   (nth index))]
    (pattern-unit choice start dur)))

(defmethod pattern-unit clojure.lang.PersistentArrayMap
  [xs start dur]
  (-> (repeat (:x xs) [(:p xs)])
      (vec)
      (pattern-unit start dur)))

(defn pattern [pat] #(pattern-unit pat 0 % ))

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
