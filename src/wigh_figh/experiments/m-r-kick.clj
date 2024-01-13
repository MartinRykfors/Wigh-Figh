(ns wigh-figh.experiments.m-r-kick
  (:require [overtone.live :refer :all]
            [wigh-figh.fader :as f]))

(defn chirp [time start-freq end-freq]
  (with-overloaded-ugens
    (let [env (env-gen (env-perc 0 time))]
      (* env (sin-osc (lin-lin env 0 1 end-freq start-freq))))))

(defsynth kick [out-bus 0]
  (->> [[1.02 6000 200]
        [1 100 20]
        [3 50 20]]
       (map #(apply chirp %))
       (reduce +)
       (#(/ % 3))
       (* (env-gen (env-cutoff) :action FREE))
       (out out-bus)))

(def inst-gr (group "instruments"))
(def mix-gr (group "mixer channels" :after inst-gr))
(def send-gr (group "send effects" :after mix-gr))
(def return-gr (group "return channels" :after send-gr))
(def send-bus (audio-bus))
(def return-bus (audio-bus))

(defsynth mixer-channel [in-bus 10 amp 0.7 send 0.0]
  (->> (in in-bus)
       (* amp)
       (out [0 1]))
  (->> (in in-bus)
       (* send)
       (out send-bus)))

(defsynth return-channel [amp 0.7]
  (->> (in return-bus)
       (* amp)
       (out [0 1])))

(def bus1 (audio-bus))
(def ch1 (mixer-channel [:tail mix-gr] bus1))
(def ret-ch (return-channel [:tail return-gr]))


(defsynth rev []
  (as-> (in send-bus) x
    (* x (sin-osc 2000))
    (out return-bus x)))

(defsynth pass [] (out return-bus (in send-bus)))

(def rev-inst (rev [:tail send-gr]))
(def pass-inst (pass [:tail send-gr]))

(kick [:tail inst-gr] bus1)
(kick  0)
(ctl ch1 :amp 0.8)
(ctl ch1 :send 0.1)
(pp-node-tree)
(clear-all)

;; put lfo group at head of foundation pre overtone
;; put mixer, send, return in tail of post overtone, or after one another
;; also you only need one send bus and have the effect replace-out the bus. look at definition for fx-reverb and the like

(stop)
;; (defn burst [time hi-cut lo-cut]
;;   (with-overloaded-ugens
;;     (-> (white-noise)
;;         (* (env-gen (env-perc 0 time) :action FREE))
;;         (lpf lo-cut)
;;         (hpf hi-cut))))

;; (defsynth snare []
;;   (->> (burst 0.3 300 2000)
;;        (out [0 1])))
;; (snare)

;; (defsynth hihat []
;;   (->> (burst 0.1 2000 10000)
;;        (+ (sin-osc 500))
;;        (#(decimator:ar % 1000 8))
;;        (out [0 1])))
;; (hihat)

;; (defn m-seq [time]
;;   (let [next-time (+ time 1000)]
;;     (at time (kick))
;;     (at (+ time 250) (hihat))
;;     (at (+ time 500) (snare))
;;     (at (+ time 750) (hihat))
;;     (apply-by next-time #'m-seq [next-time])))

;; (m-seq (now))
;; (stop)
