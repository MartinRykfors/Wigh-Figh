(ns wigh-figh.pattern-test
  (:require [clojure.test :refer :all]
            [wigh-figh.pattern :refer :all]))

(deftest pattern-parsing
  (testing "Assert trigger times are created correcly by the pattern generator"
    (are [x y] (= x y)
         []
         ((pattern [0]) 1)

         [0]
         ((pattern [1]) 1)

         [0 1 2 3]
         ((pattern [4]) 4)

         [0 1 2]
         ((pattern [2 1]) 4)

         [0 1 2]
         ((pattern [[1 1] 1]) 4)

         [0 1 3/2 2]
         ((pattern [[1 [1 1]] 1]) 4)))

  (testing "Test nil pattern"
    (are [x y] (= x y)
         []
         ((pattern nil) 0)))

  (testing "Random choice of patterns"
    (are [actual possibilities] (contains? possibilities actual)
         ((pattern #{1}) 1)
         #{[0]}

         ((pattern #{2 4}) 4)
         #{[0 2] [0 1 2 3]}))
  (testing "Repeated patterns"
    (are [x y] (= x y)
         [0 1]
         ((pattern {:x 2 :p 1}) 2)

         [0 1 2 3]
         ((pattern {:x 4 :p 1}) 4)

         ((pattern [[0 1] [0 1] [0 1]]) 4)
         ((pattern {:x 3 :p [0 1]}) 4))))

(deftest pattern-mutating
  (testing "Rotation"
    (are [x y] (= x y)
         [1]
         (rot [1])

         [2 1]
         (rot [1 2])

         [[1 1] 1]
         (rot [1 [1 1]])))

  (testing "Many-step rotation"
    (are [x y] (= x y)
         [0 1 1]
         (rot [1 1 0] 2)))

  (testing "Recursive rotation"
    (are [x y] (= x y)
         [[0 1]]
         (rot-rec [[1 0]])

         [[0 1] 1]
         (rot-rec [1 [1 0]])

         [[1 [0 1]] 1 [0 1]]
         (rot-rec [[1 0] [[1 0] 1] 1])))

  (testing "Many-step recursive rotation"
    (are [x y] (= x y)
         [[3 [0 3]] 2 [1 0 0]]
         (rot-rec [2 [0 0 1] [3 [0 3]]] 2))))
