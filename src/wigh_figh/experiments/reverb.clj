(ns wigh-figh.experiments.reverb
  (:use [overtone.live]
        [wigh-figh.fader]
        [wigh-figh.sequencer]
        [wigh-figh.pattern]))


(defsynth ping []
  (let [dec-time 0.03
        sig (->>
             (env-gen (env-perc 0.0 dec-time))
             (* 13000)
             (+ 1000)
             (sin-osc )
             (+ (* 0.8 (white-noise)))
             (* (env-gen (env-perc 0.0 dec-time)))
             (#(free-verb % 0.6 0.5 0.5)))
        _ (detect-silence sig 0.0001 0.1 FREE)]
    (out [0 1] sig)))

(ping)

(demo 3 (as-> (dust:ar 504) x
         (one-pole x 0.9)
         (* 10000 x)
         (+ 400 x)
         (sin-osc x)
         (+ x (sin-osc (+ (* 300 x) 900) (* 1000 x)))
         (* 0.2 x)
         (normalizer x)))

(defsynth texture [dust-level 400 range1 10000 base-freq 400 fm-amt 300 carrier-freq 900 pm-amt 1000 lpfreq 400 lpres 1 amp 0.2]
  (as-> (dust:ar dust-level) x
         (one-pole x 0.9)
         (* range1 x)
         (+ base-freq x)
         (sin-osc x)
         (+ x (sin-osc (+ (* fm-amt x) carrier-freq)))
         (* 0.2 x)
         (normalizer x)
         (rlpf x lpfreq lpres)
         (* amp x)
         (out [0 1] x)))

(def instance (texture))

(ctl instance :dust-level (fader 100 10000 "------#-----------"))
(ctl instance :range1 (fader 100 20000 "----#-------------"))
(ctl instance :base-freq (fader 100 1000 "------------#-----"))
(ctl instance :fm-amt (fader 10 1000 2 "----------------#-"))
(ctl instance :lpfreq (fader 100 14000 3 "-------------#----"))
(ctl instance :lpres (fader 0.2 2 2 "--------#---------"))
(ctl instance :amp (fader 0.0 1 3 "----------#-------"))
;; (stop)
