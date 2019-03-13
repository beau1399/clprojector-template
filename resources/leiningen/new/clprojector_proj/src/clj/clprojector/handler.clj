(ns {{name}}.handler
  (:require 
  	    [reitit.ring.coercion :as rrc]
            [reitit.coercion.spec]
	    [reitit.ring :as reitit-ring]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.memory :as memory]
            [{{name}}.middleware :refer [middleware]]
            [{{name}}.util :as clu]
            [hiccup.page :refer [include-js include-css html5]]
            [config.core :refer [env]]))

(def store (memory/memory-store))

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, maximum-scale=1, user-scalable=0"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [
     ["/" {:get
           {
            :handler (fn [request]
                       {:status  200
                        :headers {"Content-Type" "text/html"}
                        :body
                        (html5 [:head (head) (include-js "/js/app.js")]
                               [:body {:style "margin:0" }
                                [:img {:src "http://13.85.17.138/cljs-white.png" :style "display:none;" :id "img1"}] 
                                [:div {:style "width:100vw; height:100vh; margin:0;padding:0; position:fixed; bottom:0; right:0" }]
                                [:script  "{{name}}.core.scene()"]])})}}]]
    {:data {:middleware (concat [[wrap-session {:store store}]] middleware) }})
    (reitit-ring/routes
     (reitit-ring/create-resource-handler {:path "/" :root "/public"})
     (reitit-ring/create-default-handler))))


