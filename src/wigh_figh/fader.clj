(ns wigh-figh.fader
  (:use [clojure.core]))

(defn- fader-ratio [fader-string]
  (/ (.indexOf fader-string "#") (dec (count fader-string))))

(defn fader
  "Takes a string of the shape \"-----#--------\" and returns the
  relative position of the '#' in the string, mapped to the interval
  defined by min and max. When called with four arguments, the third
  parameter controls the curve of the mapping."
  ([min max fader-string]
   (fader min max 1 fader-string))
  ([min max curve fader-string]
   (+ min (* (Math/pow (fader-ratio fader-string) curve) (- max min)))))
