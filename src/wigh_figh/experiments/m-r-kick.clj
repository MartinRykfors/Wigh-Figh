(ns wigh-figh.experiments.m-r-kick
  (:use [overtone.live]
        [wigh-figh.fader]))

(defn chirp [time start-freq end-freq]
  (with-overloaded-ugens
    (let [env (env-gen (env-perc 0 time))]
      (* env (sin-osc (lin-lin env 0 1 end-freq start-freq))))))

(defsynth kick []
  (->> [[0.01 600 200]
        [1 100 20]
        [3 50 20]]
       (map #(apply chirp %))
       (reduce +)
       (#(/ % 3))
       (* (env-gen (env-cutoff) :action FREE))
       (out [0 1])))

(kick)

(defn burst [time hi-cut lo-cut]
  (with-overloaded-ugens
    (-> (white-noise)
        (* (env-gen (env-perc 0 time) :action FREE))
        (lpf lo-cut)
        (hpf hi-cut))))

(defsynth snare []
  (->> (burst 0.3 300 2000)
       (out [0 1])))
(snare)

(defsynth hihat []
  (->> (burst 0.1 2000 10000)
       (+ (sin-osc 500))
       (#(decimator:ar % 1000 8))
       (out [0 1])))
(hihat)

(defn m-seq [time]
  (let [next-time (+ time 1000)]
    (at time (kick))
    (at (+ time 250) (hihat))
    (at (+ time 500) (snare))
    (at (+ time 750) (hihat))
    (apply-by next-time #'m-seq [next-time])))

(m-seq (now))
(stop)
