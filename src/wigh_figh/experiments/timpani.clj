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
   (* (sin-osc (fader 10 1000 3 "-------#----------")))
   (#(freq-shift % (fader 3000 17000 3 "------#-----------") 0))
   (+ (* 0.2 (white-noise)))
   (#(rhpf % (fader 1000 17000 3 "----#-------------") 0.6))
   (* (env-gen (env-perc 0.01 (fader 0.05 0.4 "--#---------------")) :action FREE))
   (out [0 1])))

(thing) 

(defonce gen (atom nil))
(reset! gen
        [
         [:pattern [[2 1 1 0] [0 1 1 0] 4 [0 1 0 1] ] #(do (thing))]
         ])

(run-sequencer 120 4 gen)
(stop)

(demo 8 (->>
         (sin-osc 0.3)
         (+ 1)
         (* 0.5)
         (pulse 200)
         (+ (saw 200.8))
         (#(rlpf % 1000 0.6))))

(demo (impulse:ar 400 0))
(demo (* 40 (resonz (dust2 200) 400 0.1)))
(demo 3
      (->
       ;; [270 2300 3000]
       ;; [300 870 2250]
       [400 2000 2550]
       ;; [530 1850 2500]
       ;; [640 1200 2400]
       ;; [660 1700 2400]
       ((fn [f] (->
                 ;; (impulse:ar (+ 78 (* 10 (sin-osc 2 (sin-osc 2)))))
                 (->>
                  (sin-osc 0.3)
                  (+ 1)
                  (* 0.5)
                  (pulse 200)
                  (+ (saw 200.8))
                  (#(rlpf % 1000 0.6)))
                 (resonz f 0.02))))
       ;; ((fn [f] (-> (white-noise) (resonz f 0.04))))
       (mix)
       (normalizer)))
(demo 9
      (->
       (sin-osc 4)
       (* 300)
       (#(sin-osc 302 %))
       (decimator:ar 13)
       (* 200)
       (+ 500)
       (sin-osc)
       (decimator:ar (+ 1800 (* 800 (sin-osc 0.3))))))

(demo (bpf (dust2 200) 600 0.5))
(demo (dust2 200) )
(demo (sin-osc))
