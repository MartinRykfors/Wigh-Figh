(ns wigh-figh.expression-test
  (:require [clojure.test :refer :all]
            [wigh-figh.expression :refer :all]))

(deftest expression-generation-tests
  (testing "a randomly generated expression has the expected number of tracks"
    (are [expected-count actions]
        (= expected-count (count (:tracks (generate-expression actions 2))))
        2
        [:kick :snare]

        3
        [:kick :snare :hihat]
        ))
  (testing "an expression is a collection of tracks"
    (are [actions] (seq? (:tracks (generate-expression actions 2)))
      [:kick]
      [:kick :snare]
      ))
  (testing "every generated track has a pattern"
    (are [actions] (every? seq? (map :pattern (:tracks (generate-expression actions 2))))
      [:kick]
      [:kick :snare]
      ))
  (testing "every generated track has one of the actions"
    (are [actions] (= actions (map :key (:tracks (generate-expression actions 2))))
      [:kick]
      [:kick :snare]
      [:kick :snare :hihat]
      ))
  (testing "all tracks have patterns that are are either empty or less than max-length"
    (are [length]
        (every?
         (fn [track] (or
                      (empty? (:pattern track))
                      (< (apply max (:pattern track)) length)))
         (:tracks (generate-expression [:kick :snare :hihat] length)))
      1 2 3 4 8))

  (let [expression1 (->expression [(->track [0 1] :kick) (->track [0] :snare)])
        expression2 (->expression [(->track [1 2] :kick) (->track [2] :snare)])
        palette (->palette [expression1 expression2] {:kick :kick-val :snare :snare-val})]
    (testing "zero-only kernel yields empty total expression"
      (are [expected kernel] (= expected (express kernel palette 16))
        {}
        (->kernel [0])
        ))
    (testing "single-unit kernel will replicate the expression"
      (are [expected kernel] (= expected (express kernel {:expressions [expression1]} 2))
        {:kick [0 1] :snare [0]}
        (->kernel [1])
        ))
    (testing "the kernel can shift the expression"
      (are [expected kernel] (= expected (express kernel {:expressions [expression1]} 2))
        {:kick [1 2] :snare [1]}
        (->kernel [0 1])
        ))
    (testing "it chooses which expression to insert based on the kernel"
      (are [expected kernel] (= expected (express kernel palette 2))
        {:kick [1 2] :snare [2]}
        (->kernel [2])
        ))
    (testing "it merges multiple expressions"
      (are [expected kernel] (= expected (express kernel palette 2))
        {:kick [0 1 2 3] :snare [0 3]}
        (->kernel [1 2])
        ))))
