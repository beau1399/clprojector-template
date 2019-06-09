# clprojector-proj

CLPROJECTOR is a Leiningen template for Web projects that use browser scripting to do 3 dimensional rendering. It provides functions to translate, rotate, and project 3D coordinates onto the 2D raster graphics interface offered by the browser. There are also functions to draw lines and polygons from 3D coordinates, and to output text and raster images to the display.

The Web infrastructure used to serve up the app and build / load all of its Clojure(Script) code comes from the Porpus template (https://github.com/beau1399/porpus). If you have questions about the Web server used, where styles and whatnot should be stored, how incremental reloading during development works, etc., you are advised to take a look at the Porpus README.md file.

## Rationale

The code provided here is built around the HTML5 **canvas** control. This is in some way behind the state-of-the-art, in that most browsers now support WebGL. However, I came into this project with a lot of preexisting **canvas**-driven code and I wanted to generalize it into a library and template so that I could continue to use it for those problems where the performance of **canvas** is adequate. To write a library like this from the ground up using WebGL would have been a lengthier effort.

## Usage

This section shows how to download the CLPROJECTOR template, install it, use it to generate a runable 3D Web application, and then view the demo that comes out-of-the-box with each such Web app generated. The first step is to clone the repository. This should be done from the root folder under which you keep your various Git-sourced code folders:

```$ git clone https://github.com/beau1399/clprojector-template.git```

Then Leiningen must be used to install the template:

```$ cd clprojector```

```$ lein install```

Move back out to your top-level code folder and instantiate a CLPROJECTOR application:

```$ cd ..```

```$ lein new clprojector-proj projtest```

Finally, use Figwheel to run a local Web server:

```$ cd projtest```

```$ lein figwheel```

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
````

The identifier "scene" is expected by the CLPROJECTOR infrastructure. It will be called upon page load and must make a call to JavaScripts **js/setInterval** function to create an animation loop. The anonymous function created in the second line from the bottom above is the one that will executed repeatedly, once per frame of demo animation. Before that happens, calls to **cld/get-context** and **cld/get-canvas** obtain variable **ctx**, an HTML5 **context** object, which is reused and passed to the CLPROJECTOR rendering functions throughout the runtime of the demo.

Atoms **angle** and **place** will vary over time and define the state of the model. The first of these is a number in radians which is overloaded to hold both 1) the angle by which the two-color cube is rotated about its own "Y" axis and 2) the position of the second, wireframe cube in its orbit.

Next, the code in **core.cljs** proceeds with the body of the rendering function. First, the display is cleared:

```       (cld/cls ctx 0 0 0)      ```

The parameters to **cls** are the context, and red, green, and blue color values, which range from 0 to 255. Here, we are clearing the display to black.

Following the code downward, we see the creation of the red background lines (notice that we are rendering from the back of the scene to front; we do not want the red lines to appear atop the shapes):

```
          (dorun (map #(cld/line ctx
                              (- (* % 0.25) 2.5) -1 1
                              (- (* % 0.25) 2.5) 1 1
                              255 0 0 1)(range 20))) 
```                              
Set aside, for now, lines 2 and 3 above, which are only explicable in terms of the CLPROJECTOR coordinate system. What we have is a **map** operation being applied to the range 0...19, to create 20 vertical red lines. Most of these lines will not be visible on a given display; the code accomodates super-wide aspect ratios that are not currently prevalent.

The **map** operation applies an anonymous function to the range, taking each 0...19 value and building it into the "X" and "Y" values that are the first two parameters to **cld/line**, after the initial context parameter. After the "X" and "Y" parameters, red, green, blue, and alpha (ranging from 0.0 to 1.0) values are passed.


### Coordinate System

