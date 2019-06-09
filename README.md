# clprojector-proj

CLPROJECTOR is a Leiningen template for Web projects that use browser scripting to do 3 dimensional rendering. It provides functions to translate, rotate, and project 3D coordinates onto the 2D raster graphics interface offered by the browser. There are also functions to draw lines and polygons from 3D coordinates, and to output text and raster images to the display.

The Web infrastructure used to serve up the app and build / load all of its Clojure(Script) code comes from the Porpus template (https://github.com/beau1399/porpus). If you have questions about the Web server used, where styles and whatnot should be stored, how incremental reloading during development works, etc., you are advised to take a look at the Porpus README.md file.

## Rationale

The code provided here is built around the HTML5 **canvas** control. This is in some way behind the state-of-the-art, in that most browsers now support WebGL. However, I came into this project with a lot of preexisting **canvas**-driven code and I wanted to generalize it into a library and template so that I could continue to use it for those problems where the performance of **canvas** is adequate. To write a library like this from the ground up using WebGL would have been a lengthier effort.

## Usage

This section shows how to download the CLPROJECTOR template, install it, use it to generate a runable 3D Web application, and then view the demo that comes out-of-the-box with each such Web app generated. The first step is to clone the repository. This should be done from the root folder under which you keep your various Git-sourced code folders:

$ git clone https://github.com/beau1399/clprojector-template.git

Then Leiningen must be used to install the template:

$ cd clprojector

$ lein install

Move back out to your top-level code folder and instantiate a CLPROJECTOR application:

$ cd ..

$ lein new clprojector-proj projtest

Finally, use Figwheel to run a local Web server:

$ cd projtest

$ lein figwheel

Now you can browse to http://localhost:3449/ to view the demo in action:

![CLPROJECTOR demo](https://raw.githubusercontent.com/beau1399/clprojector-template/master/clprojector.png)

## The Demo

The demo renders several different visual features

1. A background of vertical red lines
2. A static ClojureScript logo near the bottom right corner of the display
3. A two-color, semi-transparent cube rotating about its own "Y" axis that moves away from the viewer along the "Z" axis.
4. A wireframe cube that orbits around an axis at X=0, Z=2
5. Static text near the top left corner of the display reading "CLProjector Demo"

The code responsible for rendering these feature resides under the project root at **src/cljs/clprojector/core.cljs**. At the top of this file, some necessary code is referenced:

``` 
(ns clprojtst.core
  (:require
   [clprojtst.draw :as cld]
   [reitit.frontend :as reitit]))
```
It is from the **draw** sub-namespace of the main project namespace, aliased as "cld," that the CLPROJECTOR rendering functions will come.

The executable portion of **core.cljs** begins thus:

````
(defn ^:export scene []
  (let[ctx (cld/get-context (cld/get-canvas))]
   (def angle (atom 0))    
    (def place (atom 0))
    (js/setInterval     
     (fn []
       (cld/cls ctx 0 0 0)         
````

The identifier "scene" is expected by the CLPROJECTOR infrastructure. It will be called upon page load and must make a call to JavaScripts **js/setInterval** function to create an animation loop. This is seen near the bottom of the snippet above. Before that happens, calls to **cld/get-context** and **cld/get-canvas** obtain variable **ctx**, an HTML5 **context** object, which is passed to the CLPROJECTOR rendering functions.

Atoms **angle** and **place** will vary over time and define the state of the model. The first of these is a number in radians which is overloaded to hold both 1) the angle by which the two-color cube is rotated about its own "Y" axis and 2) the position of the second, wireframe cube in its orbit.

The most important

1. **src/cljs/clprojector/core.cljs**
2. **src/clj/clprojector/handler.clj**
3. **src/cljs/clprojector/draw.cljs**
4. **src/cljs/clprojector/internal.cljs**
5. **src/cljc/clprojector/util.cljc**



STRETCH-VIEW - don't call it in animation loop, call it once.

## Programming Interface

## Internals
