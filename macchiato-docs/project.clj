(defproject macchiato-docs "0.1.0"
            :description "ClojureScript micro-framework"
            :url "https://github.com/macchiato-framework"
            :license {:name "MIT License"
                      :url "https://opensource.org/licenses/MIT"}
            :dependencies [[org.clojure/clojure "1.8.0"]
                           [markdown-clj "0.9.91"
                            :exclusions [com.keminglabs/cljx]]
                           [ring/ring-devel "1.3.2"]
                           [compojure "1.3.4"]
                           [ring-server "0.4.0"]
                           [selmer "1.10.2"]
                           [clj-text-decoration "0.0.3"]
                           [io.aviso/pretty "0.1.18"]
                           [cryogen-markdown "0.1.1"]
                           [cryogen-core "0.1.21"]]
            :plugins [[lein-ring "0.8.13"]]
            :main cryogen.compiler
            :ring {:init cryogen.server/init
                   :handler cryogen.server/handler})
