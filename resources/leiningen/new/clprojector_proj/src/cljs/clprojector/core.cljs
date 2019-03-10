(ns {{name}}.core
  (:require
   [{{name}}.draw :as cld]
   [reitit.frontend :as reitit]))
(defn ^:export scene []
  (let[ctx (cld/get-context (cld/get-canvas))]
   (def angle (atom 0))    
    (def place (atom -0.5))
    (js/setInterval     
     (fn []
       (cld/cls ctx 0 0 0)
;;;Background lines
       (dorun (map #(cld/line ctx
                              (- (* % 0.25) 2.5) -1 0
                              (- (* % 0.25) 2.5) 1 0
                              255 0 0 1)(range 20)))
;;;Orbiting cube
       (cld/line-list
        ctx 255 0 255 1
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
           '(0.25 -0.25 -0.25)))))    
;;;Travelling Cube (most of it)
       (cld/facet-list
        ctx  
        (map
         #(cld/translate %1 0 0 @place)
         (map
          #(cld/rotate-about-y % @angle)
          (map
           #(cld/rotate-about-x % @angle)
           (list
;;;BACK face
            '(0.5 -0.5 0.5)
            '(-0.5 0.5 0.5)
            '(-0.5 -0.5 0.5)             
            '(-0.5 0.5 0.5)
            '(0.5 -0.5 0.5)
            '(0.5 0.5 0.5)             
;;;LEFT            
            '(-0.5 -0.5 -0.5)
            '(-0.5 -0.5 0.5)
            '(-0.5 0.5 0.5)             
            '(-0.5 0.5 0.5)                                 
            '(-0.5 0.5 -0.5)
            '(-0.5 -0.5 -0.5)
;;;FRONT
            '(0.5 0.5 -0.5)             
            '(-0.5 -0.5 -0.5)
            '(-0.5 0.5 -0.5)
            '(-0.5 -0.5 -0.5)             
            '(0.5 0.5 -0.5)
            '(0.5 -0.5 -0.5)
;;;RIGHT
            '(0.5 0.5 0.5)
            '(0.5 -0.5 0.5)
            '(0.5 -0.5 -0.5)             
            '(0.5 -0.5 -0.5)
            '(0.5 0.5 -0.5)
            '(0.5 0.5 0.5)))))
        0 0 255 0.6)
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

       (cld/write-text ctx 0.025 0.05 0 255 0 1 22 "CLProjector Demo" "monospace")
       (cld/draw-image ctx 0.85 0.825 "img1")
       
       (swap! place #(if (< % 100) (+ 0.05 %) -0.5))
       (swap! angle #(+ 0.01 %))
       ) 10)))
