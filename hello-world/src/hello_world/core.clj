(ns hello-world.core)

(defn handler
  "Ring handler"
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (:remote-addr request)})
