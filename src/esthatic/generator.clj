(ns esthatic.generator
  (:require [clojure.string :as str]
            [clojure.java.shell :as sh]
            [me.raynes.fs :as fs]))

(defn dump [path res]
  (let [dir (butlast path)
        dir-path (str "dist/" (str/join "/" dir))
        fl  (or (last path) "index")
        fl-path (str dir-path "/" fl ".html")]
    (println "page: " fl-path)
    (fs/mkdir dir-path)
    (spit fl-path res)))

(defn safe-dispatch [& args])

(defn devar [v] (if (var? v) (var-get v) v))

(defn *generate [{path :path params :params limit :limit :as ups} {routes :es/routes :as config}]
  (when (< limit 0) (throw (Exception. "ups limit recur")))
  (doseq [[k v] (devar routes)]
    (println path k v)
    (cond
      (= :. k) (let [uri (str/join "/" path)
                       req {:uri uri :request-method k}
                       res (try
                             ((:dispatch config) req)
                             (catch Exception e
                               (println "ERROR: " req (.toString e))
                               {:body (str "<pre>" e "</pre>")}))]
                   (dump path (:body res)))
      (vector? k) (let [k (first k)
                        gen (get routes k)]
                    (doseq [pv (gen)]
                      (println "Generate subpage:" path  pv)
                      (*generate {:path (conj path pv) :params (assoc params k pv) :limit (dec limit)} (assoc config :es/routes v))))
      (keyword? k) "nop"
      :else (*generate {:path (conj path k) :params params :limit (dec limit)} (assoc config :es/routes v)))))


(defn generate [config]
  (println "Generating into dist:")
  ;; (fs/delete-dir "dist")
  ;; (fs/mkdir "dist")
  (doseq [f (fs/glob "resources/public/*")]
    (println "assets: " (fs/base-name f))
    ((if (fs/directory? f) fs/copy-dir fs/copy)
     (.getPath f) (str "dist/" (fs/base-name f))))

  (*generate {:path [] :params {} :limit 100} config)
  (println "Done!"))


