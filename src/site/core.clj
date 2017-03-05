(ns site.core
  (:require
   [esthatic.core :as es]
   [esthatic.data :as esd]
   [esthatic.hiccup :as esh]
   [clojure.string :as str]
   [clj-yaml.core :as yaml]
   [clojure.java.shell :as sh]
   [clojure.java.io :as io]
   [clojure.string :as s]))

(defn footer [{data :data :as opts}]
  [:div#footer
   [:css
    [:#footer {:background-color "#666"
               :color "white"
               :$margin [10 0 0]
               :$height 20 :text-align "center"
               :$padding [3 0] }
     [:img {:height "60px"}]

     [:.footer-container {:$width 40 :text-align "center" :margin "0 auto"}]
     [:p {:$text 1}]]]

   [:div.footer-container
    [:h2 [:img.logo {:src (es/href opts "/imgs/logo.png")}] [:> :brand]]
    [:h4 [:> :moto]]
    ]])

(def style
  [:body {:font-family "'Exo 2'"
          :$text [1 1.5]
          :font-weight "300"}
   [:p {:$push-bottom 1}]
   [:.block {:margin-top "40px"}]
   [:nav
    [:&.navbar-default {:background-color "white"
                        :padding-top "10px"
                        :border-bottom "1px solid #ddd"}]
    [:&.navbar {:margin 0} [:.navbar-brand {:font-weight "bold"}]]
    [:a {:color "#666"}]
    ]])

(defn layout [h]
  (fn [{data :data params :params :as opts}]
    [:html
     [:head
      [:bs/css]
      [:fa/css]
      [:goog/font :Exo-2]
      [:goog/font :Open-Sans]
      [:link {:rel "icon" :href (es/href opts "/imgs/icon.png") :type "image/x-icon"}]
      [:css style]
      [:css
       [:body
        [:nav.navbar
         {:border-radius 0}
         [:li [:a {:color "#666"
                   :border-bottom "3px solid transparent"}
               [:&:hover {:border-color "#4cc61e"
                          :color "black"
                          :background "transparent"}]]]]
        (when (:invert-menu params)
          [:&
           [:nav.navbar
            {:background (str  "url(" (es/href opts "/imgs/bg.png") ") #583426")
             :margin-bottom "40px"}
            [:.navbar-brand {:color "white"
                             :background-image (str  "url(" (es/href opts "/imgs/logo.png") ")")
                             :background-size "40px"
                             :background-repeat "no-repeat"
                             :padding-left "51px"
                             :background-position-y "9px"}]
            [:li.active [:a {:border-bottom "3px solid #4cc61e"}]]
            [:li [:a {:color "white"}
                  [:&:hover {:background-color "#291e1a"
                             :color "white"}]]
             ]]])]]]
     [:body
      [:bs/menu {:source [:menu]}]
      (h opts)
      (footer opts)]]))

(defn doc-layout [h]
  (fn [opts]
    [:div.container (h opts)]))


(defn moto [{data :data :as opts}]
  [:div#moto
   [:css
    [:#moto {:margin 0
             :background-color "#7B3F00"
             :color "white"
             :border-bottom "1px solid #eee"}
     [:#coffee
      {:padding "180px 0 200px"
       :margin "0 auto"
       :text-align "center"
       :background-image (str  "url(" (es/href opts "/imgs/bg.png") ")")
       :height "500px"
       :background-color "#583426"
       :background-attachment "fixed"
       ;; :background-position "center"
       :background-repeat "repeat"
       :background-size "initial"
       :color "white"}
      [:.logo {:height "105px"
               :vertical-align "top"
               :margin-right "20px"
               :display "inline-block"}]
      [:.text {:display "inline-block"
               :vertical-align "top"}
       [:.brand {:font-size "60px"
                 :display "block"}]
       [:.moto  {:font-size "24px"
                 :font-weight "300"
                 :display "inline-block"}]]]]]

   [:div#coffee
    [:h1
     [:img.logo {:src (es/href opts "/imgs/logo.png")}]
     [:span.text
      [:span.brand "MACCHIATO"]
      [:span.moto "ClojureScript arrives on server"]]]]])

(defn icon [nm]
  [:i {:class (str "fa fa-" (name nm))}])


