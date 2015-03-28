(ns wigh-figh.experiments.generic-sequencer
  (:use [overtone.live]
        [wigh-figh.pattern]))


(defsynth hit [note 40]
  (let [base-freq (midicps note)]
    (->> (sin-osc (* 14 base-freq))
         (* 3)
         (* (env-gen (env-perc 0 0.2)))
         (sin-osc (* 3 base-freq))
         (* 2)
         (* (env-gen (env-perc 0 1.3)))
         (sin-osc base-freq)
         (* (env-gen (env-perc 0 0.4) :action FREE))
         (#(lpf % 1000))
         (out [0 1]))))

(defsynth tick []
  (->> (white-noise)
       (#(lpf % 19000))
       (#(hpf % 8000))
       (* (env-gen (env-perc 0 0.1) :action FREE))
       (out [0 1])))

(defsynth kick []
  (->> (sin-osc (+ 30 (* 600 (env-gen (env-perc 0 0.1)))))
       (* (env-gen (env-perc 0 1.3) :action FREE))
       (out [0 1])))

(kick)
(hit)
(tick)

(defn random-unit []
  (vec (take 8 (repeatedly #(rand-int 2)))))
(def generators (atom []))
(reset! generators [
                    [(pattern {:x 4 :p #{[0 1]  [0 2]}})
                     (fn []  (doseq [note (chord :c3 :minor)] (hit note))) ]
                    [(pattern [1 0 0 0 1 0 0 1 1 0 0 0 1 1 0 0]) kick ]
                    [(pattern [ {:x 4 :p #{[1 2] [0 1 1 2] [0 1 1 1]}}]) tick ]
                    ])

(sequencer (+ 1000 (now)) 4 2000 generators)
(stop)
