(ns aleph-encoding-bug.core
  (:require [byte-streams :as bs]
            [aleph.http :as http])
  (:import (java.io FileInputStream)))

(defn handler [req]
  (let [body-string (bs/to-string (:body req))]
    (if-let [bug (second (re-matches #".*(sentralbyr[^å]).*" body-string))]
      (do (println "encoding error =>" bug)
          {:status  500
           :headers {"content-type" "text/plain"}
           :body    "encoding bug"})
      (do (println "everything seems fine")
          {:status  200
           :headers {"content-type" "text/plain"}
           :body    "OK"}))))

(defonce server (http/start-server (fn [req] (handler req)) {:port 8080 :raw-stream? true}))

; $ curl -d @bad.json localhost:8080 => encoding error
; $ curl -d @good.json localhost:8080 => OK

; The following also works:
; (def ok @(http/post "http://localhost:8080/" {:body (slurp "bad.json")}))