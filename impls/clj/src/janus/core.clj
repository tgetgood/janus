(ns janus.core
  (:require [clojure.pprint :as pp]
            [janus.ast :as ast]
            [janus.reader :as r]))

(def srcpath "../../src/")
(def corexprl (str srcpath "core.xprl"))

(def cx (r/file-reader corexprl))

(def s
  (r/string-reader "[0x4e [{:asd 34} [#{:sd 34}]] \n;comment\n #_(f x y [23]) ~(bob x [1 2 3])]"))

(def forms (r/read-file corexprl))