---
title: "Websockets"
layout: :post
page-index: 3
section: "Getting Started"
---

Enabling websockets for your Macchiato server is quite simple. In the namespace `your-project.core`, there is a `server` 
function that configures and starts a server. This server instance needs to be wrapped with a function that adds 
websocket support, like this:

```
(-> (server/start opts)
    (server/start-ws (fn [{:keys [websocket uri]}]
                         (.send websocket (str "Websocket connected for path " uri))
                         (.on websocket
                              "message"
                              (fn [message]
                                ;; Bounce it back
                                (.send websocket (str "Client message was " message))))))
```

The handler function above receives a full Ring-style request map that also contains the websocket object. As you can see,
interaction with the websocket object is done through JS interop. For more details and examples on the websocket API, 
see [the docs for the ws module on NPM](https://www.npmjs.com/package/ws)