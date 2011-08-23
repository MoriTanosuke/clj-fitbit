(ns fitbit)

(require ['oauth.client :as 'oauth]
         ['com.twinql.clojure.http :as 'http]
         ['fitbit.fitbitutil :as 'util]
         ['fitbit.settings :as 'settings]
         ['clojure.contrib.json :as 'json])

(def consumer (oauth/make-consumer (:consumer_key settings/settings)
                                   (:consumer_secret settings/settings)
                                   "http://api.fitbit.com/oauth/request_token"
                                   "http://api.fitbit.com/oauth/access_token"
                                   "http://www.fitbit.com/oauth/authorize"
                                   :hmac-sha1))

(def settings (merge settings/settings {}))

;TODO check if oauth_token and oauth_token_secret are present in settings
;;skip if token already set 
;(def request-token
;	(oauth/request-token consumer))
;
;(println (oauth/user-approval-uri consumer request-token))
;(println "Enter your PIN:")
;(def settings (merge settings {:pin (read-line)}))
;
;(oauth/user-approval-uri consumer 
;                         (:oauth_token request-token))
;
;(def access-token-response (oauth/access-token consumer 
;                                               request-token
;                                               (:pin settings)))
;
;(def settings (merge settings access-token-response))
;TODO write settings to file
                                               
(def todays-food (json/read-json
                   (:content (http/get (util/get-url "foods/log" (util/today-as-str))
  :headers {"Authorization" (oauth/authorization-header
                              (oauth/credentials consumer
                                (:oauth_token settings)
                                (:oauth_token_secret settings)
                                :GET
                                (util/get-url "foods/log" (util/today-as-str))))}
  :parameters (http/map->params {:use-expect-continue false
                                 ;:default-proxy (http/http-host :host "127.0.0.1" :port 8765)
                                 })
             :as :string))))

(println todays-food)
(reduce todays-food println)
