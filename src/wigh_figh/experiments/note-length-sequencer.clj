(ns wigh-figh.experiments.note-length-sequencer
  (:use [overtone.live]
        [wigh-figh.pattern]
        [wigh-figh.sequencer]))

(defsynth hit [note 40 gate 1 index 2 ffreql 500 lfo 0.5]
  (let [base-freq (midicps note)]
    (->> 
         (sin-osc (* 1 base-freq))
         (* (+ 1 index (* index (sin-osc lfo [0 0.25]))))
         (* (env-gen (env-adsr 3 0 1.0 9) :gate gate))
         (sin-osc base-freq)
         (* (env-gen (env-adsr 0 0.2 0.5 0.2) :gate gate))
         (#(lpf % ffreql))
         (* 0.3)
         (out [0 1]))))

(kill h-i)
(def h-i (hit))

(defn turn-on []
  (ctl h-i :gate 1))

(defn turn-off []
  (ctl h-i :gate 0))

(ctl h-i :note 60)
(turn-off)
(turn-on)

(defonce gen (atom nil))

(reset! gen
        [
         [(lengths [1/16 1/5 1/32 1/8]) turn-on turn-off]
         ])

(length-sequencer (+ 500 (now)) 0 4000 gen)
(stop)
