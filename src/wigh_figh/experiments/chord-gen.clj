;; https://www.math.washington.edu/~morrow/336_09/papers/Ada.pdf
;Generate 3 signatures
;One starts with a long note
;First measure uses the long note signature
;First 4 measures use these signatures
;One signature gets duplicated
;Last 4 measures use the same sequence of signatures
;One or two mutations:
;Mutation can swap two signatures in the sequence
;Mutation can mutate a signature instance
;
;(record measure [signature start-tension end-tension])
;
;First measure starts with the start chord root
;Measures end either relaxed or tense
;Relaxed means root note or 7th (?)
(ns wigh-figh.experiments.chord-gen
  (:use [overtone.live]
        [wigh-figh.fader]))

(defsynth hit [note 40]
  (let [base-freq (midicps note)]
    (->> (sin-osc (* 40 base-freq))
         (* 1)
         (* (env-gen (env-perc 0 0.1)))
         (sin-osc (* 2 base-freq))
         (* 9)
         (* (env-gen (env-perc 0 1.0)))
         (sin-osc base-freq)
         (* (env-gen (env-perc 0 1.9) :action FREE))
         (#(lpf % (fader 200 20000 3 "-----------#------")))
         (out [0 1]))))

(doseq [note (to-interval (trfd))] (hit note))
(doseq [note (to-interval (P cmaj))] (hit note))
(doseq [note (to-interval (R cmaj))] (hit note))
(doseq [note (to-interval (L cmaj))] (hit note))

(defn one-of [] (nth [P R L #(I 6 %)] (rand-int 4)))

(def base-note 50)

(defn to-interval [notes] (map  #(+ base-note %) notes))

(defn T [n notes] (map #(mod (+ n %) 12) notes))
(defn I [n notes] (map #(mod (- n %) 12) notes))

(defn P [triad]
  (let [y1 (first triad)
        y3 (last triad)
        n (+ y1 y3)]
    (I n triad)))

(defn L [triad]
  (let [y2 (nth triad 1)
        y3 (last triad)
        n (+ y2 y3)]
    (I n triad)))

(defn R [triad]
  (let [y2 (nth triad 1)
        y1 (first triad)
        n (+ y2 y1)]
    (I n triad)))


(defrecord triad [root mode])
(defrecord uni-trf [sign trps])
(defn to-chord [triad]
  (let [base (if (= :major (:mode triad))
               [0 4 7]
               [0 3 7])]
    (map #(mod (+ (:root triad) %) 12) base)))

(def u (uni-trf. :pos [4 7]))
(def v (uni-trf. :neg [5 10]))
(defn ** [a b]
  (let [new-sign (if (= (:sign a) (:sign b)) :pos :neg)
        rhs (if (= :pos (:sign a)) (:trps b) (reverse (:trps b)))
        new-trps (map #(mod % 12) (map + (:trps a) rhs))]
    (uni-trf. new-sign new-trps)))

(defn inv [a]
  (let [rhs (if (= (:sign a) :pos) (:trps a) (reverse (:trps a)))
        new-trps (map #(mod (* -1 %) 12) rhs)]
    (uni-trf. (:sign a) new-trps)))

(** u v)
(** v u)
(** u (inv u))
(** v (inv v))

(defn opposite [mode]
  (if (= :major mode) :minor :major))

(defn act [trf triad]
  (let [t (if (= :major (:mode triad))
            (first (:trps trf))
            (last (:trps trf)))]
    (triad. (mod (+ t (:root triad)) 12)
            (if (= :pos (:sign trf))
              (:mode triad)
              (opposite (:mode triad))))))
(defn action [trf] #(act trf %))

(act (uni-trf. :neg [1 5]) (triad. 0 :minor))

(P [0 4 7])
(def p (uni-trf. :neg [0 0]))
(to-chord (act (uni-trf. :neg [0 0]) (triad. 0 :major)))
(L [0 4 7])
(def l (uni-trf. :neg [4 8]))
(to-chord (act (uni-trf. :neg [4 8]) (triad. 0 :major)))
(R [0 4 7])
(def r (uni-trf. :neg [9 3]))
(to-chord (act (uni-trf. :neg [9 3]) (triad. 0 :major)))

(def cur-chord (atom (triad. 0 :major)))
(def cur-index (atom 0))
(do
  (swap! cur-index #(mod (inc %) 4))
  (doseq [note (to-interval (to-chord (nth (map #(% (triad. 0 :major)) (chord-seq p u)) @cur-index)))] (hit note))
  @cur-index)

(defn chord-seq [g h]
  [identity (action g) (action (** g h)) (action (** g (** h (inv g))))])
