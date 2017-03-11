(ns esthatic.hiccup
  (:require [hiccup.core :as hiccup]
            [gardner.core :as css]
            [markdown.core :as md]
            [clj-yaml.core :as yaml]
            [clojure.walk :as walk]
            [clojure.java.io :as io]
            [clojure.string :as str]))


(defn garden-css [req gss]
  [:style {:type "text/css"} (css/css gss)])

(defn css-link [opts href]
  [:link {:href href :rel "stylesheet" :type "text/css"}])

(def default-rules
  {:css garden-css})

(defn pre-process [req hic]
  (walk/prewalk (fn [x]
                  (if-let [h (and (vector? x) (or (get (:hiccup/macro req) (first x))
                                                  (get default-rules (first x))))]
                    (apply h req (conj (rest x)))
                    (if (and (vector? x) (.startsWith (name (first x)) "."))
                      (into [(keyword (str "div" (name (first x))))] (rest x))
                      x)))
                hic))

(defn html [req hic]
  ;; (println "macro" (:hiccup/macro req))
  (hiccup/html (pre-process req hic)))

(def yaml-hiccup
  {:yaml/debug (fn [r & keys]
                 [:pre
                  (when-let [d (get-in r (into [:data]  keys))]
                    (yaml/generate-string d :dumper-options {:flow-style :block}))])})


(defn get-data [r ks]
  (get-in r (into [:data]  keys)))

(def data-hiccup
  {:> (fn [r & keys]
        (when-let [d (get-data r keys)]
          (str d)))
   
   :data/table (fn [r & keys]
                 (if-let [d (get-data r keys)]
                   (let [hs (mapv first (first d))]
                     [:table.table
                      [:thead
                       [:tr (for [h hs] [:th (name h)])]]
                      [:tbody
                       (for [r d]
                         [:tr (for [h hs]
                                [:td (get r h "~")])])]])))})

(defn md-doc [r path]
  [:div.markdown
   (if-let [r (io/resource path)]
     (-> r (slurp) (md/md-to-html-string))
     [:b (str "No such file " path " :( ")])])

(defn md-str [r s]
  (md/md-to-html-string s))

(def markdown-hiccup
  {:md/doc md-doc
   :md/md md-str})

(defn fa-icon [nm]
  (if (or (string? nm) (keyword? nm))
    [:i.fa {:class (str "fa-" (name nm))}]
    [:p "Unexpected args passed to [:fa/icon]" [:pre (pr-str nm)]]))

(def fa-icon-hiccup
  {:fa/css (fn [r]
             (css-link r "https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"))
   
   :fa/icon (fn [r nm] (fa-icon nm))})

(defn get-data [req pth]
  (get-in req (into [:data] pth)))

(defn href [req path]
  (if-let [prefix (:es/prefix req)]
    (str prefix path)
    path))

(defn active-item [items path]
  (:item
   (reduce (fn [acc {href :href :as it}]
             (if (and (str/starts-with? path href)
                      (< (or (:length acc) 0) (count href)))
               {:item it :length (count href)}
               acc))
           {} items)))

(defn bs-menu [req opts]
  (let [data (get-data req (or (:source opts) [:menu]))
        items (:items data)
        active (active-item items (:uri req))]
    [:nav.navbar
     [:.container
      [:.navbar-header
       [:button.navbar-toggle.collapsed {:type "button" :data-toggle "collapse" :data-target "#navbar", :aria-expanded "false", :aria-controls "navbar"}
        [:span.sr-only "Toggle navigation"]
        [:span.icon-bar]
        [:span.icon-bar]
        [:span.icon-bar]]
       [:a.navbar-brand {:href (href req "/")} (:brand data)]]
      [:div#navbar.navbar-collapse.collapsed
       [:ul.nav.navbar-nav
        (for [i (:items data)]
          [:li {:class (when (= i active) "active")}
           [:a {:href (href req (:href i))}
            (when-let  [ic (:icon i)] [:span  (fa-icon ic) " "])
            (:text i)]])]
       #_[:ul.nav.navbar-nav.navbar-right right-items]]]]))
(def bootstrap-hiccup
  {:bs/css (fn [opts & _]
             [:link {:href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
                     :rel "stylesheet" :type "text/css"}])

   :bs/menu bs-menu})


(defn google-font [opts nm]
  (css-link
   opts
   (str "https://fonts.googleapis.com/css?family="
        (str/replace (name nm) #"-" "+")
        ":400,100,100italic,200,200italic,300,300italic,400italic,500,900italic,500italic,600,600italic,700,700italic,800,800italic,900")))

(def google-hiccup
  {:goog/font google-font})

