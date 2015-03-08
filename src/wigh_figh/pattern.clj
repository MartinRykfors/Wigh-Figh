(ns wigh-figh.pattern)


(defn- pat-gen [start dur xs]
  (let [num-xs (count xs)
        step-length (/ dur num-xs)
        start-times (map #(+ start (* step-length %)) (range num-xs))
        x-times (map vector start-times (repeat num-xs step-length) xs)
        zs (map #(zipmap [:start :dur :pat] %) x-times)]
    (mapcat #(if (vector? (% :pat))
               (pat-gen (% :start) (% :dur) (% :pat))
               (cond (= 1 (% :pat)) (list (% :start))
                     (>= 0 (% :pat)) (list)
                     :else (pat-gen (% :start) (% :dur) (repeat (% :pat) 1)) )) zs)))

(defn pattern [pat] #(pat-gen 0 % pat))

(defn rot
  ([xs]
   (conj (vec (rest xs)) (first xs)))
  ([xs n]
   (nth (iterate rot xs) n)))

(defn rot-rec
  ([xs]
   (map #(if (vector? %) (vec (rot-rec %)) %) (vec (rot xs))))
  ([xs n]
   (nth (iterate rot-rec xs) n)))
