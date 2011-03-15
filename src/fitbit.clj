(ns fitbit)

(require ['oauth.client :as 'oauth]
         ['fitbit.fitbitutil :as 'util]
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
(def settings (merge settings/settings {:pin (read-line)}))

(def access-token-response
  (oauth/access-token consumer request-token (:pin settings)))


;; api access
(require ['com.twinql.clojure.http :as 'http])

(def todays-food (:content (http/get (util/get-url "foods/log" (util/today-as-str))
  :headers {"Authorization" (oauth/authorization-header
                              (oauth/credentials consumer
                                (:oauth_token access-token-response)
                                (:oauth_token_secret access-token-response)
                                :GET
                                (util/get-url "foods/log" (util/today-as-str))))}
  :parameters (http/map->params {:use-expect-continue false
                                 :default-proxy (http/http-host :host "127.0.0.1" :port 8765)})
             :as :string)))

(def todays-activities (:content
            (http/get (util/get-url "activities" (util/today-as-str))
              :headers {"Authorization" (oauth/authorization-header
                                          (oauth/credentials consumer
                                            (:oauth_token access-token-response)
                                            (:oauth_token_secret access-token-response)
                                            :GET
                                            (util/get-url "activities" (util/today-as-str))))}
              :parameters (http/map->params {:use-expect-continue false
                                             :default-proxy (http/http-host :host "127.0.0.1" :port 8765)})
              :as :string)))
