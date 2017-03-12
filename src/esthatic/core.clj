(ns esthatic.core
  (:require [clojure.string :as str]
            [hiccup.page :as hp]
            [ring.middleware.resource :as rmr]
            [ring.middleware.defaults :as rmd]
            [ring.middleware.stacktrace :as rms]
            [ring.middleware.reload :as rml]
            [esthatic.hiccup :as hiccup]
            [org.httpkit.server :as srv]
            [esthatic.data :as data]
            [esthatic.generator :as gen]
            [clojure.walk :as walk]
            [route-map.core :as rt]
            [clojure.java.io :as io]))

(defn build-stack [h mws]
  ((apply comp mws) h))

(defn hiccup-mw [h]
  (fn [req]
    (let [res (h req)
          macro (apply merge (map (fn [x] (if (var? x) (var-get x) x)) (get req :es/hiccup)))]
      {:body    (hiccup/html (update req :hiccup/macro merge macro) res)
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :status  200})))
  
(defn dispatch [{params :params uri :uri
                 routes :es/routes :as req}]
  (if-let [rt (rt/match (str/replace uri #".html$" "") (if (var? routes) (var-get routes) routes))]
    (let [mws (->> (or (:parents rt) [])
                   (mapcat :es/mw)
                   (filterv identity))
          rt-params (->> (or (:parents rt) [])
                         (map :es/params)
                         (filterv identity)
                         (reduce merge {}))
          req (merge (update req :params merge (merge (:params rt) rt-params)))
          handler (build-stack (:match rt) mws)]
      (handler req))
    (do
      {:body     (str (str/replace uri #".html$" "") " not found: " )
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :status  404})))

(defn mk-handler [opts]
  (-> (fn [req] (dispatch (merge req opts)))
      (rml/wrap-reload)
      (rmr/wrap-resource "assets")
      (rms/wrap-stacktrace-web)
      (rmd/wrap-defaults (merge rmd/site-defaults (merge opts {:security {:anti-forgery false}})))))

(defn href [req path]
  (if-let [prefix (:es/prefix req)]
    (str prefix path)
    path))



(defonce server (atom nil))

(defn restart [opts]
  (when-let [s @server] (s) (reset! server nil))
  (reset! server (srv/run-server (mk-handler opts) {:port (or (:port opts) 9090)})))

(defn generate [opts]
  (gen/generate
   (assoc opts :dispatch (mk-handler opts))))


