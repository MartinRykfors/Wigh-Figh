(ns wigh-figh.context
  (:use [overtone.live]))

(defmacro standard-groups []
  (def synth-g (group))
  (def mixer-group (group :after synth-g)))

(defsynth mixer [amp 1 bus 0 pos 0]
  (out [0 1] (pan2 (* amp (in bus)) pos)))

(defmacro declare-mixer [channel-name]
  (let [bus-name (str (name channel-name) "-bus")
        mixer-name (str (name channel-name) "-mixer")]
    (eval `(def ~(symbol bus-name)
             (audio-bus)))
    (eval `(def ~(symbol mixer-name)
             (mixer [:tail mixer-group] :bus (:id ~(symbol bus-name)))))))
