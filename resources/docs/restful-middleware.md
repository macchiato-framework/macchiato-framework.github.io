---
title: "RESTful Middleware"
layout: :post
page-index: 3
section: "Getting Started"
---

Macchiato provides middleware for automatically negotiating content type for the requests and responses.

The middleware looks at the `Content-Type` header in the request to infer how the request should be
deserialized, and the `Accept` header to find the appropriate serialization format for the response.

The middleware can be required as follows:

```clojure
(ns <project-name>.routes
  (:require
   ...
   [macchiato.util.response :as r]
   [macchiato.middleware.restful-format as rf]))
```

Let's write an echo request handler function:

```clojure
(defn echo [req res raise]
  (r/ok {:message (->req :body :message)
         :name (-> req :body :name)}))
```

We can wrap this function with the RESTful middleware:

```clojure
(def routes
  ["/" {:get (rf/wrap-restful-format echo)}])
```

Given a request such as:

```clojure
{:uri "/"
 :body "{\"name\":"\Bob\", \"messsage\":\"Hello\"}"
 :headers {"content-type" "application/javascript"
           "accept" "application/javascript"}}
```

The middleware will parse the JSON body of the request. Our handler will receive
a Clojure data structure that's been deserialized from JSON. The handler response
will in turn be automatically serialized to JSON.

At this time `wrap-restful-format` supports the following types out of the box:

* "application/json"
* "application/transit+json"


### Adding Custom Types

Custom types can be added by implementing the `deserialize-request` and `serialize-response` multimethods.
See the JSON multimethods below as an example:

```clojure
(defmethod deserialize-request "application/json"
  [{:keys [body keywordize?]}]
  (js->clj (js/JSON.parse body) :keywordize-keys keywordize?))

(defmethod serialize-response "application/json"
  [{:keys [body]}]
  (js/JSON.stringify (clj->js body)))
```

Once you've added the multimethods, you must provide the `:content-types` and the `:accept-types` keys
when wrapping the middleware. These keys should each point to a set of content types that you wish to work with.



