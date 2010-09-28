(ns clojureTetris.testbench
  (:use [penumbra.opengl])
  (:use [clojureTetris.interface] :reload-all)
  (:require [penumbra.app :as app]))





;;; Main display, render each frame

(defn display-loop [[delta time] state]
  (translate 0 0 -20)

 
  
  

  (app/repaint!))
  



(defn start-display-loop
  "Wire up and start the display loop
     - see interface.clj for used options, functions etc"
  [state]
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