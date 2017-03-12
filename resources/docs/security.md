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



