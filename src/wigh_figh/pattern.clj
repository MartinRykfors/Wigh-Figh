(ns wigh-figh.pattern
  (:require [wigh-figh.scheduling :as s]))

(defprotocol Length-unit
  (note-lengths [this normal-factor start duration]))

(defprotocol Pattern-unit
  (trigger-times [this start duration]))

(defprotocol Pattern-expander
  (expand [this measure-index]))

(defn pattern [pat]
  (fn [measure-index] (trigger-times (expand pat measure-index) 0 1)))

(defmethod s/schedule :pattern [type measure-index & rest]
  (let [[p trigger-f] rest
        trigger-times ((pattern p) measure-index)]
    (map (fn [time] [time trigger-f]) trigger-times)))

(defn lengths [pat normal-factor]
  (fn [measure-index] (note-lengths (expand pat measure-index) normal-factor 0 1)))

(defmethod s/schedule :hold [type measure-index & rest]
  (let [[normal-factor pattern on-f off-f] rest
        notes ((lengths pattern normal-factor) measure-index)]
    (mapcat (fn [note] [[(:start note) on-f] [(:end note) off-f]]) notes)))

(extend-type java.lang.Double
  Length-unit
  (note-lengths [x normal-factor start _] [{:start start :end (+ (/ x normal-factor) start)}])
  Pattern-expander
  (expand [x _] [x]))

(extend-type clojure.lang.Ratio
  Length-unit
  (note-lengths [x normal-factor start _] [{:start start :end (+ (/ x normal-factor) start)}])
  Pattern-expander
  (expand [x _] [x]))

(extend-type java.lang.Long
  Pattern-unit
  (trigger-times [x start duration]
    (->> x
         (range)
         (map #(/ % x))
         (map #(* % duration))
         (map #(+ start %))
         (vec)))
  Length-unit
  (note-lengths [x normal-factor start _]
    (if (= 0 x)
      [[]]
      [{:start start :end (+ (/ x normal-factor) start)}]))
  Pattern-expander
  (expand [x _] [x]))

(defn- cut-note
  ([note]
   note)
  ([note next-note]
   (if (<= (:end note) (:start next-note))
     note
     (assoc note :end (:start next-note)))))

(defn- cut-notes [notes]
  (->> notes
       (partition-all 2 1)
       (map #(apply cut-note %))))

(extend-type clojure.lang.Sequential
  Length-unit
  (note-lengths [xs normal-factor start duration]
    (let [num-xs (count xs)
          step-length (/ duration num-xs)
          start-times (map #(+ start (* step-length %)) (range num-xs))]
      (->>
       (mapcat #(note-lengths %1 normal-factor %2 step-length) xs start-times)
       (filter #(not (empty? %)))
       (cut-notes))))
  Pattern-unit
  (trigger-times [xs start duration]
    (let [num-xs (count xs)
          step-length (/ duration num-xs)
          start-times (map #(+ start (* step-length %)) (range num-xs))]
      (mapcat #(trigger-times %1 %2 step-length) xs start-times)))
  Pattern-expander
  (expand [xs measure-index] (vector (vec (mapcat #(expand % measure-index) xs)))))

(extend-type nil
  Pattern-unit
  (trigger-times [_ _ _] [])
  Pattern-expander
  (expand [_ _] [nil]))

(defrecord rep [n pattern]
  Pattern-expander
  (expand [this measure-index]
    (mapcat
     #(expand % measure-index)
     (repeat (:n this) (:pattern this)))))

(defn r [n pattern] (->rep n pattern))

(defrecord choice [patterns]
  Pattern-expander
  (expand [this m-i]
    (let [pattern (nth (:patterns this) (rand-int (count (:patterns this))))]
      (expand pattern m-i))))

(defn c
  ([] [])
  ([& patterns]
   (->choice (vec patterns))))

(defrecord indexed [patterns]
  Pattern-expander
  (expand [this m-i]
    (let [n (mod m-i (count (:patterns this)))
          selected-pattern (nth (:patterns this) n)
          reduced-index (quot m-i (count (:patterns this)))]
      (expand selected-pattern reduced-index))))

(defn i
  ([] [])
  ([& patterns]
   (->indexed (vec patterns))))
