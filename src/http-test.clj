(ns http-test
  (:use [clojure.contrib.java-utils :only [as-str]])
  (:require [com.twinql.clojure.http :as http])
)

(def http-params {:use-expect-continue false
                  ;; set a debug proxy
                  :default-proxy (http/http-host :host "localhost" :port 8765)})

(def HEADERS {:foo "foo", :bar "bar", :baz "baz"})
(zipmap (map #(as-str %) (keys HEADERS)) (vals HEADERS))
(:code (http/get "http://www.kopis.de"
         :parameters (http/map->params http-params)
         :headers (zipmap (map #(as-str %) (keys HEADERS)) (vals HEADERS))))
