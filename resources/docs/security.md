---
title: "Security"
layout: :post
page-index: 2
section: "Getting Started"
---

### CSRF

Macchiato enabled CSRF by default. Requests such as form POSTs must contain the CSRF token.

First, you'll require the anti-forgery middleware namespace to access the token:

```clojure
(ns <project-name>.routes
  (:require
    ...
    [hiccups.runtime]
    [macchiato.util.response :as r]
    [macchiato.middleware.anti-forgery :as af])
  (:require-macros
    [hiccups.core :refer [html]])
```

The token can then be used as follows:

```clojure
(defn home [req res raise]
  (let [af-token af/*anti-forgery-token*]
    (->
       [:html
        [:body
         [:h2 "leave a message"]
         [:form {:method "POST" :action "/message"}
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

As you can see in the above example, the form contains a hidden field with the name `__anti-forgery-token`.
This field contains the value of the CSRF token that will be checked by the anti-forgery middleware when
the request is submitted to the server.

### Password Hashing

Password hashing is handled by the [macchiato-crypto](https://github.com/macchiato-framework/macchiato-crypto)
library.

First, you have to pick an encryption algorithm, either `bcrypt` or `scrypt`:

```clojure
(require '[macchiato.crypto.<algorithm> :as password])
```

Then use the `encrypt` function to apply a secure, one-way encryption
algorithm to a password:

```clojure
(def encrypted (password/encrypt "foobar"))
```

And the `check` function to check the encrypted password against a
plaintext password:

```clojure
(password/check "foobar" encrypted) ;; => true
```

The `encrypt` and `check` functions have async versions as well:

```clojure
(password/encrypt-async
  "secret"
  (fn [err result]
    (is (scrypt/check "secret" result))))

(password/check-async
  "secret"
  (password/encrypt "secret")
  (fn [err result]
    (is result)))
```

### Authentication

Authentication is handled by the [macchiato-auth](https://github.com/macchiato-framework/macchiato-auth) library.
`
