---
title: "Sessions and Cookies"
layout: :post
page-index: 2
section: "Getting Started"
---

## Sessions

Macchiato defaults to using in-memory sessions.

The session middleware is initialized in the `<app>.middleware` namespace by the `wrap-defaults`
function. Session timeout is specified in second and defaults to 30 minutes of inactivity.

We can easily swap the default memory store for a different one, such as a cookie store.
Below, we explicitly specify the `ring.middleware.session.cookie/cookie-store` with the name `example-app-session` as our session store:

```clojure
(wrap-defaults
  (-> site-defaults
      (assoc-in [:security :anti-forgery] false)
      (assoc-in [:session :store] (cookie-store))
      (assoc-in [:session :cookie-name] "example-app-sessions")))
```

We can also specify the maximum age for our session cookies using the `:max-age` key:

```clojure
(wrap-defaults
  (-> site-defaults
      (assoc-in [:security :anti-forgery] false)
      (assoc-in [:session :store] (cookie-store))
      (assoc-in [:session :cookie-attrs] {:max-age 10})))
```

When using cookie store it is also important to specify a secret key (16 characters) for cookie encryption. Otherwise a random one will be generated each time application is started and sessions created before will be lost.

```clojure
(wrap-defaults
  (-> site-defaults
      (assoc-in [:security :anti-forgery] false)
      (assoc-in [:session :store] (cookie-store {:key "BuD3KgdAXhDHrJXu"}))
      (assoc-in [:session :cookie-name] "example-app-sessions")))
```

### Accessing the session

Ring tracks sessions using the request map and the current session will be found under the `:session` key.
Below we have a simple example of interaction with the session.

```clojure
(ns <project-name>.routes
  (:require
   [macchiato.util.response :refer [response]]))

(defn set-user! [{:keys [params session]} res raise]
  (let [id (:id params)]
    (-> (response (str "User set to: " id))
        (assoc :session (assoc session :user id))
        (response/content-type "text/plain")
        (response/ok)
        (res))))

(defn remove-user! [{session :session} res raise]
  (-> (response "User removed")
      (assoc :session (dissoc session :user))
      (response/content-type "text/plain")
      (response/ok)
      (res)))

(defn clear-session! [req res raise]
  (-> (response "Session cleared")
      (dissoc :session)
      (response/content-type "text/plain")
      (response/ok)
      (res)))

(def routes
[["/" {"login/:id" set-user!
       "remove" remove-user!
       "logout" clear-session!}]])
```

### Flash sessions

Flash sessions have a lifespan of a single request, these can be accessed using the `:flash` key instead of the `:session` key used for regular sessions.

## Cookies

Cookies are found under the `:cookies` key of the request, eg:

```clojure
{:cookies {"username" {:value "Bob"}}}

```

Conversely, to set a cookie on the response we simply update the response map with the desired cookie value:

```clojure
(-> "response with a cookie" response (assoc-in [:cookies "username" :value] "Alice"))
```

Cookies can contain the following additional attributes in addition to the `:value` key:

* :domain - restrict the cookie to a specific domain
* :path - restrict the cookie to a specific path
* :secure - restrict the cookie to HTTPS URLs if true
* :http-only - restrict the cookie to HTTP if true (not accessible via e.g. JavaScript)
* :max-age - the number of seconds until the cookie expires
* :expires - a specific date and time the cookie expires
