(ns wigh-figh.sessions.lcc
  (:use [overtone.live]
        [wigh-figh.fader]
        [wigh-figh.pattern]
        [wigh-figh.visuals.laser]
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

(defsynth fm [note 60 amp 0.1 attack 0.1 decay1 1 index1 3 decay2 1 mult1 3 mult2 3 index2 3 decay3 1 ffreql 1000 ffreqh 20]
  (let [bfreq (midicps note)]
    (->>
     (sin-osc (* mult2 bfreq))
     (* index2)
     (* (env-gen (env-perc 0 decay3) ))
     (sin-osc (* mult1 bfreq))
     (* index1)
     (* (env-gen (env-perc 0 decay2) ))
     (sin-osc bfreq)
     (* (env-gen (env-perc attack decay1) :action FREE))
     (#(lpf % ffreql))
     (#(hpf % ffreqh))
     (* amp)
     (out [0 1]))))

(play-chord)
(defn play-chord []
  (doseq [note (chord :c3 :minor)]
    (fm
     ;:attack 0.1
     :decay1 (fader 0 2 "----#-------------")
     :decay2 (fader 0 2 "----#-------------")
     :decay3 (fader 0 2 "----#-------------")
     :ffreql (fader 300 17000 3 "-------------#----")
     :index1 (fader 0 10 "----#-------------")
     :index2 (fader 0 10 "----#-------------")
     :mult1 3
     :mult2 3
     :note note)))

(defn snare []
  (fm
     :attack 0
     :decay1 (fader 0 2 "---#--------------")
     :decay2 (fader 0 2 "--------------#---")
     :decay3 (fader 0 2 "--------------#---")
     :ffreql (fader 300 17000 3 "--------------#---")
     :ffreqh (fader 300 17000 3 "-----#------------")
     :index1 (fader 0 10 "----------------#-")
     :index2 (fader 0 10 "---------------#--")
     :mult1 20.23
     :mult2 50.32
     ))

(hihat)
(defn hihat []
  (fm
     :attack 0
     :decay1 (fader 0 2 "-#----------------")
     :decay2 (fader 0 2 "--------------#---")
     :decay3 (fader 0 2 "----------#-------")
     :ffreql (fader 300 17000 3 "----------------#-")
     :ffreqh (fader 300 17000 3 "------------#-----")
     :index1 (fader 0 10 "--------------#---")
     :index2 (fader 0 10 "---------------#--")
     :mult1 20.23
     :mult2 50.32
     ))

(defonce gen (atom nil))

(reset! gen
        [
         [(pattern [1 0 [2 0] 0]) #(do (play-chord) (transition-state!) (kick))]
         [(pattern [[0 1 1 2] [0 1 1 2] [0 1 1 2] [0 1 1 2]]) #(do (hihat))]
         [(pattern [[0 1] [0 1] [0 1] [0 1]]) #(do (snare))]
         ])

(sequencer (+ 1000 (now)) 4 3000 gen )
(stop)

