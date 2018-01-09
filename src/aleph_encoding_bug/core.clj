(ns aleph-encoding-bug.core
  (:require [byte-streams :as bs]
            [clojure.string :as str]
            [clojure.test :as test]
            [clojure.java.io :as io]
            [aleph.http :as http]))

(test/is (str/includes? (slurp "bad.json") "sentralbyrå"))

(test/is (str/includes? (let [stream (io/input-stream (io/file "bad.json"))]
                          (bs/to-string stream))
                        "sentralbyrå"))

(defn handler [req]
  (let [body-string (bs/to-string (:body req))]
    (if-let [bug (second (re-matches #".*(sentralbyr[^å]).*" body-string))]
      (do (println "encoding error =>" bug)
          {:status  500
           :headers {"content-type" "text/plain"}
           :body    (str "encoding bug => " bug)})
      (do (println "everything seems fine")
          {:status  200
           :headers {"content-type" "text/plain"}
           :body    "OK"}))))

(defonce server (http/start-server (fn [req] (handler req)) {:port 8080 :raw-stream? true}))

; $ curl -d @bad.json localhost:8080
; returns:
; encoding bug => sentralbyr�

; $ curl -d @good.json localhost:8080 => OK

; The following also works:
; (def ok @(http/post "http://localhost:8080/" {:body (slurp "bad.json")}))