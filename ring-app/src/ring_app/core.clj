(ns ring-app.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [ring.middleware.reload :refer [wrap-reload]]))

(defn handler
  "handles requests"
  [request-map]
  (response/response
   (str "<html><body>Your IP is: "
        (:remote-addr request-map)
        "</body></html>")))

(defn wrap-nocache
  "Takes our handler, returns  handler that adds pragma to the response header
  for Jetty to serve"
  [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Pragma"] "no-cache"))))

(defn -main
  "Starts a jetty server with our handler attached, join? manages blocking"
  []
  (jetty/run-jetty
   (-> #'handler
       wrap-nocache
       wrap-reload)
   {:port 3000
    :join? false}))
