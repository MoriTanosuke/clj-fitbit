(ns fitbit)

(require ['oauth.client :as 'oauth]
         ['fitbit.settings :as 'settings])

(def consumer (oauth/make-consumer (:consumer_key settings/settings)
                                   (:consumer_secret settings/settings)
                                   "http://api.fitbit.com/oauth/request_token"
                                   "http://api.fitbit.com/oauth/access_token"
                                   "http://api.fitbit.com/oauth/authorize"
                                   :hmac-sha1))

(def request-token (oauth/request-token consumer))

(println (oauth/user-approval-uri consumer
                         request-token))

(println "Enter your PIN:")
(def settings (merge settings/settings {:pin (read-line)}))

(def access-token-response (oauth/access-token consumer 
                                               request-token
                                               (:pin settings)))

(require ['com.twinql.clojure.http :as 'http])

(http/get "http://www.google.de"
  :parameters (http/map->params {:use-expect-continue false
                                 :default-proxy (http/http-host :host "127.0.0.1" :port 8765)}))

(http/get "http://www.google.de"
  :headers {"Authorization" (oauth/authorization-header
                              (oauth/credentials consumer
                                (:oauth_token access-token-response)
                                (:oauth_token_secret access-token-response)
                                :GET
                                "http://www.google.de"))}
  :parameters (http/map->params {:use-expect-continue false
                                 :default-proxy (http/http-host :host "127.0.0.1" :port 8765)}))



(http/get "http://api.fitbit.com/1/user/-/activities/date/2011-03-14.json"
  :headers {"Authorization" (oauth/authorization-header
                              (oauth/credentials consumer
                                (:oauth_token access-token-response)
                                (:oauth_token_secret access-token-response)
                                :GET
                                "http://api.fitbit.com/1/user/-/activities/date/2011-03-14.json"))}
  :parameters (http/map->params {:use-expect-continue false
                                 :default-proxy (http/http-host :host "127.0.0.1" :port 8765)}))

(http/get "http://api.fitbit.com/1/user/-/foods/log/date/2011-03-14.json"
  :headers {"Authorization" (oauth/authorization-header
                              (oauth/credentials consumer
                                (:oauth_token access-token-response)
                                (:oauth_token_secret access-token-response)
                                :GET
                                "http://api.fitbit.com/1/user/-/foods/log/date/2011-03-14.json"))}
  :parameters (http/map->params {:use-expect-continue false
                                 :default-proxy (http/http-host :host "127.0.0.1" :port 8765)}))
