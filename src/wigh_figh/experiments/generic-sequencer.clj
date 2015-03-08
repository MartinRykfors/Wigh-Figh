(ns wigh-figh.experiments.generic-sequencer
  (:use [overtone.live]
        [wigh-figh.pattern]))


(defsynth hit [note 40]
  (let [base-freq (midicps note)]
    (->> (saw:ar (* 3 base-freq))
         (* 3)
         (* (env-gen (env-perc 0 0.5)))
         (sin-osc base-freq)
         (* (env-gen (env-perc 0 0.3) :action FREE))
         (#(lpf % 3000))
         (out [0 1]))))

(defsynth tick []
  (->> (white-noise)
       (#(lpf % 10000))
       (#(hpf % 3000))
       (* (env-gen (env-perc 0 0.1) :action FREE))
       (out [0 1])))

(defsynth kick []
  (->> (sin-osc (+ 30 (* 300 (env-gen (env-perc 0 0.5)))))
       (* (env-gen (env-perc 0 0.3) :action FREE))
       (out [0 1])))

(kick)
(hit)
(tick)

(def generators (atom []))
(reset! generators [
                     [(pattern
                       (rot-rec [[1 0 1 0] 2 0 [1 0 0 1]] 0))
                      (fn []   (doseq [note (chord :c3 :minor) t [0 ]] (hit (+ t note)))) ]
                     [(pattern [1 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 ]) kick ]
                     [(pattern [[1 0 2 1] [0 0 0 2] 3 1]) tick]
                     ])
;todo: use references to generator list
;add mechanism for not generating infinite seqs

(defn b-seq [time num-beats measure-length gen]
  (let [next-time (+ measure-length time)]
    (doseq [[seq-gen trigger-f] @gen]
     (try 
       (let [trigger-times (take-while #(< % num-beats) (seq-gen num-beats))]
         (doseq [trig-time trigger-times]
           (apply-by (+ time (* (/ measure-length num-beats) trig-time)) trigger-f)))
       (catch Exception e (prn "caught exception from sequence gen"))))
    (apply-by next-time #'b-seq [next-time num-beats measure-length gen])))

(b-seq (+ 1000 (now)) 6 2000 generators)
(stop)
