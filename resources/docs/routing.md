---
title: "Routing"
layout: :post
page-index: 3
section: "Getting Started"
---

[Bidi](https://github.com/juxt/bidi) is used as the default routing library. However, you can easily swap
for a number of other libraries, such as [router](https://github.com/darkleaf/router).

The routing logic is found in the `<project-name>.routes` namespace. The `router` function is responsible
for selecting the handler for the route based on the URI:

```clojure
(defn router [req res raise]
  (if-let [{:keys [handler route-params]} (bidi/match-route* routes (:uri req) req)]
    (handler (assoc req :route-params route-params) res raise)
    (not-found req res raise)))
```

The `router` will attempt to match the URI using `bidi/match-route*` function against the routes:

```clojure

(def routes
  ["/" {:get home}])
```

When no routes are found, then the `not-found` route will be called:

```clojure
(defn not-found [req res raise]
  (-> (html
        [:html
         [:body
          [:h2 (:uri req) " was not found"]]])
      (r/not-found)
      (r/content-type "text/html")
      (res)))
```

A route handler function must accept three arguments as seen above:

* req - the request map containing the parsed request
* res - a function that will handle sending the response to the server asynchronously
* raise - a function that should be called when you wish to propagate the error to the default error handler


## route-map



