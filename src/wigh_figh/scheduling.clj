(ns wigh-figh.scheduling)

(defmulti schedule
  (fn [type measure-index & rest] type))
