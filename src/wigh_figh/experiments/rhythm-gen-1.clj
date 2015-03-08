(ns wigh-figh.experiments.rhythm-gen-1
  (:use [overtone.live]
        [wigh-figh.fader]))

(defn op [freq amp phase] (with-overloaded-ugens (* amp (sin-osc freq phase))))

(defsynth fm-bang [note 60 f-freq 4000 index1 2 index2 2 decay0 1 decay1 1 decay2 1]
  (let [b-freq (midicps note)]
    (->> (op (* 2 b-freq) index2 0)
         (* (env-gen (env-perc 0.01 decay2)))
         (op (* 3 b-freq) index1)
         (* (env-gen (env-perc 0.01 decay1)))
         (op (* 1 b-freq) 0.2)
         (* (env-gen (env-perc 0.02 decay0) :action FREE))
         (#(lpf % f-freq))
         (out [0 1]))))

(defn trigger [note]
  (fm-bang :note note
           :f-freq (fader 700 15000 4 "---#---------")
           :index2 (fader 0 4 "------#------")
           :decay2 (fader 0.1 2 "--#----------")
           :index1 (fader 0 4 "-----#-------")
           :decay1 (fader 0.1 2 "----#--------")
           :decay0 (fader 0.1 2 "-----#-------")))

(defn bang-chord []
  (doseq [note [42 45 49]] (trigger note)))

(stop)

(def choices [
              [1 1 4]
              ])

(defn rand-choice [] (get choices (rand-int (count choices))))

(defn play-seq [apply-times]
  (doseq [time apply-times] (apply-at time bang-chord)))

(defn times [start-time bpm choice]
  (let [beat-time (* (* (/ 60 bpm) 1000) 4) 
        delta-time (/ (* (get choice 1) beat-time) (get choice 2))
        num-events (get choice 0)
        time-of-event #(+ start-time (* delta-time %))]
    [(map time-of-event (range num-events)) (time-of-event num-events)]))

(defn b-seq [t]
  (let [[trigger-times next-time] (times t 150 (rand-choice))]
    (play-seq trigger-times)
    (apply-by next-time #'b-seq [next-time])))
(b-seq (now))

(stop)
