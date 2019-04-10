(ns {{name}}.internal
    (:require
     [{{name}}.util :as clu]))

(def camera-distance 2)
(def dist camera-distance)

(def view-height (atom (or (.-clientHeight (.-documentElement js/document)) (.-innerHeight js/window))))
(def view-width (atom (or (.-clientWidth (.-documentElement js/document)) (.-innerWidth js/window))))

(defn half-width [] (/ @view-width 2))
(defn half-height [] (/ @view-height 2))

(defn project
  ([px py pz]
   (let [zprime (- pz dist)]
    (loop [x px y py z zprime]  
     (if (> z (- 0 dist)) ;Don't return something behind the camera...
      {:x (+ (half-width) (*  (half-height) (/  x (+ dist z))))
       :y (+ (half-height) (* (half-height) (/  y (+ dist z))))}
      (recur (* 0.999 x)(* 0.999 y)(* 0.999 z)) ;...retract to camera plane instead
   ))))
  ([{ :keys [x y z]}]
   (project x y z)))

(defn line-2d
  [ctx x1 y1 x2 y2 r g b a] ;2D
   (set! (.-strokeStyle ctx)
         (str "rgba(" r "," g "," b "," a ")"))
   (set! (.-lineWidth ctx) 1)   
   (.beginPath ctx)
   (.moveTo ctx x1 y1)
   (.lineTo ctx x2 y2)
   (.closePath ctx)
   (.stroke ctx))

(defn line [ctx x1 y1 z1 x2 y2 z2 r g b a ]
   (let [twod1 (project x1 y1 z1  )
         twod2 (project x2 y2 z2  )]
     (line-2d ctx (:x twod1) (:y twod1) (:x twod2) (:y twod2) r g b a)))

(defn ccw [x1 y1 x2 y2 x3 y3]
  (let [valu (- (* (- y2 y1)(- x3 x2))
                (* (- x2 x1)(- y3 y2)))](> valu 0)))

(defn facet [ctx points r g b a]
  (let [p1  (nth points 0)     p2  (nth points 1)     p3  (nth points 2)
        pj1 (apply project p1) pj2 (apply project p2) pj3 (apply project p3)
        x1  (:x pj1)           y1  (:y pj1)           x2  (:x pj2) y2 (:y pj2) x3 (:x pj3) y3 (:y pj3)]
    (if (ccw x1 y1 x2 y2 x3 y3)     
      (do
        ;;;https://petewarden.com/2011/01/30/a-fundamental-bug-in-html5s-canvas/        
        (set! (.-lineWidth ctx) (/ a 3)) ;seems to help seams!
        (set! (.-fillStyle ctx)
              (str "rgba(" r "," g "," b "," a ")"))        
        (set! (.-strokeStyle ctx)
        (str "rgba(" r  "," g "," b "," a ")"))
        (.beginPath ctx)
        (.moveTo ctx x1 y1)
        (.lineTo ctx x2 y2)
        (.lineTo ctx x3 y3)
        (.closePath ctx)
        (.fill ctx)        
        (.stroke ctx)))))
