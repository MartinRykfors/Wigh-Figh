(ns wigh-figh.expression)

(defrecord track [pattern key])
(defrecord expression [tracks])
(defrecord palette [expressions action-map])
(defrecord kernel [expression-keys])

(defn generate-expression [actions max-length]
  (->>
   actions
   (map (fn [action]
          (->track (filter (fn [_] (= 0 (rand-int 2))) (range max-length)) action)))
   (->expression)))

(defn- merge-patterns [first second]
  (sort (seq (clojure.set/union (set first) (set second)))))

(defn- expand-tracks [tracks shift]
  (reduce
   (fn [acc {key :key pattern :pattern}]
     (let [shifted-pattern (map #(+ shift %) pattern)]
       (assoc acc key shifted-pattern))) {} tracks))

(defn express [{expression-keys :expression-keys} {expressions :expressions}]
  (first (reduce (fn [[total-pattern index] exp-key]
                   (if (= 0 exp-key)
                     [total-pattern (inc index)]
                     (-> (:tracks (nth expressions (dec exp-key)))
                         (expand-tracks index)
                         ((partial merge-with merge-patterns total-pattern))
                         ((fn [x] [x (inc index)])))))
                 [{} 0] expression-keys)))

