(ns {{name}}.internal)

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
