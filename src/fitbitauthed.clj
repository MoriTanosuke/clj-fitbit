(ns fitbitauthed
  (:use clojure.contrib.json))

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

;; api access
(require ['com.twinql.clojure.http :as 'http])

(def todays-foods (:content (http/get (util/get-url "foods/log" (util/today-as-str))
  :headers {"Authorization" (oauth/authorization-header
                              (oauth/credentials consumer
                                (:oauth_token settings/stored-access-token-response)
                                (:oauth_token_secret settings/stored-access-token-response)
                                :GET
                                (util/get-url "foods/log" (util/today-as-str))))}
  :parameters (http/map->params {:use-expect-continue false
                                 :default-proxy (http/http-host :host "127.0.0.1" :port 8765)})
             :as :string)))

(def todays-activities (:content
            (http/get (util/get-url "activities" (util/today-as-str))
              :headers {"Authorization" (oauth/authorization-header
                                          (oauth/credentials consumer
                                            (:oauth_token settings/stored-access-token-response)
                                            (:oauth_token_secret settings/stored-access-token-response)
                                            :GET
                                            (util/get-url "activities" (util/today-as-str))))}
              :parameters (http/map->params {:use-expect-continue false
                                             :default-proxy (http/http-host :host "127.0.0.1" :port 8765)})
              :as :string)))

(println "your active score: " (:activeScore (:summary (read-json todays-activities))))

(println "your carbs: " (:carbs (:summary (read-json todays-foods))))
(println "your fat: " (:fat (:summary (read-json todays-foods))))
(println "your protein: " (:protein (:summary (read-json todays-foods))))

