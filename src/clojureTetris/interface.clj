(ns clojureTetris.interface
  (:use [clojureTetris.model] :reload-all)
  (:use [penumbra.opengl])
  (:require [penumbra.app :as app]))

;; Draw operations

(defn quad [width height]
  (push-matrix
   (translate -0.5 -0.5 0.5)
   (normal 0 0 -1)
   (vertex width height 0)
   (vertex 0 height 0)
   (vertex 0 0 0)
   (vertex width 0 0)))

(defn cube [thickness height]
  (draw-quads
   (dotimes [_ 4]
     (rotate 90 0 1 0) 
     (quad thickness height))
   (rotate 90 1 0 0)
   (quad thickness thickness)
   (translate 0 0 (- height))
   (quad thickness thickness)))

(defn draw-matrix [matrix offset-x offset-y]
  "Draws 2d matrix as a field of cubes starting at offset xy pos"
  (push-matrix
   (translate (- offset-x) (-  offset-y) 0)
   (matrix-each-indexed matrix (fn [state x y]
				 (push-matrix
				  (translate (- x) (- y) 0)
				  (cond (> state 0)
					(cube 1 1)))))))

;;; Scene init, resize handling, updates, input

(defn mouse-scene-rotation [state]
  (rotate (:rot-x state) 1 0 0)
  (rotate (:rot-y state) 0 1 0))

(defn progress-constant-rotation [state time]
  (rotate (rem (* 90 time) 360) 1 2 3))

(def *light-ambient* [0.5 0.5 0.5 1])
(def *light-diffuse* [1 1 1 1])
(def *light-position* [0 0 2 1])

(defn init [state]
  (app/vsync! true)
  (enable :depth-test)
  (depth-test :lequal)
  (shade-model :smooth)
  (clear-color 0 0 0 0.5)
  (hint :perspective-correction-hint :nicest)
  (light 1
         :ambient *light-ambient*
         :diffuse *light-diffuse*
         :position *light-position*)
  (enable :light1)
  (enable :lighting)
  (enable :color-material)
  state)

(defn reshape [[x y width height] state]
  (frustum-view 60.0 (/ (double width) height) 1.0 100.0)
  (load-identity)
  state)

(defn update [[delta time] state]
  (assoc state
    :second (+ (:second state) delta)))

;; Input handling

(defn key-press [key state]
  (condp = key
      :f1    (let [state (update-in state [:fullscreen] #(not %))]
	       (app/fullscreen! (:fullscreen state))
	       state)
      " "    (assoc state :rotation true)
      :left  (assoc state :direction :left)
      :right (assoc state :direction :right)
      ;; :up    (assoc state :direction :up) ;Can't move up in Tetris!
      :down  (assoc state :direction :down)
      state))

(defn key-release [key state]
  (condp = key
      " "  (dissoc state :rotation)
      :left  (dissoc state :direction)
      :right (dissoc state :direction)
      :up    (dissoc state :direction)
      :down  (dissoc state :direction)
      state))

(defn mouse-drag [[dx dy] [x y] button state]
  (assoc state
    :rot-x (+ (:rot-x state) dy)
    :rot-y (+ (:rot-y state) dx)))

;; Effects TODO:

;; Thread: change dimensions of pieces  and/or field in a pulse
;; Thread: change colors of pieces in pulse/pattern
;; Thread: run misc particles/filter effects continously
;; Event: Row clear
;; Event: Merge shapes
;; Event: Game over
;; Event: Piece rotation
;; Event: Field rotation

;; GFX iteration 1: Effects based on time and game events only
;; GFX iteration 2: Effects visualizing background music pattern


