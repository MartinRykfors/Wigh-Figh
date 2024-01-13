(ns wigh-figh.experiments.m-r-kick
  (:require [overtone.live :refer :all]))

(demo 20 (sin-osc (+ 400 (* 300 (sin-osc 1)))))
(demo 20 (sin-osc (sin-osc 1 0 300 400)))

(defsynth thing []
  (out 0 (sin-osc (sin-osc 1 0 300 400))))
(defsynth thing2 []
  (out 0 (sin-osc (+ 400 (* 300 (sin-osc 1))))))

(def t (thing))
(def x (thing2))


(demo (sin-osc 800 0.0 0.0 0.0))
(stop-all)
(pp-node-tree)
(graphviz)
(grain-buf)

((a b c))
(free a)
