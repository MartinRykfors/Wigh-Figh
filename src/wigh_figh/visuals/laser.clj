(ns wigh-figh.visuals.laser
  (:require [quil.core :as q]))

(defonce sh (atom nil))
(def animation-state (atom {:start (float 0.0) :progress (float 0.0)}))

(defn setup []
  (q/frame-rate 60)
  (q/background 0)
  (reset! sh (q/load-shader (.getPath (clojure.java.io/resource "laser.frag"))))
  (.set @sh "size" (float (q/width)) (float (q/height))))

(defn animation-time []
  (float (+ (:start @animation-state) (min (float 0.9999) (:progress @animation-state)))))

(defn advance-time! []
  (swap! animation-state #(update-in % [:progress] (fn [x] (+ x (float 0.05))))))

(defn transition-state! []
  (swap! animation-state #(assoc-in % [:progress] (float 0.0)))
  (swap! animation-state #(assoc-in % [:start] (float (rand-int 1000)))))


(defn reload-shader! []
  (reset! sh (q/load-shader (.getPath (clojure.java.io/resource "laser.frag"))))
  (.set @sh "size" (float (q/width)) (float (q/height))))

(transition-state!)

(defn draw []
  ;(reload-shader!)
  (q/background 30 0 0)
  (q/rect 0 0 (q/width) (q/height))
  (.set @sh "time" (float (/ (q/millis) 1000)))
  (.set @sh "animTime" (animation-time))
  (q/shader @sh)
  (advance-time!))

(q/defsketch sketch
  :title "Lasers!!!"
  :setup setup
  :draw draw
  :renderer :p2d
  :size [800 600]) 
