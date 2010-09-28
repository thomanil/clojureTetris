(ns clojureTetris.model)

;; FIXME Trim down shape matrices, and factor rotate funs to take any matrix dimensions (not just 4x4)

(def base-square-shape 	[[0 0 0 0]
			 [0 1 1 0]
			 [0 1 1 0]
			 [0 0 0 0]])

(def base-I-shape 	[[0 0 1 0]
			 [0 0 1 0]
			 [0 0 1 0]
			 [0 0 1 0]])

(def base-S-right-shape [[0 0 0 0]
			 [0 1 1 1]
			 [1 1 1 0]
			 [0 0 0 0]])

(def base-S-left-shape 	[[0 0 0 0]
			 [1 1 1 0]
			 [0 1 1 1]
			 [0 0 0 0]])

(def base-L-right-shape [[0 0 1 0]
			 [0 0 1 0]
			 [0 1 1 0]
			 [0 0 0 0]])

(def base-L-left-shape 	[[0 0 1 0]
			 [0 0 1 0]
			 [0 0 1 1]
			 [0 0 0 0]])

(def base-T-shape 	[[0 0 1 0]
			 [0 1 1 0]
			 [0 0 1 0]
			 [0 0 0 0]])

(defn random-shape []
  "Returns a random shape"
  (let [no-of-shapes 7
	dice-roll (rand-int no-of-shapes)]
    (cond (= dice-roll 0) base-square-shape
	  (= dice-roll 1)  base-I-shape
	  (= dice-roll 2)  base-S-right-shape
	  (= dice-roll 3)  base-S-left-shape
	  (= dice-roll 4)  base-L-right-shape
	  (= dice-roll 5)  base-L-left-shape
	  (= dice-roll 6)  base-T-shape)))

;; FIXME use the flatten fn from core/core 1.2 instead
(defn flatten  [x]
  "Takes any nested combination of sequential things (lists, vectors,
	  etc.) and returns their contents as a single, flat sequence.
	  (flatten nil) returns nil."
  (filter (complement sequential?)
	  (rest (tree-seq sequential? seq x))))

(defn flatten-matrix [matrix]
  "Turn matrix into flat list"
  (flatten matrix))

(defn unflatten-matrix [width flat-matrix]
  "Reconstruct matrix of given width from a flat list"
  (let [partitioned-matrix (partition width flat-matrix)]
    (vec(map vec partitioned-matrix))))

(defn xy [x y matrix]
  "Convenience method for accessing elements within two dimensional vectors"
  ((matrix y) x))

(defn new-matrix [x y]
  "Create vector (of vectors), with length x and width y"
  (let [row (vec (repeat x 0))]
    (vec (repeat y row))))

(defn matrix-height [matrix]
  "Determine height of given matrix"
  (count matrix))

(defn matrix-width [matrix]
  "Determine width of given matrix"
  (count (matrix 0)))

