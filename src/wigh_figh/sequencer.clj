(ns wigh-figh.sequencer
  (:require [wigh-figh.scheduling :as s])
  (:use [overtone.live :only [apply-by now]]))

(defn sequencer [time measure-index measure-length gen]
  (doseq [[type & rest] @gen]
    (try 
      (let [events (apply s/schedule type measure-index rest)]
        (doseq [[trig-time trigger-f] events]
          (apply-by (+ time (* measure-length trig-time)) trigger-f)))
      (catch Exception e (prn (str "Sequence exn: " (.getMessage e))))))
  (let [next-time (+ measure-length time)]
    (apply-by next-time #'sequencer [next-time (inc measure-index) measure-length gen])))

(defn run-sequencer [bpm signature generator-atom]
  (let [ms-per-beat (* 1000 (/ 60 bpm))
        measure-length (* signature ms-per-beat)]
    (sequencer (+ 600 (now)) 0 measure-length generator-atom)))
