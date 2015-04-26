(ns wigh-figh.sessions.lcc
  (:use [overtone.live]
        [wigh-figh.fader]
        [wigh-figh.pattern]
;        [wigh-figh.visuals.laser]
        ))

(defn chirp [time start-freq end-freq]
  (with-overloaded-ugens
    (let [env (env-gen (env-perc 0 time))]
      (* env (sin-osc (lin-lin env 0 1 end-freq start-freq))))))

(defsynth kick [amp 0.5]
  (let [sig (->> [[0.01 3000 200]
                  [0.3 170 70]
                  [0.6 80 20]]
                 (map #(apply chirp %))
                 (mix)
                 (* amp))
        _ (detect-silence sig 0.0001 0.1 FREE)]
    (out [0 1] sig)))






(defonce gen (atom nil))

(reset! gen
        [
         [(pattern [4]) #(do (kick))]
         ])

(sequencer (+ 1000 (now)) 4 3000 gen )
(stop)

