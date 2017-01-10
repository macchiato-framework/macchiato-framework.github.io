{:title "Getting Started"
 :layout :page
 :page-index 1
 :section "Getting Started"}


This tutorial will guide you through building a simple guestbook application using Macchiato. The guestbook allows users to leave a message and to view a list of messages left by others. The application will demonstrate the basics of HTML templating, database access, and project architecture.

### Requirements

You'll need the following prerequisites to get started:

* [JDK](http://www.azul.com/downloads/zulu/)
* [Leiningen](http://leiningen.org/)
* [Node.js](https://nodejs.org/)

## Creating a new application

Once you have Leiningen installed you can run the following commands in your terminal to initialize your application:

```
~ $ lein new macchiato guestbook
~ $ cd guestbook
```

The above will create a new template project.

```
~ $ lein build
```

This will start the Figwheel compiler for ClojureScript. Once the sources are compiler, open a new terminal window and
run the project with Node.js as follows:

```
~ $ cd guestbook
~ $ node target/out/guestbook.js
```

Once the server starts, you can visit your site at `localhost:3000`.

### Anatomy of a Macchiato application

The newly created application has the following structure:

```
├── .gitignore
├── LICENSE
├── Procfile
├── project.clj
├── README.md
├── env
│   ├── dev
│   │   ├── guestbook
│   │   │   └── app.cljs
│   │   └── user.clj
│   ├── prod
│   │   └── guestbook
│   │       └── app.cljs
│   └── test
│       └── guestbook
│           └── app.cljs
├── public
│   ├── css
│   │   └── site.css
│   └── index.html
├── src
│   └── guestbook
│       ├── config.cljs
│       ├── core.cljs
│       ├── middleware.cljs
│       └── routes.cljs
├── system.properties
└── test
    └── guestbook
        └── core_test.cljs
```

Let's take a look at what the files in the root folder of the application do:

* `Procfile` - used to facilitate Heroku deployments
* `LICENSE` - a license file for the project
* `README.md` - where documentation for the application is conventionally put
* `project.clj` - used to manage the project configuration and dependencies by Leiningen
* `.gitignore` - a list of assets, such as build generated files, to exclude from Git

### The Public Directory

This directory contains any public assets for the applications that will be served by the HTTP server.

### The Source Directory

All our code lives under the `src` folder. Since our application is called guestbook, this
is the root namespace for the project. Let's take a look at all the namespaces that have been created for us.

#### guestbook

* `core.clj` - this is the entry point for the application that contains the logic for starting and stopping the server
* `config.clj` - defines the configuration for the application based on the environment variables
* `routes.clj` - defines the HTTP routes for the application
* `middleware.clj` - a namespace that contains the common middleware for the HTTP routes

### The Env Directory

Environment specific code and resources are located under the `env/dev`, `env/test`, and the `env/prod` paths.
The `dev` configuration will be used during development, `test` during testing,
while the `prod` configuration will be used when the application is packaged for production.

The `user` namespace in the `env/dev` folder is used for starting the ClojureScript REPL.

The `app.cljs` file is the entry point for the application. The dev version of this file looks as follows:

```clojure
 (ns ^:figwheel-always guestbook.app
  (:require
    [guestbook.core :as core]
    [cljs.nodejs]
    [mount.core :as mount]))

(mount/in-cljc-mode)

(cljs.nodejs/enable-util-print!)

(.on js/process "uncaughtException" #(js/console.error %))

(set! *main-cli-fn* core/app)
```

You can see that this namespace sets environment properties such as the Mount configuration, printing, and error handling.
Finally, it sets the `core/app` function as the main function to be run by Node.js.

The production version of this namespace looks a little different:

```clojure
 (ns guestbook.app
  (:require
    [guestbook.core :as core]
    [cljs.nodejs]
    [mount.core :as mount]))

(mount/in-cljc-mode)

(cljs.nodejs/enable-util-print!)

(set! *main-cli-fn* core/main)
```

We're no longer using Figwheel, and we don't have a global exception handler here.

### The Test Directory

Here is where we put tests for our application, a couple of sample tests have already been defined for us.

### The Project File

As was noted above, all the dependencies are managed via updating the `project.clj` file.
The project file of the application we've created is found in its root folder and should look as follows:

```
(defproject guestbook "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :dependencies [[bidi "2.0.14"]
                 [com.cemerick/piggieback "0.2.1"]
                 [com.taoensso/timbre "4.7.4"]
                 [hiccups "0.3.0"]
                 [macchiato/core "0.1.2"]
                 [macchiato/env "0.0.5"]
                 [mount "0.1.10"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]]
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-doo "0.1.7"]
            [lein-npm "0.6.2"]
            [lein-figwheel "0.5.8"]
            [lein-cljsbuild "1.1.4"]
  [org.clojure/clojurescript "1.9.293"]]
  :npm {:dependencies [[source-map-support "0.4.6"]
                       [sqlite3 "3.1.8"]
                       [synchronize "2.0.0"]]}
  :source-paths ["src" "target/classes"]
  :clean-targets ["target"]
  :target-path "target"
  :profiles
  {:dev
   {:cljsbuild
    {:builds {:dev
              {:source-paths ["env/dev" "src"]
               :figwheel     true
               :compiler     {:main          guestbook.app
                              :output-to     "target/out/guestbook.js"
                              :output-dir    "target/out"
                              :target        :nodejs
                              :optimizations :none
                              :pretty-print  true
                              :source-map    true
                              :source-map-timestamp false}}}}
    :figwheel
    {:http-server-root "public"
     :nrepl-port 7000
     :reload-clj-files {:clj false :cljc true}
     :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

    :source-paths ["env/dev"]
    :repl-options {:init-ns user}}
   :test
   {:cljsbuild
    {:builds
     {:test
      {:source-paths ["env/test" "src" "test"]
       :compiler     {:main guestbook.app
                            :output-to     "target/test/guestbook.js"
                            :target        :nodejs
                            :optimizations :none
                            :source-map    true
                            :pretty-print  true}}}}
    :doo {:build "test"}}
   :release
   {:cljsbuild
    {:builds
     {:release
      {:source-paths ["env/prod" "src"]
       :compiler     {:main          guestbook.app
                      :output-to     "target/release/guestbook.js"
                      :target        :nodejs
                      :optimizations :simple
                      :pretty-print  false}}}}}}
  :aliases
  {"build" ["do"
            ["clean"]
            ["npm" "install"]
            ["figwheel" "dev"]]
   "package" ["do"
              ["clean"]
              ["npm" "install"]
              ["npm" "init" "-y"]
              ["with-profile" "release" "cljsbuild" "once"]]
   "test" ["do"
           ["npm" "install"]
           ["with-profile" "test" "doo" "node"]]})
```


As you can see the `project.clj` file is simply a Clojure list containing key/value pairs describing different aspects of the application.

The most common task is adding new libraries to the project. These libraries are specified using the `:dependencies` vector.
In order to use a new library in our project we simply have to add its dependency here.

NPM dependencies are managed under the `:npm` key, this is where you can add modules from [npmjs.com](https://npmjs.com).

The items in the `:plugins` vector can be used to provide additional functionality such as reading environment variables via `lein-cprop` plugin.

The `:profiles` contain a map of different project configurations that are used to initialize it for either development or production builds.

The `:cljsbuild` key under the profiles controls the compiler options for ClojureScript.

The `:aliases` key points to composite tasks needed for building, testing, and packaging the application for production.


### Project Anatomy Summary

As you can see the project structure is relatively straightforward:

* the `env` folder contains profile specific source code
* the `src` folder contains the application source code
* the `test` folder contains test code and test assets
* the `public` folder contains any static assets for the application
* the `projct.clj` file is used to manage the build configuration

## Developing the Application

### Creating the Database

We'll start by adding a SQLite database to the application. Let's open the `project.clj` file and add the following NPM dependencies:

```clojure
:npm {:dependencies [[source-map-support "0.4.6"]
                     [sqlite3 "3.1.8"] ;; <-- SQLite NPM module
                     [synchronize "2.0.0"] ;; <-- NPM fibers module
                     ]}
```

If you have `lein build` running, then you'll need to restart it in order to load the new dependency.

Next, let's add a new namespace file called `src/guestbook/db.cljs`. We'll start by adding the following namespace declaration:

```clojure
(ns guestbook.db
  (:require
    [cljs.nodejs :as node]
    [mount.core :refer [defstate]]))
```

With that in place, we can define vars for the synchronize and sqlite3 libraries:

```clojure
(def sync (node/require "synchronize"))

(def sqlite3 (node/require "sqlite3"))
```

We can now use Mount `defstate` to create the database resource:

```clojure
(defstate db
  :start (let [db (sqlite3.Database. ":memory:")]
           (.run
             db
             "CREATE TABLE guestbook
                         (id INTEGER PRIMARY KEY AUTOINCREMENT,
                          name VARCHAR(30),
                          message VARCHAR(200),
                          time TIMESTAMP DEFAULT CURRENT_TIMESTAMP);"))
  :stop (.close @db))
```

The state will initialize an in-memory database when it starts, and close it when it stops. Let's add the functions to
add messages to the guestbook and read the saved messages from it:

```clojure
(defn add-message [{:keys [name message]}]
  (.run @db "INSERT INTO guestbook (name, message) VALUES (?, ?)" #js [name message]))

(defn messages []
  (-> @db
      (.all "SELECT * FROM guestbook" (sync.defer))
      (sync.await)
      (js->clj :keywordize-keys true)))
```

Notice that we're using `sync.defer` and `sync.await` from the synchronize library we included to select the messages from the database.
This is necessary because the `all` method for the database driver is async and expects a callback.

### Updating Routes

Let's navigate to the `guestbook.routes` namespace to add the code for displaying the message form and saving the messages
entered by the users. First, we'll need to update the namespace declaration as follows:

```clojure
(ns guestbook.routes
  (:require
    [bidi.bidi :as bidi]
    [hiccups.runtime]
    [guestbook.db :as db]
    [macchiato.middleware.anti-forgery :as af]
    [macchiato.util.response :as r]
    [cljs.nodejs :as node])
  (:require-macros
    [hiccups.core :refer [html]]))
```

We've added a reference to the `db` namespace we created earlier, and the `anit-forgery` namespace. We'll need the latter
to create a CSRF anti-forgery token in our form.

Next, we'll create an instance of the `synchronize` modile:

```clojure
(def sync (node/require "synchronize"))
```

We can now update the `home` route as follows:

```clojure
(defn home [req res raise]
  (sync.fiber
    #(let [af-token af/*anti-forgery-token*]
       (->
         [:html
          [:body
           [:h2 "Messages"]
           [:ul
            (for [{:keys [name message time]} (db/messages)]
              [:li name " says " message " at " time])]
           [:hr]
           [:h2 "leave a message"]
           [:form {:method "POST" :action "/message"}
            [:input
             {:type        :text
              :name        "name"
              :placeholder "name"}]
            [:input
             {:type  "hidden"
              :name  "__anti-forgery-token"
              :value af-token}]
            [:input
             {:type        :text
              :name        "message"
              :placeholder "message"}]
            [:input
             {:type  :submit
              :value "add message"}]]]]
         (html)
         (r/ok)
         (r/content-type "text/html")
         (res)))))
```

If you'll recall, the `messages` in the `guestbook.db` namespace uses `sync.defer` and `sync.awat` calls to handle the
asynchronous call to the databse. These must be executed inside a `sync.fiber` call, and we'll have to add one in our route
handler function.

The rest of the code in the function generates the HTML content for the page. We call `db/messages` to retrieve the currently
stored messages and display them. Then we add a form that will allow the users to post a new message to the `/message` route.

Next, we'll add the handler for adding new messages that will look as follows:

```clojure
(defn message [req res raise]
  (db/add-message (select-keys (:params req) [:name :message]))
  (res (r/found "/")))
```

All we do here is call the `db/add-message` function with the form parameters and redirect the user back to the home page.

Finally, we'll have to add the new route to the `routes` definition as follows:

```clojure
(def routes
  ["/" {""        {:get home}
        "message" {:post message}}])
```

That's all there is to it.

### Packaging the Application

To package our application for production, we have to run the following command in the terminal:

```
lein package
```

This will compile the application and produce the `target/release/guestbook.js` file. It will also print the
following configuration to the terminal:

```javascript
{
  "private": true,
  "name": "guestbook",
  "description": "FIXME: write this!",
  "version": "0.1.0-SNAPSHOT",
  "dependencies": {
    "random-bytes": "1.0.0",
    "multiparty": "4.1.2",
    "source-map-support": "0.4.6",
    "ws": "1.1.1",
    "sqlite3": "3.1.8",
    "cookies": "0.6.2",
    "etag": "1.7.0",
    "synchronize": "2.0.0",
    "qs": "6.3.0",
    "content-type": "1.0.2",
    "url": "0.11.0",
    "simple-encryptor": "1.1.0",
    "accepts": "1.3.3",
    "concat-stream": "1.5.2"
  },
  "main": "index.js",
  "directories": {
    "test": "test"
  },
  "devDependencies": {},
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1"
  },
  "keywords": [],
  "author": "",
  "license": "ISC"
}
```

You'll have to save this configuration in a file called `package.json` and publish it along with your application.




