(ns {{name}}.draw (:require
                      [{{name}}.util :as clu]
                      [{{name}}.internal :as int]))

(def dist clu/camera-distance) 
(def half-width (/ clu/virtual-width 2))
(def half-height (/ clu/virtual-height 2))

(defn project
  ([x y z]
   {:x (+ half-width (*  half-height (/ (* x dist) (+ dist z))))
    :y (+ half-height (* half-height (/ (* y dist) (+ dist z))))})
  ([{ :keys [x y z]}]
   (project x y z)))

(declare get-canvas)

(defn make-canvas []
  (.appendChild  (first (array-seq (.getElementsByTagName js/document "div")))
                 (js/document.createElement "canvas"))
  (set! (.-width (get-canvas)) clu/virtual-width)
  (set! (.-height (get-canvas)) clu/virtual-height)
  (get-canvas)  
)

(defn get-canvas []
  (let [exists  (first (array-seq (.getElementsByTagName js/document "canvas")))]
    (or exists (make-canvas))))

(defn get-context [canvas] (.getContext canvas "2d" ))

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

(defn facet-list [ctx l r g b a]
  (loop [ll l]
    (apply facet           
           (concat (conj (list (take 3 ll)) ctx)
                   (list r g b a)))
  (if (> (count ll) 3) (recur (drop 3 ll)))))

(defn cls [ctx r g b]
  (set! (.-fillStyle ctx)   (str "rgba(" r "," g "," b ",1)"))
  (.fillRect ctx 0 0 clu/virtual-width clu/virtual-height))

(defn rotate-about-axis
  [px py pz rx ry rz theta]
  (let [costheta (Math/cos theta) sintheta (Math/sin theta)]
    {
     :x (+
         (* px (+ costheta (* rx rx (- 1 costheta))))
         (* ( - (* (- 1 costheta) rx ry) (* rz sintheta)) py)
         (* ( + (* (- 1 costheta) rx rz) (* ry sintheta)) pz))
     :y (+        
         (* ( + (* (- 1 costheta) rx ry) (* rz sintheta)) px)
         (* py (+ costheta (* ry ry (- 1 costheta))))          
         (* ( - (* (- 1 costheta) ry rz) (* rx sintheta)) pz))
     :z (+        
         (* ( - (* (- 1 costheta) rx rz) (* ry sintheta)) px)
         (* ( + (* (- 1 costheta) ry rz) (* rx sintheta)) py)
         (* pz (+ costheta (* rz rz (- 1 costheta)))))}))

(defn translate
  ([px py pz tx ty tz] { :x (+ px tx) :y (+ py ty) :z (+ pz tz)  })
  ([l tx ty tz]  (list (+ (nth l 0) tx)  (+  (nth l 1) ty)  (+  (nth l 2) tz))))

(defn rotate-about-x
  ([x y z theta] (rotate-about-axis x y z 1 0 0 theta))
  ([l theta]
   (let [m (apply rotate-about-x (concat l (list theta)))]
    (list (:x m) (:y m) (:z m)))))

(defn rotate-about-y
  ([x y z theta] (rotate-about-axis x y z 0 1 0 theta))
  ([l theta]
   (let [m (apply rotate-about-y (concat l (list theta)))]
    (list (:x m) (:y m) (:z m)))))

(defn rotate-about-z
  ([x y z theta] (rotate-about-axis x y z 0 0 1 theta))
  ([l theta]
   (let [m (apply rotate-about-z (concat l (list theta)))]
    (list (:x m) (:y m) (:z m)))))

(defn line [ctx x1 y1 z1 x2 y2 z2 r g b a ] ;3D
   (let [twod1 (project x1 y1 z1  )
         twod2 (project x2 y2 z2  )]
     (int/line-2d ctx (:x twod1) (:y twod1) (:x twod2) (:y twod2) r g b a)))

(defn make-pairs ([collect] (make-pairs collect []))
  ([collect product]
   (let [fragment (take 2 collect) pr (cons fragment product) ]
    (if (= (count fragment) 0) (rest product)
        (recur (rest collect) pr)))))

;;Takes 3D coords
(defn line-list [ctx r g b a l]
  (doseq [ elem (make-pairs l)]
    (apply line           
           (concat (conj (concat (first elem)(second elem)) ctx)
                   (list r g b a)))))


(defn poly [ctx r g b a points]
  (set! (.-fillStyle ctx)  (str "rgba(" r "," g "," b "," a ")"))
  (.beginPath ctx)
  (.moveTo ctx (:x (apply project (first points)))(:y (apply project (first points))))
  (loop [p (drop 1 points)]
   (.lineTo ctx (:x (apply project (first p)))(:y (apply project (first p))))
   (if (> (count p) 1)(recur (drop 1 p))))
  (.closePath ctx)
  (.fill ctx))

(defn write-text [ctx x y r g b a size text font]
  (set! (.-fillStyle ctx)
        (str "rgba(" r "," g "," b "," a ")"))
  (set! (.-font ctx) (str size "px " font))
  (.fillText ctx text  (* clu/virtual-width x) (* clu/virtual-height y))
)

(defn draw-image [ctx x y id]
  (let [img (.getElementById js/document id) tx (* clu/virtual-width x) ty (* clu/virtual-height y)]
   (.drawImage ctx img tx ty)))