(defn $index [{data :data :as opts}]
  [:div#index
   (moto opts)
   [:div.container
    [:div#features
     [:css
      [:#features {:margin "40px 0 60px"}
       [:img {:width "40px" :opacity 0.7 :margin-right "10px"}]]]
     [:div.row
      (for [{:keys [title text img]} (get-in data [:features])]
        [:div.col-md-6
         [:h3 [:img {:src (es/href opts (str "/imgs/" img))}] title]
         [:p text]])]]
    [:div.block [:md/doc "index.md"]]]])


(defn $doc [{{id :doc-id} :params data :data uri :uri :as opts}]
  [:div.container-fluid
   [:css
    [:.docs-nav 
     [:li
      {:border-left "4px solid #eee"}
      [:a {:color "#888"
           :padding "5px 20px"}]
      [:&.active {:border-left "4px solid #777"}
       [:a {:color "#333"}]]]]]
   [:.row 
    [:.col-md-2.docs-nav
     [:h3 "Documentation"]
     [:ul.nav
      (for [[bn f] (sort-by (fn [[k v]] (:page-index v)) (:files data))]
        [:li {:class (when (str/ends-with? uri bn) "active")}
         [:a {:href bn } (or (:title f) bn)]])]]
    [:.col-md-9
     [:md/md (get-in data [:files id :content])]]]])

(def meta-start-regex  #"^---")

(defn read-file-meta [x]
  (with-open [rdr (io/reader x)]
    (let [lines (line-seq rdr)]
      (when (re-find meta-start-regex (first lines))
        (loop [acc []
               [line & lines] (rest lines)]
          (when line
            (if (re-find meta-start-regex line)
              (merge (or (yaml/parse-string (str/join "\n" acc)) {})
                     {:content (str/join "\n" lines)})
              (recur (conj acc line) lines))))))))

(defn read-files [dir]
  (->>
   (file-seq (io/file (io/resource dir)))
   (filter #(.isFile %))
   (mapv (fn [x]
           (merge
            {:name (.getName x)
             :basename (str/replace (.getName x) #"\.md" "") 
             :path (.getPath x)}
            (or (read-file-meta (.getPath x)) {}))))
   (reduce (fn [acc f] (assoc acc (:basename f) f)) {})))

(defn with-ls [h]
  (fn [req]
    (->>
     (read-files "docs")
     (assoc-in req [:data :files])
     (h))))



(def routes
  {:es/mw [(esd/with-yaml "data.yaml")
           #'es/hiccup-mw
           #'layout]

   :. #'$index
   "index" {:. #'$index}
   "docs" {:es/mw [#'with-ls]
           :es/params {:invert-menu true}
           :doc-id (fn [] (mapv :basename (vals (read-files "docs"))))
           [:doc-id]
           {:. #'$doc}}
   })

(def config
  {:port 8888
   :repo "https://github.com/niquola/macchiato-framework.github.io"
   :es/hiccup [#'esh/bootstrap-hiccup
               #'esh/yaml-hiccup
               #'esh/google-hiccup
               #'esh/data-hiccup
               #'esh/fa-icon-hiccup
               #'esh/markdown-hiccup]
   :es/routes #'routes })

;; (defn -main [] (es/generate config))

(defn start []
  (es/restart config))

(defn generate [config]
  (let [prefix (or (:prefix config) (System/getenv "SITE_PREFIX") "/macchiato-site")]
    (es/generate (assoc config :es/prefix prefix))))

(defn publish []
  (println (sh/sh "bash" "-c" "rm -rf dist"))
  (println  (sh/sh "bash" "-c" (str "git clone " (:repo config) " dist")))
  (generate (merge config {:prefix ""})))

(defn local-publish []
  (println (sh/sh "bash" "-c" "rm -rf dist"))
  (println  (sh/sh "bash" "-c" (str "git clone " (:repo config) " dist")))
  (println (sh/sh "bash" "-c" "rm -rf dist/.git"))
  (generate (merge config {:prefix "/macchiato-site"}))
  (println (sh/sh "bash" "-c" "cd dist && git init && git add -A  && git commit -a -m 'build' && git remote add origin https://github.com/niquola/macchiato-site.git && git checkout -b gh-pages && git push -f origin gh-pages")))

(comment
  (println (sh/sh "bash" "-c" "cd dist && git init && git add -A  && git commit -a -m 'build' && git remote add origin https://github.com/niquola/macchiato-site.git && git checkout -b gh-pages && git push -f origin gh-pages"))
  (es/restart config)

  (generate (merge config {:prefix "/"}))
  (publish)
  (local-publish)
  )

  
