(ns esthatic.tmp
  (:require [clojure.string :as str]
            [gardner.core :as css]
            [hiccup.page :as hp]
            [ring.middleware.resource :as rmr]
            [ring.middleware.defaults :as rmd]
            [ring.middleware.reload :as rml]
            [hiccup.core :refer [html]]
            [org.httpkit.server :as srv]
            [esthatic.data :as data]
            [esthatic.bootstrap :as bootstrap]
            [esthatic.generator :as gen]
            [garden.core :as garden]
            [garden.units :as units]
            [garden.stylesheet :as gs]
            [clojure.walk :as walk]
            [route-map.core :as rt]
            [markdown.core :as md]
            [clojure.java.io :as io]))

(defn svg [opts path]
  (slurp (str  "resources/assets/" (name path) ".svg")))

(defn markdown [opts str]
  (if (re-matches #".*\.md" str)
    (md/md-to-html-string (slurp (io/resource str)))
    (md/md-to-html-string str)))

(defn md-page [fl]
  (fn [opts]
    (println "HERE")
    [:div.page
     [:$md fl]]))

(defn style [opts garden-rules]
  (css/config (let [cfg (:styles opts)] (if (var? cfg) (var-get cfg) cfg)))
  [:style {:type "text/css"}
   (css/css garden-rules)])

(defn media [opts rule garden-rules]
  (css/config (let [cfg (:styles opts)] (if (var? cfg) (var-get cfg) cfg)))
  [:style {:type "text/css"}
   (garden/css (gs/at-media rule garden-rules))])

(def css-s
  {:bootsrtrap "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
   :fontawesome "https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"})

(defn css-link [opts href]
  [:link {:href href :rel "stylesheet" :type "text/css"}])

(defn cdn-css [opts k]
  (css-link opts (get css-s k)))

(defn fa-icon [opts nm]
  [:i.fa {:class (str "fa-" (name nm))}])

(defn google-font [opts nm]
  (css-link
   opts
   (str "https://fonts.googleapis.com/css?family="
        (str/replace (name nm) #"-" "+")
        ":400,100,100italic,200,200italic,300,300italic,400italic,500,900italic,500italic,600,600italic,700,700italic,800,800italic,900")))

(defn inspect-data
  ([opts data] [:pre (data/to-yaml data)])
  ([opts data & ks] (inspect-data opts (get-in data ks))))


(defn br [opts & [x]]
  [:div {:style (garden/style
                 {:height
                  (units/px*
                   (or (get-in opts [:styles :$v]) 18)
                   (or x 1))})}])

(def pre-rules
  {:$style style
   :$media media
   :$svg svg
   :$md markdown
   :$gist (fn [op nm] [:script {:src (str "https://gist.github.com/" nm ".js")}])
   :$nav bootstrap/nav
   :$nav-item bootstrap/nav-item
   :$cdn-css cdn-css
   :$inspect inspect-data
   :$nbsp (fn [& _] "&nbsp;")
   :$icon fa-icon
   :$br br
   :$google-font google-font})

(defn pre-process [opts hic]
  (walk/prewalk (fn [x]
                  (if-let [h (and (vector? x) (get pre-rules (first x)))]
                    (apply h opts (conj (rest x)))
                    (if (and (vector? x) (.startsWith (name (first x)) "."))
                      (into [(keyword (str "div" (name (first x))))] (rest x))
                      x)))
                hic))

(defn http [opts hic]
  {:body    (html (pre-process opts hic))
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :status  200})

(def ^:dynamic *lang* nil)
(defn url [& parts]
  (let [root (or (System/getenv "SITE_URL") "/")
        last (last parts)
        path (if (map? last) (butlast parts) parts)
        lang (if *lang* (str (name *lang*) "/") "")
        params (if (map? last) last {})
        hash (if (map? last) (:# last) nil)]
    (str root lang (str/join "/" path) (if (re-matches #"^.*\..*$" last) "" ".html") (when hash (str "#" hash)))))


(defn build-stack
  "wrap h with middlewares mws"
  [h mws]
  ((apply comp mws) h))

(defn dispatch [{params :params uri :uri meth :request-method :as req
                 {routes :routes layout :layout :as opts} :es/opts}]
  (if-let [mtch (rt/match [meth (str/replace uri #".html$" "")] routes)]
    (let [layouts (filterv identity (mapv :layout (or (:parents mtch) [])))
          req (merge (update req :params merge (:params mtch))
                     {:data (data/load)})
          _ (println "!!!" mtch layouts)
          handler (build-stack (:match mtch) (or layouts []))]
      (http opts (handler req)))
    {:body (str uri " not found: " [meth (str/replace uri #".html$" "")] " " (pr-str (var-get routes))) :status 404}))

(defn mk-dispatch [opts]
  (fn [req] (dispatch (assoc req :es/opts opts))))


(defn mk-handler [{routes :routes port :port :as opts}]
  (-> (mk-dispatch opts)
      (rml/wrap-reload)
      (rmr/wrap-resource "assets")
      (rmd/wrap-defaults (merge rmd/site-defaults (merge opts {:security {:anti-forgery false}})))
      )
  )

(defn start [{routes :routes port :port :as opts}]
  (css/config (:styles opts))
  (srv/run-server (mk-handler opts) {:port (or port 9090)}))

(defn generate [config]
  (gen/generate
   (assoc config :dispatch (mk-handler config))))
