(ns wigh-figh.experiments.visuals
  (:use [wigh-figh.pattern]
        [overtone.live])
  (:require [wigh-figh.visuals.gridshift :as viz]))

(viz/transition-state!)

(defsynth kick []
  (->> (sin-osc (+ 30 (* 600 (env-gen (env-perc 0 0.1)))))
       (* (env-gen (env-perc 0 1.3) :action FREE))
       (out [0 1])))

(kick)

(def generator (atom []))
(reset! generator
        [[(pattern [1 [1 0 0 1] [ 0 1 0 1] 1 ]) #(do (kick) (viz/transition-state!))]])
(sequencer (+ (now) 1000) 4 2000 generator)
