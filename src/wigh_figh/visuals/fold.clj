(ns wigh-figh.visuals.fold
  (:require [quil.core :as q]
            [wigh-figh.visuals.live-shader :as m]))

(def animation-state (atom {:start 0.0 :progress 0.0}))

(def background-type (atom (float 2.0)))

(defn reset-animation! []
  (reset! animation-state {:start (float (rand-int 1000)) :progress (float 0.0)}))

(defn step [t]
  (min (float 0.9) (+ t (/ 1 60))))

(defn update-animation! []
  (swap! animation-state #(update-in % [:progress] step)))

(defn set-background! [type]
  (let [index (case type
                :digital (float 0.0)
                :static (float 1.0)
                :horizon (float 2.0))]
    (reset! background-type index)))

(set-background! :digital)
(set-background! :static)
(set-background! :horizon)

(defn ease [x]
  (->> x
       (* -9)
       (q/exp)
       (- 1)))

(defn animation-time []
  (float (+ (:start @animation-state) (ease (:progress @animation-state)))))

(def sketch-atom (atom nil))

(defn setup []
  (q/frame-rate 60)) 

(defn init-uniforms [shader]
  (.set shader "size" (float (q/width)) (float (q/height))))

(defn write-fps []
  (when (= 0 (mod (q/frame-count) 50))
    (.setTitle (.frame @sketch-atom) (str (q/current-frame-rate)))))

(defn update-uniforms [shader]
  (.set shader "time" (float (/ (q/millis) 1000)))
  (.set shader "atime" (animation-time))
  (.set shader "stat" (float @background-type))
  (update-animation!)
  (when (= 0 (mod (q/frame-count) 60))
    (reset-animation!))
  (write-fps))

(q/defsketch fold
  :title ""
  :size [800 800]
  :setup setup
  :renderer :opengl
  :middleware [m/live-shader]
  :shader-file-name "fold.frag"
  :init-uniforms init-uniforms
  :update-uniforms update-uniforms)

(reset! sketch-atom fold)
