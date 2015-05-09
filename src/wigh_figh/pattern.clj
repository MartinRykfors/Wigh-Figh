(ns wigh-figh.pattern
  (:use [overtone.live :only [apply-by]]))

(defprotocol Pattern-gen
  (pattern-unit [this start duration measure-index])
  (pattern-modifier [this]))

(defn pattern [pat] #(pattern-unit (pattern-modifier pat) 0 1 %))

(extend-type java.lang.Long
  Pattern-gen
  (pattern-unit [x start duration _]
    (->> x
       (range)
       (map #(/ % x))
       (map #(* % duration))
       (map #(+ start %))
       (vec)))
  (pattern-modifier [x] [x]))

(extend-type clojure.lang.PersistentVector
  Pattern-gen
  (pattern-unit [xs start duration measure-index]
    (let [num-xs (count xs)
        step-length (/ duration num-xs)
        start-times (map #(+ start (* step-length %)) (range num-xs))
        x-times (map vector start-times xs)
        zs (map #(zipmap [:start :pat] %) x-times)]
    (mapcat #(pattern-unit (% :pat) (% :start) step-length measure-index) zs)))
  (pattern-modifier [xs] (vector (vec (mapcat pattern-modifier xs)))))

(extend-type nil
  Pattern-gen
  (pattern-unit [_ _ _ _] [])
  (pattern-modifier [_] nil))

(extend-type clojure.lang.PersistentHashSet
  Pattern-gen
  (pattern-unit [xs start duration measure-index]
    (let [index (rand-int (count xs))
        choice (-> xs
                   (vec)
                   (nth index))]
      (pattern-unit choice start duration measure-index)))
  (pattern-modifier [xs] (set (map pattern-modifier xs))))

(extend-type clojure.lang.PersistentArrayMap
  Pattern-gen
  (pattern-unit [_ _ _ _] [])
  (pattern-modifier [xs]
    (let [{:keys [x p]} xs]
      (repeat x (flatten (pattern-modifier p))))))

(extend-type clojure.lang.PersistentList
  Pattern-gen
  (pattern-unit [xs start duration measure-index]
    (let [length (count xs)
          choice (nth xs measure-index)]
      (println "mi" measure-index)
      (println "ln" length)
      (println "ch" choice)
      (println "xs" xs)
      (pattern-unit choice start duration (quot measure-index length))))
  (pattern-modifier [xs] (list (mapcat pattern-modifier xs))))

(extend-type clojure.lang.LazySeq
  Pattern-gen
  (pattern-unit [xs start duration measure-index]
    (pattern-unit (list xs) start duration measure-index))
  (pattern-modifier [xs] (list (pattern-modifier xs))))


(type (map inc [0 1 2]))

(defn sequencer [time num-beats measure-length gen]
  (let [next-time (+ measure-length time)]
    (doseq [[seq-gen trigger-f] @gen]
     (try 
       (let [trigger-times (take-while #(< % num-beats) (seq-gen num-beats))]
         (doseq [trig-time trigger-times]
           (apply-by (+ time (* (/ measure-length num-beats) trig-time)) trigger-f)))
       (catch Exception e (prn "caught exception from sequence gen"))))
    (apply-by next-time #'sequencer [next-time num-beats measure-length gen])))
