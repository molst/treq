treq resolves an [edn](https://github.com/edn-format/edn) tree of requests into function calls. This is useful when implementing servers that can serve an arbitrary combination of all the requests it supports at once, in order to minimize the number of ajax requests required to fulfil a higher order task.

Simple example request (could be big, deep and nested). This kind of structure is called a 'resolution' throughout the library:
```clj
(def initial-resolution
    {:source
        {:set-member {:person/nickname "frodo"}
         :get-members ["gandalf", "smeagol"]
         :get-lock "aaa"}})
```
Example response. The initial resolution and the resulting resolution has the same structure, which makes composition of many resolve operations easy.:
```clj
{:source
    {:set-member {:person/nickname "frodo"}
     :get-members ["gandalf", "smeagol"]
     :get-lock "aaa"}
 :result
    {:set-member {:person/nickname "frodo" :db/id 123}
     :get-members [{:person/nickname "gandalf" :db/id 124} {:person/nickname "smeagol" :db/id 125}]}
 :errors
    {:get-lock [{:message "aaa not found" :tags #{:db :not-found}}]}}
```
Example of tree request to function mappings. These mappings are called resolvers:
```clj
(defn resolvers [db-conn]
    [{:locations [[:set-member]]
      :access-fns [(fn [resolution nickname] (db/set-member db-conn nickname)]}
      
     {:locations [[:get-members]]
      :access-fns [(fn [resolution nicknames] (db/get-members db-conn nicknames)]}
      
     {:locations [[:get-lock]]
      :access-fns [(fn [resolution token] (db/get-lock db-conn token)]}])
```
This is how the core functionality of the library is used:
```clj
(:require [treq.core :as t])
(t/resolve initial-resolution resolvers) ;print, store, forward, or do some action using the result
```

By default, the source tree is not resolved in any particular order and might be run in parallel. A way to express order will be added on top of this when the need arize (without any need to break compatibility).

## Getting Started

Sorry, no wiki yet. See some example usage in [here](https://github.com/molst/annagreta/blob/master/src/annagreta/treq.clj).

## Project Maturity

Developed primarily for my personal use. Anything can change without notice.

## Artifacts

[Treq on Clojars](https://clojars.org/treq). If you are using Maven, add the following repository
definition to your `pom.xml`:

```xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

With Leiningen:
```
[treq "0.1"]
```

## Major dependencies

 * [Clojure](http://clojure.org/) (version 1.5.1)

## License

Copyright (C) 2013 [Marcus Holst](https://twitter.com/zolst)

Licensed under the [Eclipse Public License v1.0](http://www.eclipse.org/legal/epl-v10.html) (the same as Clojure).
