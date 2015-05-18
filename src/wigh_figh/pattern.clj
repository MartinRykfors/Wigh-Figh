(ns wigh-figh.pattern
  ;(:use [overtone.live :only [apply-by]])
  )

(defprotocol Pattern-unit
  (trigger-times [this start duration]))

(defprotocol Pattern-expander
  (expand [this measure-index]))

(defprotocol Pattern-gen
  (pattern-unit [this start duration measure-index])
  (pattern-modifier [this]))

(defn pattern [pat] #(trigger-times (expand pat %) 0 1))

(extend-type java.lang.Long
  Pattern-unit
  (trigger-times [x start duration]
    (->> x
       (range)
       (map #(/ % x))
       (map #(* % duration))
       (map #(+ start %))
       (vec)))
  Pattern-expander
  (expand [x _] [x]))

(extend-type clojure.lang.Sequential
  Pattern-unit
  (trigger-times [xs start duration]
    (let [num-xs (count xs)
        step-length (/ duration num-xs)
        start-times (map #(+ start (* step-length %)) (range num-xs))
        x-times (map vector start-times xs)
        zs (map #(zipmap [:start :pat] %) x-times)]
    (mapcat #(trigger-times (% :pat) (% :start) step-length) zs)))
  Pattern-expander
  (expand [xs measure-index] (vector (vec (mapcat #(expand % measure-index) xs)))))

(extend-type nil
  Pattern-unit
  (pattern-unit [_ _ _] [])
  Pattern-expander
  (pattern-modifier [_ _] []))

(defrecord rep [n pattern]
  Pattern-expander
  (expand [this measure-index]
    (mapcat
     #(expand % measure-index)
     (repeat (:n this) (:pattern this)))))

(defrecord choice [patterns]
  Pattern-expander
  (expand [this m-i]
    (let [pattern (nth (:patterns this) (rand-int (count (:patterns this))))]
      (expand pattern m-i))))


;; (defn sequencer [time num-beats measure-length gen]
;;   (let [next-time (+ measure-length time)]
;;     (doseq [[seq-gen trigger-f] @gen]
;;      (try 
;;        (let [trigger-times (take-while #(< % num-beats) (seq-gen num-beats))]
;;          (doseq [trig-time trigger-times]
;;            (apply-by (+ time (* (/ measure-length num-beats) trig-time)) trigger-f)))
;;        (catch Exception e (prn "caught exception from sequence gen"))))
;;     (apply-by next-time #'sequencer [next-time num-beats measure-length gen])))
