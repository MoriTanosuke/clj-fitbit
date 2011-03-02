(ns example)

(require 'fitbit.fitbit
         ['oauth.client :as 'oauth]
         'fitbit.settings)

(def consumer (oauth/make-consumer (:consumer_key settings/settings)
                                   (:consumer_secret settings/settings)
                                   "http://api.fitbit.com/oauth/request_token"
                                   "http://api.fitbit.com/oauth/access_token"
                                   "http://api.fitbit.com/oauth/authorize"
                                   :hmac-sha1))

;; Fetch a request token that a OAuth User may authorize
(def request-token (oauth/request-token consumer))

;; Send the User to this URI for authorization, they will be able 
;; to choose the level of access to grant the application and will
;; then be redirected to the callback URI provided.
;; 
;; If you are using OAuth with a desktop application, a callback URI
;; is not required. 
(println (str "Open this URL in your browser: "
  (oauth/user-approval-uri consumer request-token)))

;; read PIN from user
(println "Enter your PIN:")
(def settings (merge settings/settings {:pin (read-line)}))

;; Assuming the User has approved the request token, trade it for an access token.
;; The access token will then be used when accessing protected resources for the User.
;;
;; If the OAuth Service Provider provides a verifier, it should be included in the
;; request for the access token.  See [Section 6.2.3](http://oauth.net/core/1.0a#rfc.section.6.2.3) of the OAuth specification
;; for more information.
(def access-token-response (oauth/access-token consumer 
                                               request-token
                                               (:pin settings)))

;; get the activities
(println (fitbit/with-oauth consumer 
                    (:oauth_token access-token-response)
                    (:oauth_token_secret access-token-response)
                    (fitbit/activities)))
;; get weight
(println (fitbit/with-oauth consumer 
                    (:oauth_token access-token-response)
                    (:oauth_token_secret access-token-response)
                    (fitbit/weight "2011-02-26" "7d")))
