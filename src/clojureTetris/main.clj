(ns clojureTetris.main
  (:use [penumbra.opengl])
  (:use [clojureTetris.interface] :reload-all)
  (:use [clojureTetris.model] :reload-all)
  (:require [penumbra.app :as app]))

(def piece-state (atom (piece 0 0 (random-shape))))
(def field-state (atom (new-matrix 10 15)))
(def timeof-last-down-move (atom 0))
(def timeof-last-player-move (atom 0))


(defn set-scene []
  "Set basic viewpoint"
  (translate -5 8 -15)  ;; TODO (demo) make viewpoint dynamic based on field size
  (rotate 180 0 1 0)    ;; Twist around to get expected view while rendering
  (rotate 10 0 1 0)     ;; Then tweak it slightly to emphasize 3d effect
  (rotate -10 1 0 0))


;;FIXME very bad performance..
(defn render-game [state]
  "Draws the current game state on screen"
  (set-scene)

  (mouse-scene-rotation state)

  (color 0 0.2 0)
  (draw-matrix [(vec (repeat (matrix-width @field-state) 1))]
	       0
	       (matrix-height @field-state))
  
  (color 0 0.6 0)
  (draw-matrix @field-state 0 0)
  
  (color 0 0.9 0)
  (draw-matrix (@piece-state :shape)
	       (@piece-state :x)
	       (@piece-state :y)))

(defn move-piece-with-sideeffects [rotation direction speed]
  "Attempt to move the piece, trigger any side effects in game state"
  (when (not (nil? rotation)) ;; Piece can rotate if it doesn't collide with anything
	(let [rotated-shape (rotated-clockwise (@piece-state :shape))
	      rotated-piece-state (piece (@piece-state :x)
					 (@piece-state :y)
					 rotated-shape) 
	      outside? (outside-field? rotated-piece-state @field-state)
	      overlap? (field-overlap? rotated-piece-state @field-state)]
	  (when (and (not outside?) (not overlap?))
		(reset! piece-state rotated-piece-state))))

  (when (not(nil? direction)) ;; Let player move, stick piece if it touches down on floor or other pieces.
	(let [new-piece-state (move @piece-state direction speed)
	      going-outside-field? (outside-field? new-piece-state @field-state)
	      piece-sticks? (will-stick? @piece-state direction speed @field-state)]
	  
	  (if piece-sticks?
	    (do	(reset! field-state (merge-into @piece-state @field-state))
		(reset! piece-state (piece 0 0 (random-shape))) )
	    
	    (when (and (not going-outside-field?)
		       (not(field-overlap? new-piece-state @field-state)))
		  (reset! piece-state new-piece-state))))))

(defn update-game-state [time state]
  "Progress game and handle global game state"
  ;; Force piece downward
  (let [secs-between-falls 0.5 
	secs-since-last (- time @timeof-last-down-move)]
    (when (> secs-since-last secs-between-falls)
	  (do (move-piece-with-sideeffects nil :down 1)
	      (reset! timeof-last-down-move time))))

  ;; Clear any full rows
  (reset! field-state (clear-full-rows @field-state))

  ;; Handle game over state, restart game
  (when (game-over? @field-state)
	(reset! field-state (new-matrix (matrix-width @field-state)
					(matrix-height @field-state)))))

(defn handle-player-input [time state] 
  "Handle player-driven movement and actions"
  (let [secs-between-player-moves 0.05
	secs-since-last (- time @timeof-last-player-move)]
    (when (> secs-since-last secs-between-player-moves)
	  (do (move-piece-with-sideeffects (state :rotation) (state :direction) 1)
	      (reset! timeof-last-player-move time)))))

(defn display-loop [[delta time] state]
  "The main game loop tracks user input, and is partially responsible
   for game progression. Other threads can also change and update the game state"
  (render-game state)
  (update-game-state time state)
  (handle-player-input time state)
  (app/repaint!))

(defn start-display-loop [state]
  "Wire up and start the display loop
     - see interface.clj for used options, functions etc"
  (let [display-proxy (fn [& args]
			(apply display-loop args))
	app-options {:reshape reshape
		     :display display-proxy
		     :mouse-drag mouse-drag
		     :key-press key-press
		     :key-release key-release
		     :update update
		     :light true
		     :init init}]
    (app/start app-options state)))

(start-display-loop {:rot-x 0, :rot-y 0 :second 0})