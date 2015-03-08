(ns wigh-figh.group-macros
  (:use [overtone.live]
        [wigh-figh.fader]
        [wigh-figh.context]))

(standard-groups)

(declare-mixer :hit)

(defsynth hit-s [freq 400]
  (->> (saw freq)
       (* (line 1 0 0.4 :action FREE))
       (#(lpf % (fader 200 10000 4 "---#--------------")))
       (out hit-bus)))

(ctl hit-mixer :amp (fader 0 1 2 "---------#--------"))
(ctl hit-mixer :pos (fader -1 1 "#-----------------"))
(defn m-seq [t]
  (hit-s [:tail synth-g])
  (apply-by (+ 1000 t) #'m-seq [ (+ 1000 t) ]))
(m-seq (now))
(stop)
