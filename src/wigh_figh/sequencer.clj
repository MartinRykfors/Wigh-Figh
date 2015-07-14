(ns wigh-figh.sequencer
  (:use [overtone.live :only [apply-by]]))

(defn sequencer [time measure-index measure-length gen]
  (doseq [[seq-gen trigger-f] @gen]
    (try 
      (let [trigger-times (take-while #(< % 1) (seq-gen measure-index))]
        (doseq [trig-time trigger-times]
          (apply-by (+ time (* measure-length trig-time)) trigger-f)))
      (catch Exception e (prn (str "Sequence exn: " (.getMessage e))))))
  (let [next-time (+ measure-length time)]
    (apply-by next-time #'sequencer [next-time (inc measure-index) measure-length gen])))

(defn length-sequencer [time measure-index measure-length gen]
  (doseq [[seq-gen on-f off-f] @gen]
    (try 
      (let [trigger-times (seq-gen measure-index)]
        (doseq [trig-time trigger-times]
          (apply-by (+ time (* measure-length (:start trig-time))) on-f)
          (apply-by (+ time (* measure-length (:end trig-time))) off-f)))
      (catch Exception e (prn (str "Sequence exn: " (.getMessage e))))))
  (let [next-time (+ measure-length time)]
    (apply-by next-time #'length-sequencer [next-time (inc measure-index) measure-length gen])))
