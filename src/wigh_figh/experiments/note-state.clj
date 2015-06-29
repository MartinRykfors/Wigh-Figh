(ns wigh-figh.experiments.note-state
  (:use [overtone.live]
        [wigh-figh.fader]))

(defsynth hit [note 40 gate 1 index 2 ffreql 500 lfo 0.5]
  (let [base-freq (midicps note)]
    (->> 
         (sin-osc (* 1 base-freq))
         (* (+ 1 index (* index (sin-osc lfo [0 0.25]))))
         (* (env-gen (env-adsr 3 0 1.0 9) :gate gate))
         (sin-osc base-freq)
         (* (env-gen (env-adsr 1 1 0.5 4) :gate gate :action FREE))
         (#(lpf % ffreql))
         (* 0.3)
         (out [0 1]))))

(def notes (atom nil))

(defn trigger [note]
  (when-not (contains? @notes note)
    (swap! notes #(assoc % note (hit :note note :gate 1)))))

(defn release [note]
  (when (contains? @notes note)
    (let [ins (get @notes note)]
      (ctl ins :gate 0)
      (swap! notes #(dissoc % note)))))

(defn ctlv [k v]
  (doseq [ins (vals @notes)]
    (ctl ins k v)))

(trigger 48)
(trigger 51)
(trigger 55)
(trigger 60)
(trigger 63)
(trigger 67)
(trigger 72)
(trigger 75)
(trigger 79)
(release 48)
(release 51)
(release 55)
(release 60)
(release 63)
(release 67)
(release 72)
(release 75)
(release 79)

(ctlv :ffreql (fader 500 9000 3 "----#-------------"))
(ctlv :index (fader 1 10 "----------------#-"))
(ctlv :lfo (fader 0.1 5 "#-----------------"))
;; problem: notes are not triggered with the ctl'd values
(stop)
