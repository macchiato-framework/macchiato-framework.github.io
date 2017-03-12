---
title: "HTML Templating"
layout: :post
page-index: 2
section: "Getting Started"
---

[Hiccups](https://github.com/macchiato-framework/hiccups) is representation of HTML using Clojure data structures.
The advantage of using Hiccups is that we can use the full power of Clojure to generate and manipulate our markup.
With Hiccups, you don't have to learn a separate DSL for generating HTML with its own rules and quirks.

In Hiccups, HTML elements are represented by Clojure vectors and the attributes are represented using maps.
The structure of the HTML element looks as follows:

```clojure
[:tag-name {:attribute-key "attribute value"} tag-body]
```

For example, if we wanted to create a div with a paragraph in it, we could write:

```clojure
[:div {:id "hello", :class "content"} [:p "Hello world!"]]
```

which corresponds to the following HTML:

```xml
<div id="hello" class="content"><p>Hello world!</p></div>
```

Hiccups provides shortcuts for setting the id and class of the element, so instead of what we wrote above we could simply write:

```clojure
[:div#hello.content [:p "Hello world!"]]
```
