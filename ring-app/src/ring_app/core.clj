(ns ring-app.core
  (:require
   [reitit.ring :as reitit]
   [muuntaja.middleware :as muuntaja]
   [ring.adapter.jetty :as jetty]
   [ring.util.http-response :as response]
   [ring.middleware.reload :refer [wrap-reload]]))

(defn html-handler
  "handles html requests"
  [request-map]
  (response/ok
   (str "<html><body>Your IP is: "
        (:remote-addr request-map)
        "</body></html>")))

(defn json-handler
  "handles Content-Type: application/json"
  [request]
  (response/ok
   {:result (get-in request [:body-params :id])}))

(def routes
  [["/" html-handler]
   ["/echo/:id"
    {:get
     (fn [{{:keys [id]} :path-params}] ; destructure path-params to get id
       ;; When the keys and symbols can all have the same name,
       ;; there is a shorter syntax available {:keys [id]}
       (response/ok (str "<p> the value is: " id  "</p>")))}]
   ["/api" {:middleware [wrap-formats]}
    ;; we apply wrap-formats only to /api end points, via router :middlware key
    ;; https://cljdoc.org/d/metosin/reitit/0.5.13/doc/ring/data-driven-middleware
    ["/multiply"
     {:post
      (fn [{{:keys [a b]} :body-params}]
        (response/ok {:result (* a b)}))}]]])

(def handler
  (reitit/ring-handler ; map handler to reitit handler
   (reitit/router routes)     ; apply reitit router to routes
   (reitit/routes ; https://cljdoc.org/d/metosin/reitit/0.5.13/doc/ring/static-resources
    (reitit/create-resource-handler {:path "/"}) ; first so isn't masked
    (reitit/create-default-handler
     {:not-found
      (constantly (response/not-found "404 - Page not found"))
      :method-not-allowed
      (constantly (response/method-not-allowed "405 - method not allowed"))
      :not-acceptable
      (constantly (response/not-acceptable "406 - Not acceptable"))}))))


(defn wrap-nocache
  "Takes our handler, returns  handler that adds pragma to the response header
  for Jetty to serve"
  [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Pragma"] "no-cache"))))

(defn wrap-formats
  "Uses Muuntaja middleware to serialize/deserialize data based on Content-Type"
  [handler]
  (-> handler
      (muuntaja/wrap-format)))

(defn -main
  "Starts a jetty server with our handler attached, join? manages blocking
  We use threading macro to string together some  functions wrapping handler"
  []
  (jetty/run-jetty
   (-> #'handler
       wrap-nocache
       wrap-reload)
   {:port 3000
    :join? false}))
