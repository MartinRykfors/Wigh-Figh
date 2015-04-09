;; todo
;; fix sexp fu (thought I did that or was it at work?)
;; fix rounding error in viz
;; fm synth needs amp parameter
;; probably not going to bother with the mixers for now
;; practice building the fm synth
;; practice designing the hihat and snare
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

(defsynth kick []
  (->> [[0.02 2000 200]
        [1 300 70]
        [1 50 20]]
       (map #(apply chirp %))
       (mix)
       (#(* (- 1 (detect-silence % 0.0001 0.1 FREE)) %))
       (out [0 1])))

(defsynth fm [note 40 time1 1 mult1 4 time2 2.7 index1 3 ffreq 1000 mult2 3 time3 0.3 index2 3 hfreq 20]
  (let [base-freq (midicps note)]
    (->> (sin-osc (* mult2 base-freq))
         (* (env-gen (env-perc 0.0 time3) ))
         (* index2)
         (sin-osc (* mult1 base-freq))
         (* (env-gen (env-perc 0.0 time2) ))
         (* index1)
         (sin-osc base-freq)
         (* (env-gen (env-perc 0.1 time1) :action FREE))
         (#(lpf % ffreq))
         (#(hpf % hfreq))
         (out [0 1]))))

(defn play-note []
  (doseq [note (chord :c3 :minor)]
    (fm :note note
        :time1 (fader 0 2 "-----#------------")
        :time2 (fader 0 2 "----#-------------")
        :time3 (fader 0 2 "--------#---------")
        :mult1 5
        :mult2 4
        :index1 (fader 0 3 "-----#------------")
        :index2 (fader 0 9 "--------#---------")
        :ffreq (fader 100 17000 3 "-------#----------"))))

(defn snare []
  ( fm :note 20
       :time1 (fader 0 1 "-------#----------")
       :time2 (fader 0 1 "---------#--------")
       :time3 (fader 0 0.3 "----#-------------")
       :mult1 5.3
       :mult2 100.334
       :index1 (fader 0 30 "-----#------------")
       :index2 (fader 0 30 "------------#-----")
       :ffreq (fader 100 17000 3 "---------------#--")
       :hfreq (fader 100 17000 3 "-------#----------")
       ))

(defn hihat []
  ( fm :note 30
       :time1 (fader 0 2 "--------#---------")
       :time2 (fader 0 1 "--#---------------")
       :time3 (fader 0 2 "------------#-----")
       :mult1 20.3
       :mult2 30
       :index1 (fader 0 9 "---------------#--")
       :index2 (fader 0 9 "----------------#-")
       :ffreq (fader 100 17000 3 "-----------------#")
       :hfreq (fader 100 17000 3 "--------------#---")
       ))


(defonce gen (atom nil))

(reset! gen
        [
         [(pattern [4]) #(do (play-note) (transition-state!) (kick)  (hihat) )]
         [(pattern [0]) #(do (snare) )]
         [(pattern [0]) #(do (hihat) )]
         [(pattern [0]) #(do (kick)  )]
         ])

(sequencer (+ 1000 (now)) 4 3000 gen )
(stop)

