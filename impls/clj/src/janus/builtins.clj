(ns janus.builtins
  (:require [janus.ast :as ast]
            [janus.interpreter :as i]
            [janus.runtime :as rt]
            [janus.util :refer [fatal-error!]]
            [taoensso.telemere :as t]))

(defn validate-def [form]
  (when-not (<= 2 (count form) 3)
    (fatal-error! form "invalid args to def"))
  (when (and (= 3 (count form)) (not (string? (second form))))
    (fatal-error! form "Invalid docstring to def"))

  (let [name (first form)
        doc  (if (= 3 (count form)) (second form) "")
        body (last form)]
    [name body (assoc (meta form) :doc doc :source body)]))

(defn xprl-def [form env c]
  (let [[name body meta] (validate-def form)]
    (letfn [(next [cform]
              (let [def  (ast/->TopLevel name cform meta)
                    env' (assoc (:env (meta form)) name def)]
                (rt/emit c
                  (ast/keyword "env") env'
                  (ast/keyword "return") def)))]
      (t/event! :def/evalbody {:level :trace :data body})
      (i/eval body env (rt/withcc c :return next)))))

(def macros
  {"def" xprl-def
   "μ"   i/createμ
   ;; "withcc" withcc
   ;; "emit"   emit
   })

(def fns
  {"+*"     +
   "**"     *
   "-*"     -
   ;; REVIEW: Is there a better way to do data (non-branching) selection in
   ;; clojure?
   "select" (fn [p t f]
              (condp identical? p
                ast/t t
                ast/f f))})

(def base-env
  (merge
   (into {} (map (fn [[k v]] [(ast/symbol k) (ast/->PrimitiveMacro v)])) macros)
   (into {} (map (fn [[k v]] [(ast/symbol k) (ast/->PrimitiveFunction v)])) fns)))