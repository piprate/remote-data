(ns piprate.remote-data
  (:require
   [clojure.spec.alpha :as s]
   [cognitect.anomalies :as anom]))

(s/def ::status #{::not-requested
                  ::loading
                  ::error
                  ::success})

(s/def ::error ::anom/anomaly)

(s/def ::remote-data (s/keys :req [::status]))

(s/fdef loading :ret ::remote-data)
(s/fdef success :ret ::remote-data)
(s/fdef error :ret ::remote-data)

(defn not-requested
  ([]
   (not-requested nil))
  ([current-state]
   (merge current-state {::status ::not-requested})))

(defn loading
  ([]
   (loading nil))
  ([current-state]
   (loading current-state nil))
  ([current-state extra-data]
   (dissoc (merge current-state extra-data {::status ::loading})
           ::error
           ::data)))

(defn success
  ([data]
   (success nil data))
  ([current-state data]
   (merge current-state {::status ::success
                         ::data   data})))

(defn error
  ([err]
   (error nil err))
  ([current-state err]
   (merge current-state {::status ::error
                         ::error  err})))

(defn response
  ([resp]
   (response nil resp))
  ([current-state resp]
   (if (s/valid? ::error resp)
     (error current-state resp)
     (success current-state resp))))

(defn loading?
  [rd]
  (= ::loading (::status rd)))

(defn error?
  [rd]
  (= ::error (::status rd)))

(defn not-requested?
  [rd]
  (= ::not-requested (::status rd)))

(defn success?
  [rd]
  (= ::success (::status rd)))
