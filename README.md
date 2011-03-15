Purpose of this project
=======================

This library gives you a way to access the Fitbit API. Check out 
http://dev.fitbit.com to get more information about the API itself.

The main goal is easy access to your data on Fitbit. The OAuth 
authorization is part of this library, but relies on *clj-oauth*.

A simple client is included to show you how to use this library, but
the focus is on obtaining the data and providing it as clojure data
structures.

Usage
=====

Client
------

    lein deps
    lein repl
    (use 'fitbit)


License
=======

Copyright (C) 2011 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
