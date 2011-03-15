(ns fitbit.fitbitutil)

(def today (java.util.Date.))

(defn today-as-str
  [] (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") today))

(defn get-url
  ([action date] (str "http://api.fitbit.com/1/user/-/" action "/date/" date ".json")))
