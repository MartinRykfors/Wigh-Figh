(ns wigh-figh.pattern-test
  (:require [clojure.test :refer :all]
            [wigh-figh.pattern :refer :all]))

(deftest pattern-parsing
  (testing "Ints implement Pattern-unit as expected"
    (are [x y] (= x (trigger-times y 0 1))
         [0]
         1

         [0 1/2]
         2

         [0 1/3 2/3]
         3)
    (testing "Ints implement Pattern-expander"
      (are [x y] (= x (expand y 0))
           [1]
           1

           [5]
           5)))

  (testing "Sequences implement Pattern-unit properly"
    (are [expected-times input-seq] (= expected-times (trigger-times input-seq 0 1))
         [0]
         [1]

         [0 1/2]
         [1 1]

         [0 1/2 3/4]
         [1 2]

         [1/2 3/4]
         [0 2]))

  (testing "Sequences implement Pattern-expand properly"
    (are [expected-seq input-seq] (= expected-seq (expand input-seq 0))
         [[1]]
         [1]))

  (testing "Trigger-times of sequences are not affected by expansion when containing only seqs and ints"
    (are [x] (= ((pattern x) 0) ((pattern (expand x 0)) 0))
         [1]
         [2]
         [2 1]
         [[1 1] 2]
         [[1 2] 1 [[3] 2]])) ;; time to give test.check a spin?

  (testing "Assert trigger times are created correcly by the pattern generator"
    (are [x pat] (= x ((pattern pat) 0))
         []
         0

         [0]
         1

         [0 1/4 2/4 3/4]
         [4]

         [0 1/4 2/4]
         [2 1]

         [0 1/4 2/4]
         [[1 1] 1]

         [0 1/4 3/8 2/4]
         [[1 [1 1]] 1]))

  (testing "Test expanding repetitions"
    (are [x y] (= x (expand (apply ->rep y) 0))
         [1]
         [1 1]

         [1 1 1 1 1]
         [5 1]

         [[1 2] [1 2] [1 2]]
         [3 [1 2]]))

  (testing "Expanding a random choice yields one of the choices"
    (are [xs] (some #{(expand (->choice xs) 0)} (map #(expand % 0) xs)  )
         [1]
         [1 2]))
  (testing "Accessing indexed patterns by measure-index"
    (are [x y index] (= (expand x 0) (expand (->indexed y) index) )
         1
         [1] 0

         1
         [2 1] 1

         3
         [1 2 3] 2

         1
         [1 2 3] 3))
  (testing "Accessing nested indexed patterns"
    (are [x y index] (= (expand x 0) (expand y index))
         1
         (i (i 1 2) 3) 0

         3
         (i (i 1 2) 3) 1

         2
         (i (i 1 2) 3) 2

         3
         (i (i 1 2) 3) 3

         1
         (i (i 1 2) 3) 4
         ))
  (testing "Expanding empty things yields non-event"
    (are [x] (= x [[]])
         (expand (i) 0)

         (expand (c) 0))))

(defn- vec-to-note [v]
  {:start (nth v 0)
   :end (nth v 1)})

(defn- vecs-to-notes [vs] (map vec-to-note vs))

(deftest Length-patterns
  (testing "Specifying lengths of notes in patterns"
    (are [expected y start duration] (= (vecs-to-notes expected)
                                        (note-lengths y 1 start duration))
         [[0 1]]
         1 0 1

         [[1/2 1]]
         1/2 1/2 1

         [[0 0.5]]
         0.5 0 1

         [[0 1/2] [1/2 1]]
         [1/2 1/2] 0 1

         [[0 1/4] [1/4 1/2]]
         [1/4 1/4] 0 1/2

         [[1/2 3/4] [3/4 1]]
         [1/4 1/4] 1/2 1/2

         [[0 1/4] [3/4 1]]
         [1/4 0 0 1/4] 0 1))
  (testing "Specifying lengths in recursive patters"
    (are [expected y start duration] (= (vecs-to-notes expected)
                                        (note-lengths y 1 start duration))
         [[0 1/3] [1/2 3/4] [3/4 1]]
         [1/3 [1/4 1/4]] 0 1))
  (testing "Lengths are cut off before the next note"
    (are [expected pattern start duration] (= (vecs-to-notes expected)
                                              (note-lengths pattern 1 start duration))
         [[0 1/2] [1/2 1]]
         [1 1/2] 0 1))
  (testing "Lengths can be normalized"
    (are [expected pattern normalization start duration]
      (= (vecs-to-notes expected)
         (note-lengths pattern normalization start duration))
      [[0 1/2] [1/2 1]]
      [1 1] 2 0 1))) 
