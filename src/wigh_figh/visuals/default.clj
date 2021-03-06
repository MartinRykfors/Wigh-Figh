(ns wigh-figh.visuals.default ;; change ns
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

(q/defsketch default ;; change sketch name
  :title ""
  :size [600 600]
  :setup setup
  :renderer :opengl
  :middleware [m/live-shader]
  :shader-file-name "default.frag" ;; change shader file
  :init-uniforms init-uniforms
  :update-uniforms update-uniforms)

(reset! sketch-atom default) ;; change to sketch name
