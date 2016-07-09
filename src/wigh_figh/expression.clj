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

(defn express [{expression-keys :expression-keys} {expressions :expressions} num-steps]
  (nth (reduce (fn [[index total-map] exp-key]
                 (if (= 0 exp-key)
                   [(inc index) total-map]
                   (let [es (nth expressions (dec exp-key))
                         local-res (reduce
                                    (fn [acc {key :key pattern :pattern}]
                                      (let [shifted-pattern (map #(+ index %) pattern)]
                                        (assoc acc key shifted-pattern))) {} (:tracks es))
                         merged (merge-with merge-patterns local-res total-map)]
                     [(inc index) merged]))) [0 {}] expression-keys) 1))
