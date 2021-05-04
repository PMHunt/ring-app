(ns ring-app.core
  (:require
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

(def handler json-handler) ; map handler to json-handler

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
       wrap-formats
       wrap-reload)
   {:port 3000
    :join? false}))
