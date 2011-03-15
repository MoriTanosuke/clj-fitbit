(ns fitbit)

(require ['oauth.client :as 'oauth]
         ['fitbit.settings :as 'settings])

;; oauth preparations
(def consumer (oauth/make-consumer (:consumer_key settings/settings)
                                   (:consumer_secret settings/settings)
                                   "http://api.fitbit.com/oauth/request_token"
                                   "http://api.fitbit.com/oauth/access_token"
                                   "http://api.fitbit.com/oauth/authorize"
                                   :hmac-sha1))

(def request-token
  (oauth/request-token consumer))

(println (oauth/user-approval-uri consumer request-token))

(println "Enter your PIN:")
(def settings
  (merge settings/settings {:pin (read-line)}))

(def access-token-response
  (oauth/access-token consumer request-token (:pin settings)))


;; api access
(require ['com.twinql.clojure.http :as 'http])

(defn today
  [] (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") (java.util.Date.)))

(defn get-url
  ([action date] (str "http://api.fitbit.com/1/user/-/" action "/date/" date ".json")))

(println (:content
           (http/get (get-url "activities" (today))
             :headers {"Authorization" (oauth/authorization-header
                                         (oauth/credentials consumer
                                           (:oauth_token access-token-response)
                                           (:oauth_token_secret access-token-response)
                                           :GET
                                           (get-url "activities" (today))))}
             :parameters (http/map->params {:use-expect-continue false
                                            :default-proxy (http/http-host :host "127.0.0.1" :port 8765)})
             :as :string)))

(println (:content (http/get (get-url "foods/log" (today))
  :headers {"Authorization" (oauth/authorization-header
                              (oauth/credentials consumer
                                (:oauth_token access-token-response)
                                (:oauth_token_secret access-token-response)
                                :GET
                                (get-url "foods/log" (today))))}
  :parameters (http/map->params {:use-expect-continue false
                                 :default-proxy (http/http-host :host "127.0.0.1" :port 8765)})
             :as :string)))
