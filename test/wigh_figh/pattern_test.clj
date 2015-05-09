(ns wigh-figh.pattern-test
  (:require [clojure.test :refer :all]
            [wigh-figh.pattern :refer :all]))

(deftest pattern-parsing
  (testing "Assert trigger times are created correcly by the pattern generator"
    (are [x y] (= x y)
         []
         ((pattern [0]) 0)

         [0]
         ((pattern [1]) 0)

         [0 1/4 2/4 3/4]
         ((pattern [4]) 0)

         [0 1/4 2/4]
         ((pattern [2 1]) 0)

         [0 1/4 2/4]
         ((pattern [[1 1] 1]) 0)

         [0 1/4 3/8 2/4]
         ((pattern [[1 [1 1]] 1]) 0)))

  (testing "Test nil pattern"
    (are [x y] (= x y)
         []
         ((pattern nil) 0)))

  (testing "Random choice of patterns"
    (are [actual possibilities] (contains? possibilities actual)
         ((pattern #{1}) 0)
         #{[0]}

         ((pattern #{2 4}) 0)
         #{[0 2/4] [0/4 1/4 2/4 3/4]}))

  (testing "Expanding repeated patterns"
    (are [x y] (= x y)
         [[1] [1]]
         (pattern-modifier {:x 2 :p 1}) 

         [[1] [1] [1] [1]]
         (pattern-modifier {:x 4 :p 1}) 

         [[0 1] [0 1] [0 1]]
         (pattern-modifier {:x 3 :p [0 1]})

         [[[2] [2] [2] 1]]
         (pattern-modifier [{:x 3 :p 2} 1])

         [[1 1] [1 1]]
         (pattern-modifier {:x 2 :p {:x 2 :p 1}})

         ;; '([2] [2])
         ;; (pattern-modifier '({:x 2 :p 2}))
         ))
  (testing "Choice of pattern through measure-index"
    (are [x y] (= x y)
         [0]
         ((pattern '(1 0)) 0)

         []
         ((pattern '(1 0)) 1))))
