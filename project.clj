(defproject clojureTetris "1.0.0-SNAPSHOT"
  :description "A simple implementation of Tetris in Clojure and OpenGL"
  :dependencies [[org.clojure/clojure "1.1.0"]
                 [org.clojure/clojure-contrib "1.1.0"]
		 [penumbra "0.5.0"]
                 [ant/ant-launcher "1.6.2"]
                 [org.apache.maven/maven-ant-tasks "2.0.10"]]
  :native-dependencies [[lwjgl "2.2.2"]]
  :dev-dependencies [[native-deps "1.0.0"]
		     [swank-clojure "1.2.1"]])