(ns wigh-figh.visuals.shader-test
  (:require [quil.core :as q]))

(defonce sh (atom nil))

(defn setup []
  (q/frame-rate 60)
  (q/background 0)
  (reset! sh (q/load-shader (.getPath (clojure.java.io/resource "default.frag"))))
  (.set @sh "size" (float (q/width)) (float (q/height))))

(defn draw []
  ;(reset! sh (q/load-shader (.getPath (clojure.java.io/resource "default.frag"))))
  (q/background 30 0 0)
  (q/rect 0 0 (q/width) (q/height))
  (.set @sh "time" (float (/ (q/millis) 90)))
  (q/shader @sh))

(q/defsketch sketch
  :title "Shaders!!!"
  :setup setup
  :draw draw
  :renderer :p2d
  :size [400 400]) 