![CLPROJECTOR coordinate system](https://raw.githubusercontent.com/beau1399/clprojector-template/master/frustum.png)

The image above is a three-quarter representation of the CLPROJECTOR viewable universe, rendered using vanishing point perspective. The plane shaded gray represents the square portion of the user's display. The unshaded, coplanar areas to its side account for the non-square nature of most display devices.

Considering the shaded area, we see that coordinate (0,0,0) represents the top left corner of the central, square portion of the display. The extreme top left corner of the entire display will have a negative "X" coordinate whose absolute value is proportional to the aspect ratio of the display. 

The "Z" dimension extends forward, from 0 at the camera to infinity. At Z=100, objects that would occupy the entire display at Z=0 are barely visible; this is denoted in the figure.

Returning to the demo code, these calculations, which yield the start and end coordinates for each of the red background lines, should make more sense:

```
                              (- (* % 0.25) 2.5) -1 1
                              (- (* % 0.25) 2.5) 1 1
```
The first value on each line, the "X" parameter, is identical, since the lines are vertical. When % is 0, these evaluate to 2.5 (i.e. 2.5 times the display height), which will be past the extreme right of just about any contemporary display. At the other extreme, when % is 19, the "X" parameter will be -2.25, which is well left of the display edge. 

### The Wireframe Cube

Continuing with the demo code, the next operation is the rendering of the wireframe cube:

```
;;;Orbiting cube
       (cld/line-list
        ctx 255 0 255 1
        (map
         #(cld/translate % 0 0 2)        
         (map
          #(cld/rotate-about-y % @angle)
          (map
           #(cld/translate % 1 0 0)
           (list
            '(-0.25 -0.25 -0.25)
            '(0.25 -0.25 -0.25)
            '(0.25 -0.25 0.25)
            '(-0.25 -0.25 0.25)
            '(-0.25 -0.25 -0.25)
            '(-0.25 0.25 -0.25)
            '(0.25 0.25 -0.25)
            '(0.25 0.25 0.25)
            '(-0.25 0.25 0.25)
            '(-0.25 0.25 -0.25)
            '(-0.25 0.25 0.25)
            '(-0.25 -0.25 0.25)
            '(0.25 -0.25 0.25)
            '(0.25 0.25 0.25)
            '(0.25 0.25 -0.25)
            '(0.25 -0.25 -0.25))))))
```

This snippet is best analyzed from the inside out. At its center resides a set of coordinate triplets that define a set of lines forming the cube, whose vertices are each 0.5 units long. 

This definition centers the cube around (0,0,0). On its own, this coordinate is not a very suitable center for anything, since it represents the top left corner of the display, right at camera depth. Both solids in the demo are first defined around (0,0,0), though, and then subjected to translations to place them more centrally within the viewable area.

The first two coordinate triplets are (-0.25, -0.25, -0.25) and (0.25, -0.25, -0.25). Looking at the "Y" and "Z" values, we can determine that this pair defines the top (Y=-0.25) / front (Z=-0.25) edge of the cube. It is only the "Z" coordinates that vary across this first line, as it sweeps from left to right. 

Moving on, the second coordinate (0.25, -0.25, -0.25) is reused in conjunction with the third to define the second line drawn. This is how the **cld/line-list** function, to which this coordinate list will eventually be passed, operates. This second line connects the front top right vertex of the cube to the back top right, as "Z" varies. 

The next two coordinates complete the top, square facet of the cube. The definition continues in similar fashion until all the necessary lines have been laid out.

Working outward, the cube definition is first passed to **cld/translate**, which moves it to (1,0,0). Then, **cld/rotate-about-y** rotates the cube (which is now located to the right of the Y axis) about this axis, thus achieving the orbiting effect. Finally, **cld/translate** is called again, to move the whole cube 2 units forward in the "Z" dimension, to give it some distance from the camera and allow for it to be better viewed. 

Because the cube is defined a list of coordinate triplets, and **cld/translate**, etc. operate on one point in 3D space per call, each of these operations is implemented as a **map** operation. Once all of this translation and rotation is complete, the resultant list of coordinate triplets is passed to **cld/line-list**, which also expects RGBA parameters and the context. 


The most important

1. **src/cljs/clprojector/core.cljs**
2. **src/clj/clprojector/handler.clj**
3. **src/cljs/clprojector/draw.cljs**
4. **src/cljs/clprojector/internal.cljs**
5. **src/cljc/clprojector/util.cljc**



STRETCH-VIEW - don't call it in animation loop, call it once.

## Programming Interface

There are several functions in the CLPROJECTOR programming interface that expect only 2D paramters, and they assume a "Z" value of 0 (as close to the camera as possible without being behind it). The coordinate system is discussed in the next section.


## Internals
