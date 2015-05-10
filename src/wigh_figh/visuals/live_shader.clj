(ns wigh-figh.visuals.live-shader
  (:require [quil.core :as q]))

(defonce ^{:private true} shader-state (atom {:timestamp 0 :error nil}))
(defonce ^{:private true} shader-file (atom nil))
(defonce ^{:private true} shader-atom (atom nil))

(defn- shader-file-handle [file-name]
  (->> file-name
       (clojure.java.io/resource)
       (clojure.java.io/file)))

(defn- current-timestamp []
  (.lastModified @shader-file))

(defn- load-shader [init-uniforms]
  (swap! shader-state #(merge % {:timestamp (current-timestamp) :error nil}))
  (try
    (do
      (reset! shader-atom (q/load-shader (.getPath @shader-file)))
      (init-uniforms @shader-atom))
    (catch Exception e
      (do
        (swap! shader-state #(assoc % :error (.getMessage e)))))))

(defn- should-reload? []
  (> (current-timestamp) (:timestamp @shader-state)))

(defn- reload-shader! [init-uniforms]
  (when (and (= (mod (q/frame-count) 50) 0)
             (should-reload?))
    (load-shader init-uniforms)))

(defn- draw-shader [init-uniforms]
  (q/no-stroke)
  (reload-shader! init-uniforms)
  (if (:error @shader-state)
    (do
      (q/reset-shader)
      (q/background 0)
      (q/fill 255)
      (q/text (:error @shader-state) 30 30))
    (do
      (q/shader @shader-atom)
      (q/rect 0 0 (q/width) (q/height)))))

(defn live-shader [options]
  (let [setup (:setup options (fn []))
        file-name (:shader-file-name options)
        init-uniforms (:init-uniforms options)
        update-uniforms (:update-uniforms options)]
    (reset! shader-file (shader-file-handle file-name))
    (let [updated-setup #(do
                           (setup)
                           (load-shader init-uniforms))
          updated-draw #(do
                          (draw-shader init-uniforms)
                          (update-uniforms @shader-atom))]
      (merge options {:draw updated-draw :setup updated-setup}))))
