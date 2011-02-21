(ns fitbit
  (:use [clojure.contrib.json :only [read-json]]
        [clojure.contrib.java-utils :only [as-str]])
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [com.twinql.clojure.http :as http]
            [fitbit.query :as query]
            [oauth.client :as oauth]
            [oauth.signature])
  (:import (java.io File)
           (org.apache.http.entity.mime.content FileBody)
           (org.apache.http.entity.mime MultipartEntity)))

(declare status-handler)

(def *oauth-consumer* nil)
(def *oauth-access-token* nil)
(def *oauth-access-token-secret* nil)
(def *protocol* "http")

;; Get JSON from clj-apache-http 
(defmethod http/entity-as :json [entity as state]
  (read-json (http/entity-as entity :string state)))

(defmacro with-oauth
  "Set the OAuth access token to be used for all contained fitbit requests."
  [consumer access-token access-token-secret & body]
  `(binding [*oauth-consumer* ~consumer
             *oauth-access-token* ~access-token
             *oauth-access-token-secret* ~access-token-secret]
     (do 
       ~@body)))

(defmacro with-https
  [ & body]
  `(binding [*protocol* "https"]
     (do 
       ~@body)))

(defmacro def-fitbit-method
  "Given basic specifications of a fitbit API method, build a function that will
take any required and optional arguments and call the associated fitbit method."
  [method-name req-method req-url required-params optional-params handler]
  (let [required-fn-params (vec (sort (map #(symbol (name %))
                                           required-params)))
        optional-fn-params (vec (sort (map #(symbol (name %))
                                           optional-params)))]
    `(defn ~method-name
       [~@required-fn-params & rest#]
       (let [req-uri# (str *protocol* "://" ~req-url)
             rest-map# (apply hash-map rest#)
             provided-optional-params# (set/intersection (set ~optional-params)
                                                         (set (keys rest-map#)))
             required-query-param-names# (map (fn [x#]
                                                (keyword (string/replace (name x#) #"-" "_" )))
                                              ~required-params)
             optional-query-param-names-mapping# (map (fn [x#]
                                                        [x# (keyword (string/replace (name x#) #"-" "_"))])
                                                      provided-optional-params#)
             query-params# (merge (apply hash-map
                                         (vec (interleave required-query-param-names# ~required-fn-params)))
                                  (apply merge
                                         (map (fn [x#] {(second x#) ((first x#) rest-map#)}) optional-query-param-names-mapping#)))
             need-to-url-encode# (if (= :get ~req-method)
                                   (into {} (map (fn [[k# v#]] [k# (oauth.signature/url-encode v#)]) query-params#))
                                   query-params#)
             oauth-creds# (when (and *oauth-consumer* 
                                     *oauth-access-token*) 
                            (oauth/credentials *oauth-consumer*
                                               *oauth-access-token*
                                               *oauth-access-token-secret*
                                               ~req-method
                                               req-uri#
                                               need-to-url-encode#))]
         (~handler (~(symbol "http" (name req-method))
                    req-uri#
                    :query (merge query-params#
                                  oauth-creds#)
                    :parameters (http/map->params 
                                 {:use-expect-continue false})
                    :as :json))))))

;;;; Almost every method, and all functionality, of the fitbit API
;;;; is defined below with def-fitbit-method or a custom function to support
;;;; special cases, such as uploading image files.

(def-fitbit-method activities
  :get
  "api.fitbit.com/1/user/-/activities/date/2011-02-20.json"
  []
  []
  (comp #(:content %) status-handler))

(defn status-handler
  "Handle the various HTTP status codes that may be returned when accessing
the fitbit API."
  [result]
  (condp #(if (coll? %1)  
            (first (filter (fn [x] (== x %2)) %1))
            (== %2 %1)) (:code result)
    200 result
    304 nil
    [400 401 403 404 406 500 502 503] (let [body (:content result)
                                            headers (into {} (:headers result))
                                            error-msg (:error body)
                                            error-code (:code result)
                                            request-uri (:request body)]
                                        (throw (proxy [Exception] [(str "[" error-code "] " error-msg ". [" request-uri "]")]
                                                 (request [] (body "request"))
                                                 (remaining-requests [] (headers "X-RateLimit-Remaining"))
                                                 (rate-limit-reset [] (java.util.Date. 
                                                                       (long (headers "X-RateLimit-Reset")))))))))
