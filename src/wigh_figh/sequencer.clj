(ns wigh-figh.sequencer
  (:require [wigh-figh.pattern :as p])
  (:use [overtone.live :only [apply-by]]))

(defmulti schedule
  (fn [type measure-index & rest] type))

(defmethod schedule :pattern [type measure-index & rest]
  (let [[pattern trigger-f] rest
        trigger-times ((p/pattern pattern) measure-index)]
    (map (fn [time] [time trigger-f]) trigger-times)))

(defmethod schedule :hold [type measure-index & rest]
  (print "snth" rest)
  (let [[pattern on-f off-f] rest
        notes ((p/lengths pattern) measure-index)]
    (mapcat (fn [note] [[(:start note) on-f] [(:end note) off-f]]) notes)))

(defn sequencer [time measure-index measure-length gen]
  (doseq [[type & rest] @gen]
    (try 
      (let [events (apply schedule type measure-index rest)]
        (doseq [[trig-time trigger-f] events]
          (apply-by (+ time (* measure-length trig-time)) trigger-f)))
      (catch Exception e (prn (str "Sequence exn: " (.getMessage e))))))
  (let [next-time (+ measure-length time)]
    (apply-by next-time #'sequencer [next-time (inc measure-index) measure-length gen])))
