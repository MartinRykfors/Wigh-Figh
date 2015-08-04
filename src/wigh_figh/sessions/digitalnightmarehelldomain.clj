(ns wigh-figh.sessions.digitalnightmarehelldomain ;; working title
  (:require [wigh-figh.visuals.fold :as viz])
  (:use [wigh-figh.fader]
        [wigh-figh.pattern]
        [wigh-figh.sequencer]
        [overtone.live]
        )) 

(defn chirp [time start-freq end-freq]
  (with-overloaded-ugens
    (let [env (env-gen (env-perc 0 time))]
      (* env (sin-osc (lin-lin env 0 1 end-freq start-freq))))))

(defsynth kick [amp 0.4]
  (let [noi (->> (white-noise)
                 (* (env-gen (env-perc 0 0.2)))
                 (#(lpf % 1800))
                 (* 0.8))
        sig (->> [[0.03 2000 200]
                  [0.2 120 50]
                  [0.5 50 30]]
                 (map #(apply chirp %))
                 (mix)
                 (+ (* 0.1 noi))
                 (* 80)
                 (#(clip2 % 0.7))
                 (#(lpf % 6000))
                 (#(decimator:ar % (fader 400 18000 3 "----------------#-")))
                 (* amp))
        _ (detect-silence sig 0.0001 0.1 FREE)]
    (out [0 1] sig)))

(defsynth burst [amp 0.3 lfreq 8000 gate 0]
  (->>
   (white-noise)
   (* (env-gen (env-adsr 0.01 0.3 0.7 0.01) :gate gate))
   (+ (* 3.8 (dust2 900)))
   (* amp)
   (#(lpf % lfreq))
   (* (env-gen (env-asr 0.01 1 0.01) :gate gate))
   (out [0 1])))

(def b-inst (burst))
(ctl b-inst :lfreq (fader 200 17000 3 "---------------#--"))

(defn noise-on []
  (do
    (ctl b-inst :gate 1)
    (viz/set-background! :static)))

(defn noise-off []
  (do
    (ctl b-inst :gate 0)
    (viz/set-background! :digital)))

(defsynth tex [freq 2000 amp 0.1 phase 0]
  (->>
   (sin-osc [20.3 20])
   (* 3000 (sin-osc 2.3 phase))
   (sin-osc freq)
   (* amp)
   (* (env-gen (env-triangle 20 1) :action FREE))
   (out [0 1])))

(defn texture []
  (doseq [freq [ 13000]]
    (tex :freq freq :phase (/ (rand-int 200) 400))))

(defsynth hihat [amp 0.6]
  (->>
   (white-noise)
   (* (env-gen (env-perc 0 0.1) :action FREE))
   (#(lpf % (fader 200 17000 3 "-----------------#")))
   (#(hpf % (fader 200 17000 3 "---------------#--")))
   (* amp)
   (out [0 1])))

(defsynth pad [note 50 amp 0.2 gate 1 ffreql 1000 ffreqh 700 drive 2 clip 1 index 4]
  (let [bfreq (midicps note)
        detunes [0 0.2 -0.3 0.7]]
    (->> (map #(saw (+ % bfreq)) detunes)
         (* index)
         (sin-osc bfreq)
         (mix)
         (#(lpf % ffreql))
         (#(hpf % ffreqh))
         (* drive)
         (#(clip2 % clip))
         (* amp)
         (* (env-gen (env-asr 1 1 1) :gate gate))
         (out [0 1]))))

(def p-s (atom []))
(reset! p-s (map #(pad :note % :gate 0) (chord :e3 :minor7)))
(defn play-pad []
  (doseq [pad @p-s]
    (ctl pad :gate 1)))
(defn stop-pad []
  (doseq [pad @p-s]
    (ctl pad :gate 0)))
(defn c-pads [key arg]
  (doseq [pad @p-s]
    (ctl pad key arg)))
(c-pads :ffreql (fader 200 17000 3 "-------#-------"))
(c-pads :ffreqh (fader 200 17000 3 "-----#---------"))
(c-pads :drive (fader 1 20 "----------#-------"))
(c-pads :clip (fader 0 1 "-------------#----"))
(c-pads :amp (fader 0 1 "----#-------------"))
(c-pads :index (fader 0 10 "--------#---------"))
(play-pad)
(stop-pad)
(kill pad)

(defsynth ping []
  (->>
   [17000]
   (map #(sin-osc %))
   (mix)
   (* 0.3)
   (* (env-gen (env-perc 0.1 1) :action FREE))
   (out [0 1])))

(defonce gen (atom nil))
(reset! gen
        [
         ;; [:pattern [(c 4 3) [2 0] 0 0] #(do (viz/reset-animation!) (kick))]
         ;; [:hold 64 [0 1 0 [0 1/2]] #'noise-on #'noise-off]
         ;; [:pattern [(r 16 (c 1 2))] hihat]
         ;; [:pattern [0 0 1 0] ping]
         ;; [:hold 4 [3] play-pad stop-pad]
         ])

(run-sequencer 70 4 gen)
(stop)
