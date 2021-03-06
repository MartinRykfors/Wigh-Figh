(ns wigh-figh.visuals.metamonolith
  (:require [quil.core :as q]
            [wigh-figh.visuals.live-shader :as m]))

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
  (write-fps))

(q/defsketch metamonolith
  :title ""
  :size [800 600]
  :setup setup
  :renderer :opengl
  :middleware [m/live-shader]
  :shader-file-name "metamonolith.frag"
  :init-uniforms init-uniforms
  :update-uniforms update-uniforms)

(reset! sketch-atom metamonolith)
