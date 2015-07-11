(ns wigh-figh.experiments.timpani
  (:use [overtone.live]
        [wigh-figh.fader]
        [wigh-figh.sequencer]
        [wigh-figh.pattern]))

;; http://www.soundonsound.com/sos/Dec01/articles/synthsecrets1201.asp

;; started out as an attempt to create a timpani according to the tutorial
;; turned out to sound miserably
;; now i is something completely different
(defsynth timpani [gate 0]
  (let [burst (->>
               (* (saw 100) (saw 808))
               (* (env-gen (env-perc 0.1 3.9)))
               (#(hpf % 1000))
               (#(lpf % 1000))
               )
        return (local-in:ar 1)
        mode (fn [freq amp time]
               (->>
                (sin-osc (* 0.7 freq))
                (* amp)
                (* (env-gen (env-perc 0.15 time)))))
        modes (mix (map mode [150 225 300 550] [1 0.8 0.6 0.3] [1 1 1 1]))
        sig (+ return (* 0.4 burst) (* 4 (clip2 modes 0.2)))
        _ (local-out (->>
                      sig
                      (#(delay-c % 3 0.1))
                      ;; (* 8)
                      ;; (#(clip2 % 0.8))
                      ;; (#(lpf % 8000))
                      (#(rlpf % 800 0.8))
                      (#(clip2 % 1.0))
                      (* 0.6)))]
    (out [0 1]  sig)))

(timpani)


(defsynth thing []
  (->>
   (saw (fader 10 3000 3 "---------#--------"))
   (* (sin-osc (fader 10 1000 3 "--------#---------")))
   (#(freq-shift % (fader 3000 17000 3 "-------#----------") 0))
   (+ (* 0.2 (white-noise)))
   (#(rhpf % (fader 1000 17000 3 "--------#---------") 0.6))
   (* (env-gen (env-perc 0.01 (fader 0.05 0.4 "---#--------------")) :action FREE))
   (out [0 1])))

(thing) 

(defonce gen (atom nil))
(reset! gen
        [
         [(pattern [16]) #(do (thing))]
         ])

(sequencer (+ 1000 (now)) 0 3200 gen )
(stop)

(demo 8 (->>
         (sin-osc 0.3)
         (+ 1)
         (* 0.5)
         (pulse 200)
         (+ (saw 200.8))
         (#(rlpf % 1000 0.6))))