(defn matrix-contains [matrix value]
  "Returns true if matrix has at least one occurence of given value"
  (some  #{value} (flatten-matrix matrix)))

(defn map-matrix [matrix f]
  "Runs function f on each element in matrix, returning new matrix with results."
  (let [mapped-flat-matrix  (map f (flatten-matrix matrix))
	width (matrix-width matrix)
	partitioned-matrix (partition width mapped-flat-matrix)]
    (vec(map vec partitioned-matrix))))

(defn flat-pos [x y matrix]
  "Turn matrix x y coordinate into x position for corresponding flattened list"
  (+ x (* y (matrix-height matrix))))

(defn unflat-pos [i matrix]
  "Turn flat x coord into coordinate for supplied matrix"
  (let [y (quot i (matrix-width matrix))
	x (- i (* y (matrix-width matrix)))]
    [x y]))

(defn piece [x y shape]
  "Returns new piece structure with given coordinates and shape"
  {:x x :y y :shape shape})

(defn move [old-piece direction speed]
  "Returns new piece, with direction updated to move in specified direction"
  (let [x (old-piece :x) y (old-piece :y) shape (old-piece :shape)]
    (cond
     (= direction :left)(piece (- x speed) y shape)
     (= direction :right)(piece (+ x speed) y shape)
     (= direction :up)(piece x (- y speed) shape)
     (= direction :down)(piece x (+ y speed) shape))))

(defn game-over? [field-shape]
  "Determines if game is over based on shape of given field.
	If any elements in the top row are filled, the game should end."
  (let [top-row (field-shape 0)]
    (some #{1} top-row)))

(defn indexed [s] ;; FIXME replace with map-indexed from clojure/core 1.2
  "Returns a lazy sequence of [index, item] pairs, where items come
  from 's' and indexes count up from zero.
  (indexed '(a b c d))  =>  ([0 a] [1 b] [2 c] [3 d])"
  (map vector (iterate inc 0) s))

(defn map-matrix-indexed [matrix f]
  "Maps each element, supplying the item itself and
  the x and y coords for that element"
  (let [flat-indexed-matrix (indexed (flatten-matrix matrix))
	mapped-matrix (map
		       (fn [item]
			 (let [item-xy-pos (unflat-pos (item 0) matrix)
			       x (item-xy-pos 0)
			       y (item-xy-pos 1)
			       item-contents (xy x y matrix)]
			   (f (item 1) x y)
			   )) flat-indexed-matrix)]
    (unflatten-matrix (matrix-width matrix) mapped-matrix)))

(defn matrix-each-indexed [matrix f]
  "Run function for each element in matrix,
   supplying element and x and y coords"
  (map-matrix-indexed matrix (fn [state x y] (f state x y))))

(defn rotated-clockwise [m]
  "nth dest row = every nth element of each src row"
  [[(xy 0 3 m) (xy 0 2 m) (xy 0 1 m) (xy 0 0 m)]
   [(xy 1 3 m) (xy 1 2 m) (xy 1 1 m) (xy 1 0 m)]
   [(xy 2 3 m) (xy 2 2 m) (xy 2 1 m) (xy 2 0 m)]
   [(xy 3 3 m) (xy 3 2 m) (xy 3 1 m) (xy 3 0 m)]])

(defn outside-matrix? [x y matrix]
  "Convenience method for accessing elements within two dimensional vectors"
  (let [height (matrix-height matrix)
	width (matrix-width matrix)]
    (if (or (< x 0)
	    (< y 0) 
	    (> x (- width 1)) 
	    (> y (- height 1)))
      true
      false)))

(defn field-overlap? [piece field]
  "Checks if set any tiles in the piece shape overlaps corresponding set tiles in the enclosing field"
  (let [tile-overlap-mask (map-matrix-indexed (piece :shape)
					      (fn [tile-state tile-x tile-y]
						(let [field-x (+ (piece :x) tile-x)
						      field-y (+ (piece :y) tile-y)]
						  (when (and (= tile-state 1)
							     (not(outside-matrix? field-x field-y field)))
						    (+ (xy field-x field-y field) tile-state)))))]
    (some #{2} (flatten tile-overlap-mask))))

(defn outside-field? [piece field]
  "Checks if any part of the given piece is outside the playing field"
  (let [tiles-outside-mask (map-matrix-indexed (piece :shape)
					       (fn [tile-state tile-x tile-y]
						 (when (= tile-state 1)
						   (let [field-x (+ (piece :x) tile-x)
							 field-y (+ (piece :y) tile-y)]
						     (outside-matrix? field-x field-y field)))))]
    (some #{true} (flatten tiles-outside-mask))))

(defn merge-into [piece field]
  "Sticky collision with playing field, merge given piece shape into field in the pieces
	current position"
  (map-matrix-indexed field (fn [field-state field-x field-y]
			      (let [x-pos-in-piece (- field-x (piece :x))
				    y-pos-in-piece (- field-y (piece :y))
				    piece-matrix (piece :shape)]
				(if (and (>= x-pos-in-piece 0)
					 (>= y-pos-in-piece 0)
					 (< x-pos-in-piece (matrix-width piece-matrix))
					 (< y-pos-in-piece (matrix-height piece-matrix)))
				  (max field-state (xy x-pos-in-piece y-pos-in-piece piece-matrix))
				  field-state)))))

(defn will-stick? [piece direction speed field]
  "Checks if placing a piece matrix in xy pos within a (larger) field matrix yields a stuck piece.
	 True if one or several solid piece elements right above field bottom or any solid field elements"
  (if (= direction :down)
    (let [moved-piece (move piece :down speed)]
      (or (field-overlap? moved-piece field)
	  (outside-field? moved-piece field)))
    false))

(defn list-of-full-rows [field]
  "Returns list of y coordinates of the rows which are full in given field"
  (let [detected-rows (map (fn [row]
			     (let [sum-of-row  (apply + (row 1))]
			       (if (= sum-of-row (matrix-width field))
				 (row 0)
				 -1)))(indexed field))]
    (vec(remove #{-1} detected-rows))))

(defn clear-full-rows [field] 
  "Returns field which is cleared of full rows and shuffled accordingly
     each recursion:
       find first full row in current field
       drop that row, concat an empty row on top
       continue until no more full rows remain"
  (let [drop-nth-row (fn [n matrix]
		       (vec(concat (take n matrix)
				   (drop (+ n 1) matrix))))]
    (loop [shuffled-field field]
      (if (empty? (list-of-full-rows shuffled-field))
	shuffled-field
	(recur (vec(concat [(vec (repeat (matrix-width field) 0))]
			   (drop-nth-row
			    ((list-of-full-rows shuffled-field) 0)
			    shuffled-field))))))))