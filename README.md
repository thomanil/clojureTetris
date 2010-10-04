# clojureTetris

<img src="http://github.com/downloads/thomanil/clojureTetris/screenshot.jpg" alt="clojureTetris screenshot" />

## DESCRIPTION:

A simple Tetris implementation used to learn basic Clojure,
interactive programming and OpenGl. Uses Penumbra
(http://github.com/ztellman/penumbra) to interface with OpenGL.

See more info in this blog post: http://messynotebook.com/?p=1496

## REQUISITES:

Install Leiningen (http://github.com/technomancy/leiningen) if you
don't have it already.

To run it using the approach given below, install Slime in Emacs.

## INSTALL:

Install project dependencies in root dir by running first 'lein deps', then
'lein native-deps'.

Then run test suite with 'lein test'.

## RUNNING IT FROM EMACS:

Start repl process with 'lein swank' in root dir.

Connect to swank process from Emacs with
'<slime-connect>' command.  Then evaluate the entire
src/clojureTetris/main.clj file to start playing Tetris.

## LICENSE:

(The MIT License)

Copyright (c) 2010 clojureTetris

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
'Software'), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.