(ns leiningen.new.clprojector-proj
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files]]
            [leiningen.core.main :as main]))

(def render (renderer "clprojector-proj"))

(defn clprojector-proj
  "CLJ(S) Project Template for 3D Rendering on an HTML5 Canvas"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (main/info "Generating fresh 'lein new' clprojector-proj project.")
    (->files data
             ["project.clj" (render "project.clj" data)]
             ["src/clj/{{sanitized}}/handler.clj" (render "src/clj/clprojector/handler.clj" data)]
             ["src/clj/{{sanitized}}/server.clj" (render "src/clj/clprojector/server.clj" data)]
             ["src/cljc/{{sanitized}}/util.cljc" (render "src/cljc/clprojector/util.cljc" data)]
             ["src/cljs/{{sanitized}}/core.cljs" (render "src/cljs/clprojector/core.cljs" data)]
             ["src/cljs/{{sanitized}}/draw.cljs" (render "src/cljs/clprojector/draw.cljs" data)]
             ["src/cljs/{{sanitized}}/internal.cljs" (render "src/cljs/clprojector/internal.cljs" data)]
             ["resources/public/css/site.css" (render "resources/public/css/site.css" data)]
             ["env/dev/clj/{{sanitized}}/middleware.clj" (render "env/dev/clj/clprojector/middleware.clj" data)]
             ["env/dev/clj/{{sanitized}}/repl.clj" (render "env/dev/clj/clprojector/repl.clj" data)]
             ["env/dev/clj/user.clj" (render "env/dev/clj/user.clj" data)]
             ["env/dev/cljs/{{sanitized}}/dev.cljs" (render "env/dev/cljs/clprojector/dev.cljs" data)]
             ["env/prod/clj/{{sanitized}}/middleware.clj" (render "env/prod/clj/clprojector/middleware.clj" data)]
             ["env/prod/cljs/{{sanitized}}/prod.cljs" (render "env/prod/cljs/clprojector/prod.cljs" data)]
             )))
