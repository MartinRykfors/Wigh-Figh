(ns wigh-figh.visuals.gridshift
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def state (atom
            {:grid [
                    [0 1 0 1 0 1]
                    [1 0 1 0 1 0]
                    [0 1 0 1 0 1]
                    [1 0 1 0 1 0]
                    [0 1 0 1 0 1]
                    [1 0 1 0 1 0]
                    ]
             :dir :horizontal
             :shift [0 0 0 0 0 0]
             :time 0}))

(defn- rotate [row dir]
  (cond (= 0 dir) row
        (= 1 dir) (vec (cons (last row) (butlast row)))
        (= -1 dir) (conj (vec (rest row)) (first row))))

(defn- transpose [grid]
  (vec (for [j (range (count (first grid)))]
         (vec (map #(get-in grid [% j]) (range (count grid)))))))

(defn- rotate-rows [grid shift]
  (vec (map rotate grid shift)))

(defn- transition-grid [grid dir shift]
  (if (= :horizontal dir)
    (rotate-rows grid shift)
    (transpose (rotate-rows (transpose grid) shift))))

(defn- random-dir [] (if (= 0 (rand-int 2)) :vertical :horizontal))

(defn- random-shift [grid dir]
  (let [num-picks (if (= :horizontal dir) (count grid) (count (first grid)))]
    (vec (repeatedly num-picks #(- (rand-int 3) 1)))))

(defn transition-state! []
  (let [{o-dir :dir o-shift :shift o-grid :grid} @state
        new-grid (transition-grid o-grid o-dir o-shift)
        new-time 0.0
        new-dir (random-dir)
        new-shift (random-shift new-grid new-dir)]
    (reset! state {:grid new-grid :time new-time :dir new-dir :shift new-shift})))

(defn setup []
  (q/frame-rate 60)
  (q/background 0))

(defn update []
  (swap! state #(update-in % [:time] (fn [t] (min 1 (+ 0.01 t))))))

(defn- displace [t] (- 1 (q/exp (* -50 t )) ))

(defn- displacement [i j t]
  (let [{dir :dir shift :shift} @state]
    (if (= dir :vertical)
      (if (or (= -1 j) (= (count shift) j))
        [0 0]
        [(* (displace t) (get shift j)) 0])
      (if (or (= -1 i) (= (count shift) i))
        [0 0]
        [0 (* (displace t) (get shift i))]))))

(defn- pad [vect] (cons (last vect) (conj vect (first vect))))

(defn draw []
  (update)
  (q/no-stroke)
  (q/fill 255)
  (let [{grid :grid dir :dir shift :shift time :time} @state]
    (doseq [[row i] (map vector (pad grid) (range -1 (inc (count grid))))]
      (doseq [[tile j] (map vector (pad row) (range -1 (inc (count row))))]
        (let [tile-width (/ (q/width) (count row))
              tile-height (/ (q/height) (count grid))
              disp (displacement i j time)]
          (if (zero? tile)
            (q/fill 0x01 0x1f 0x34)
            (q/fill 0x37 0x7c 0xad))
          (q/rect (* tile-width (+ j (get disp 1)))
                  (* tile-height (+ i (get disp 0)))
                  tile-width
                  tile-height))))))

(q/defsketch sketch
  :title "Grid shift"
  :setup setup
  :draw draw
  :size [400 400])
