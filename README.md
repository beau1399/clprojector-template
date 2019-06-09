# clprojector-proj

CLPROJECTOR is a Leiningen template for Web projects that uses browser scripting to do three-dimensional rendering. It provides functions to translate, rotate, and project 3D coordinates onto the 2D raster graphics interface offered by the browser. It offers functions to draw lines and polygons from 3D coordinates, and to output text and raster images to the display.

The Web infrastructure used to serve up CLPROJECTOR applications and build / load all of their Clojure(Script) code comes from the Porpus template (https://github.com/beau1399/porpus). If you have questions about the Web server used, where to store styles and scripts, how incremental reloading during development works, etc., you are advised to take a look at the Porpus README.md file.

## Rationale

The code provided here is built around the HTML5 **canvas** control. Most browsers now support WebGL. However, I came into this project with a lot of preexisting **canvas**-driven code, which I wanted to generalize into a library and template, so that I could continue to use this code for those problems where the performance of **canvas** is adequate. To write a library like this from the ground up using WebGL would have been a lengthier effort.

## Usage

This section shows how to download the CLPROJECTOR template, install it, use it to generate a runable 3D Web application, and then view the demo provided out-of-the-box. 

The first step is to clone the repository. This will typically be done from the root folder under which you keep your various Git-sourced code folders:

`$ git clone https://github.com/beau1399/clprojector-template.git`

Then Leiningen must be used to install the template:

`$ cd clprojector`

`$ lein install`

Move back out to your top-level code folder and instantiate a CLPROJECTOR application:

`$ cd ..`

`$ lein new clprojector-proj projtest`

Finally, use Figwheel to run a local Web server:

`$ cd projtest`

`$ lein figwheel`

(The **projtest** project name is assumed in many of the source code examples shown below. Of course, you can call your project whatever you want.)

Now you can browse to http://localhost:3449/ to view the demo in action:

![CLPROJECTOR demo](https://raw.githubusercontent.com/beau1399/clprojector-template/master/clprojector.png)

## The Demo

The demo renders several different visual features

1. A background of vertical red lines
2. A static ClojureScript logo near the bottom right corner of the display
3. A two-color, semi-transparent cube spinning about its own "X" and "Y" axes while it moves away from the viewer along the "Z" axis.
4. A wireframe cube that orbits around an axis at X=0, Z=2
5. Static text near the top left corner of the display reading "CLProjector Demo"

The code responsible for rendering these feature resides under the project root at **src/cljs/clprojector/core.cljs**. By default, this is the file where your rendering code should go. At the top of this file, some necessary code is referenced:

``` 
(ns clprojtst.core
  (:require
   [clprojtst.draw :as cld]
   [reitit.frontend :as reitit]))
```
It is from the **draw** sub-namespace of the main project namespace, aliased above as "cld," that the CLPROJECTOR rendering functions will come.

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

Working outward, the cube definition is first passed to **cld/translate**, which moves it to (1,0,0). Then, **cld/rotate-about-y** rotates the cube (which is now located to the right of the Y axis) about this axis by **@angle**, thus achieving the orbiting effect. Finally, **cld/translate** is called again, to move the whole cube 2 units forward in the "Z" dimension, to give it some distance from the camera and allow for it to be better viewed. 

Because the cube is defined a list of coordinate triplets, and **cld/translate**, etc. operate on one point in 3D space per call, each of these operations is implemented as a **map** operation. Once all of this translation and rotation is complete, the resultant list of coordinate triplets is passed to **cld/line-list**, which also expects RGBA parameters and the context. 

## The Solid Cube

The code that renders the solid cube that moves away from the user along the "Z" axis is, in many ways, similar to the code for the wireframe cube. Of course, the translations and rotations for this effect are different from those used to achieve the orbit effect, but they rely on the same functions and techniques.

One difference is that this cube's edges are 1.0 units in size. Using a larger cube is desirable because this cube moves much farther away from the viewer.

More fundamentally, this cube is a solid, not a wireframe. This introduces some issues related to surface rendering. The wireframe model can simply be rendered in its entirety regardless of its rotation. For a solid cube, on the other hand, we must consider rotation. We do not want to render the back surface(s) of the cube. These must be obscured from view.

In CLPROJECTOR applications, this issue is handled in a manner that should be familiar to anyone with experience with a 3D library like OpenGL or Direct3D: the solid is defined as a series of triangular facets, which are specified in counter-clockwise order. If, after translation, rotation, etc., the **cld/facet-list** function encounters a facet which is now present in its parameters in *clockwise* order, this function considers it a back-facing facet and does not render it. These details are abstracted away by CLPROJECTOR; the onus is only on the user of the library to specify the facet coordinates in the proper order.

The solid cube is rendered via two calls to **cld/facet-list**, one for each of its two colors. Consider the second call (which is very similar to the first, but shorter, since fewer faces are rendered in the second color):

```
;;; Cube (top and bottom)
       (cld/facet-list
        ctx  
        (map
         #(cld/translate %1 0 0 @place)
         (map
          #(cld/rotate-about-y % @angle)
          (map
           #(cld/rotate-about-x % @angle)
           (list
;;;TOP
            '(-0.5 -0.5 -0.5)
            '(0.5 -0.5 0.5)
            '(-0.5 -0.5 0.5)             
            '(0.5 -0.5 -0.5)             
            '(0.5 -0.5 0.5)
            '(-0.5 -0.5 -0.5)
;;;BOTTOM                 
            '(0.5 0.5 -0.5)
            '(-0.5 0.5 -0.5)
            '(0.5 0.5 0.5)             
            '(-0.5 0.5 0.5)
            '(0.5 0.5 0.5)
            '(-0.5 0.5 -0.5)))))  0 255 0 0.6)
```

Again, we see an inner coordinate data structure filtered outward through positioning and rotation functions, and then passed to the rendering function. Here, that is **cld/facet-list**. The obvious difference is that instead of rectangles, the code above specifies adjacent triangles that only form rectangles when taken together. Also, unlike **cld/line-list**, **cld/facet-list** does not reuse ending coordinates as starting coordinates; each triangular facet requires three coordinates of its own. 

The counterclockwise nature of the coordinate ordering seen above is evident throughout. The first group of three triplets, for example, begins at the top left front vertex, proceeds to the top back right vertex, and then ends at the top left back vertex. When rotated to the front of the cube, this is clearly a counterclockwise ordering, beginning at around 7 o'clock, moving next to something like 1 o'clock, and then ending around 10 o'clock.

## Two-Dimensional Features

There are several functions in the CLPROJECTOR programming interface that expect only 2D paramters, and they assume a "Z" value of 0 (as close to the camera as possible without being behind it). The text and ClojureScript logo seen in the demo call some of these 2D functions. The code the draws the text is shown below:

`(cld/write-text ctx 0.025 0.05 0 255 0 1 22 "CLProjector Demo" "monospace")`

The parameters to **write-text** are, from the left, the context, "X" and "Y" coordinates where the text starts, RGBA color values, the font size in pixels, the text to be rendered, and a font family name. Note that the font size and family are passed through to the browser, just as if they were present in a .CSS file or **style** attribute.

In the call above, the coordinate value is just inside the top left corner of the central, square portion of the display center. The text is 100% red, and 100% opaque. The font is 22 pixels in size, and uses the "monospace" font family, which is a standard Web font.

### Images and the Handler File




The most important

1. **src/cljs/clprojector/core.cljs**
2. **src/clj/clprojector/handler.clj**
3. **src/cljs/clprojector/draw.cljs**
4. **src/cljs/clprojector/internal.cljs**
5. **src/cljc/clprojector/util.cljc**



STRETCH-VIEW - don't call it in animation loop, call it once.

## Programming Interface




## Internals
