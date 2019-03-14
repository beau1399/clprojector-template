(ns {{name}}.draw (:require
                      [{{name}}.util :as clu]
                      [{{name}}.internal :as int]))

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

(defn facet-list [ctx l r g b a]
  (loop [ll l]
    (apply int/facet           
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

(defn make-pairs ([collect] (make-pairs collect []))
  ([collect product]
   (let [fragment (take 2 collect) pr (cons fragment product) ]
    (if (= (count fragment) 0) (rest product)
        (recur (rest collect) pr)))))


;;;Pass-through w/ Z-clipping since funcs. in this file have that
(defn line [ctx x1 y1 z1 x2 y2 z2 r g b a]
 (if (and (> z1 -2)(> z2 -2))
  (int/line ctx x1 y1 z1 x2 y2 z2 r g b a )))
  
(defn line-list [ctx r g b a l]
  (doseq [ elem (make-pairs l)]
    (apply int/line           
           (concat (conj (concat (first elem)(second elem)) ctx)
                   (list r g b a)))))

;;;Auto-clips Z dimension; other functions don't, at least not at this point
(defn poly [ctx r g b a points]
  (if (reduce #(and %1 %2) (map (fn [p] (>= (nth p 2) -2)  ) points)) ;Clip Z dimension
    (do
      (set! (.-fillStyle ctx)  (str "rgba(" r "," g "," b "," a ")"))
      (set! (.-strokeStyle ctx)  (str "rgba(" r "," g "," b "," a ")"))
      (.beginPath ctx)
      (.moveTo ctx (:x (apply int/project (first points)))(:y (apply int/project (first points))))
      (loop [p (drop 1 points)]
        (.lineTo ctx (:x (apply int/project (first p)))(:y (apply int/project (first p))))
        (if (> (count p) 1)(recur (drop 1 p))))
      (.closePath ctx)
      (.fill ctx)
      (.stroke ctx))))

(defn write-text [ctx x y r g b a size text font]
  (set! (.-fillStyle ctx)
        (str "rgba(" r "," g "," b "," a ")"))
  (set! (.-font ctx) (str size "px " font))
  (.fillText ctx text  (* clu/virtual-width x) (* clu/virtual-height y))
)

(defn draw-image [ctx x y id]
  (let [img (.getElementById js/document id) tx (* clu/virtual-width x) ty (* clu/virtual-height y)]
   (.drawImage ctx img tx ty)))
