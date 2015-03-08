(ns wigh-figh.sessions.brighton-fp.cheat
  (:use [overtone.live]
        [wigh-figh.fader]))

(defsynth fm-synth [note 70]
  (let [base-freq (midicps note)]
    (out [0 1] (lpf (* 0.3 (sin-osc base-freq (* 3 (sin-osc base-freq))) (env-gen (env-perc 0 0.5) :action FREE)) 5000))))

(defsynth fm-synth2 [note 70]
  (let [base-freq (midicps note)]
    (->> (sin-osc (* 2 base-freq))
         (* (fader 0 10 "------------#-----"))
         (* (env-gen (env-perc 0 (fader 0 2 "----------#-------")) ))
         (sin-osc (* 200 base-freq))
         (* (fader 0 3 "-----------#------"))
         (* (env-gen (env-perc 0 (fader 0 2 "------------#-----")) ))
         (sin-osc base-freq)
         (* 0.3)
         (* (env-gen (env-perc 0 (fader 0 2 "---------------#--")) :action FREE))
         (#(lpf % (fader 200 20000 3 "#-----------------")))
         (out [0 1]))))


(stop)


(defn trigger []
  (doseq [note (chord :C4 :minor)] (fm-synth2 note)))

(def choices [
              [1 1 1]
              ;[1 1 4]
              ;[2 1 8]
              ;[3 1 12]
              ;[4 1 16]
              ;[6 1 24]
              ;[1 10000 1]
              ])

(defn play-seq [apply-times] (doseq [time apply-times] (apply-at time trigger)))

(defn rand-choice [] (get choices (rand-int (count choices))))

(defn times [start-time bpm choice]
  (let [beat-time (* (* (/ 60 bpm) 1000) 4) 
        delta-time (/ (* (get choice 1) beat-time) (get choice 2))
        num-events (get choice 0)
        time-of-event #(+ start-time (* delta-time %))]
    [(map time-of-event (range num-events)) (time-of-event num-events)]))

(defn sequence-gen [t]
  (let [[trigger-times next-time] (times t 150 (rand-choice))]
    (play-seq trigger-times)
    (apply-by next-time #'sequence-gen [next-time])))
(sequence-gen (now))
