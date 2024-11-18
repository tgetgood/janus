(ns janus.util
  (:require [taoensso.telemere :as t]))


(defn form-log! [form level msg]
  (t/log! {:level level
           :data  (assoc (select-keys (meta form) [:string :file :line :col])
                         :form form)}
                msg))

(defn form-error! [form msg]
  (form-log! form :error msg))
