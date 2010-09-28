(ns clojureTetris.model-test
  (:use [clojureTetris.model] :reload-all)
  (:use [clojure.test]))


(deftest rotate-shape
  (let [start              [[0 0 1 0]
			    [0 0 1 0]
			    [0 1 1 0]
			    [0 0 0 0]]

	rotated-once       [[0 0 0 0]
			    [0 1 0 0]
			    [0 1 1 1]
			    [0 0 0 0]]

	rotated-again      [[0 0 0 0]
			    [0 1 1 0]
			    [0 1 0 0]
			    [0 1 0 0]]]
    (is (= rotated-once (rotated-clockwise start)))
    (is (= rotated-again (rotated-clockwise rotated-once)))))


(deftest create-new-matrix
  (let [expected-3x3  	[[0 0 0]
			 [0 0 0]
			 [0 0 0]]
	expected-1x1 [[0]]
	expected-5x2 	[[0 0 0 0 0]
			 [0 0 0 0 0]]]
    (is (= expected-1x1 (new-matrix 1 1)))
    (is (= expected-3x3 (new-matrix 3 3)))
    (is (= expected-5x2 (new-matrix 5 2)))))

(deftest matrix-dimensions
  (let [model (new-matrix 2 3)]
    (is(= 2 (matrix-width model)))
    (is(= 3 (matrix-height model)))))

(deftest matrix-contents
  (let [container 		[[0 :t]
				 [0 0]]
	empty-container 	[[0 0]
				 [0 0]]]
    (is (matrix-contains container :t))
    (is (not(matrix-contains empty-container :t)))))

(deftest matrix-mapping
  (let [seed [[0 1]
	      [3 2]]
	expected [[1 2]
		  [4 3]]
	actual (map-matrix seed inc)]
    (is(= expected actual))))

(deftest piece-creation
  (is(= {:x 0 :y 0 :shape base-square-shape} (piece 0 0 base-square-shape))))

(deftest piece-position-updates
  (let [origin (piece 0 0 base-square-shape)
	left (piece -1 0 base-square-shape)
	right (piece 1 0 base-square-shape)
	up (piece 0 -1 base-square-shape)
	down (piece 0 1 base-square-shape)]
    (is(=(left (move origin :left 1))))
    (is(=(right (move origin :right 1))))
    (is(=(up (move origin :up 1))))
    (is(=(down (move origin :down 1))))))

(deftest determine-game-over
  (let [full-field [[0 1]
		    [1 1]]
	non-full-field 	[[0 0]
			 [1 1]]]
    (is(game-over? full-field))
    (is(not(game-over? non-full-field)))))

(deftest matrix-sequence-transformation
  (is(= 3 (flat-pos 1 1 (new-matrix 2 2))))
  (is(= 9 (flat-pos 1 2 (new-matrix 4 4))))
  (is(= 13 (flat-pos 3 2 (new-matrix 5 5))))
  (is(= [0 0] (unflat-pos 0 (new-matrix 2 2))))
  (is(= [1 0] (unflat-pos 1 (new-matrix 2 2))))
  (is(= [0 1] (unflat-pos 2 (new-matrix 2 2))))
  (is(= [1 1] (unflat-pos 3 (new-matrix 2 2))))
  (is(= [1 2] (unflat-pos 9 (new-matrix 4 4))))
  (is(= [3 2] (unflat-pos 13 (new-matrix 5 5)))))

(deftest list-to-matrix
  (let [flat-matrix (flatten base-square-shape)]
    (is(= base-square-shape (unflatten-matrix (matrix-width base-square-shape) flat-matrix)))))

(deftest matrix-to-list
  (let [flat-matrix (flatten base-square-shape)]
    (is(= flat-matrix (flatten-matrix base-square-shape)))))

(deftest field-overlaps
  (let [piece (piece 0 0 [[0 1]])
	field 	[[0 0 0 1]
		 [0 0 0 1]
		 [0 0 0 1]
		 [0 1 1 1]]]
    (is(not(field-overlap? (move piece :right 1) field)))
    (is(not(field-overlap? (move piece :down 1) field)))
    (is(not(field-overlap? (move piece :down 2) field)))
    (is(field-overlap? (move piece :right 2) field))
    (is(field-overlap? (move piece :down 3) field))))

(deftest outside-field-boundaries
  (let [field  [[0 0 0 0]
		[0 0 0 0]
		[0 0 0 0]
		[0 0 0 0]]]
    (is(outside-field? (piece -1 0 [[1 0]]) field))
    (is(outside-field? (piece 0 -1 [[1 0]]) field))
    (is(outside-field? (piece 4 0 [[1 0]]) field))
    (is(outside-field? (piece 0 4 [[1 0]]) field))
    (is(not(outside-field? (piece 0 0 [[1 0]]) field)))
    (is(not(outside-field? (piece 1 2 [[1 0]]) field)))))

(deftest merge-piece-into-field
  (let [piece (piece 1 1 [[1 1]])
	field [[0 0 0 0]
	       [0 0 0 0]
	       [0 0 0 0]
	       [0 0 0 0]]
	after-merge [[0 0 0 0]
		     [0 1 1 0]
		     [0 0 0 0]
		     [0 0 0 0]]]
    (is (= after-merge (merge-into piece field)))))

(deftest piece-stickiness
  (let [piece (piece 0 0 [[1 1]])
	field [[0 0 0 0]
	       [0 0 0 0]
	       [0 0 0 0]
	       [1 1 1 1]]]
	(is (not(will-stick? piece :right 5 field)))
	(is (not(will-stick? piece :up 5 field)))
	(is (not(will-stick? piece :left 5 field)))
	(is (not(will-stick? piece :down 1 field)))
	(is (not(will-stick? piece :down 2 field)))
	(is (will-stick? piece :down 3 field))))

(deftest full-rows
  (let [field [[0 0 1 0]
	       [1 1 1 1]
	       [1 1 1 1]
	       [1 1 1 1]]
	cleared-field [[0 0 0 0]
		       [0 0 0 0]
		       [0 0 0 0]
		       [0 0 1 0]]]
    (is (= (list-of-full-rows field) [1 2 3]))
    (is (= (clear-full-rows field) cleared-field))))

(deftest test-2d-indexed-map
  (let [field  [[0 0 0]
		[0 0 0]
		[0 0 0]]
	field-xy-sum-mapped [[0 1 2]
			     [1 2 3]
			     [2 3 4]]
	map-function (fn [element x y](+ x y))]
    (is (= field-xy-sum-mapped (map-matrix-indexed field map-function)))))

;(run-tests)



