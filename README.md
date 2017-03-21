tools-timer
=========

[![Build Status](https://semaphoreci.com/api/v1/wickedshell/tools-timer/branches/master/badge.svg)](https://semaphoreci.com/wickedshell/tools-timer)

The tools-timer is a Java [Timer](https://docs.oracle.com/javase/7/docs/api/java/util/Timer.html) and [TimerTask](https://docs.oracle.com/javase/7/docs/api/java/util/TimerTask.html) wrapper for Clojure.

Installation
============

Add the following to your `project.clj`:

    [wickedshell/tools-timer "1.0.3-SNAPSHOT"]

Quick tutorial
==============

You can simply start a period task in every 5 seconds immediately like this:

```clojure
(use 'tools-timer)
(run-task! #(println "Say hello every 5 seconds.") :period 5000)
```

If you want delay the first run with 2 seconds：

```clojure
(run-task! #(println "Say hello after 2 seconds.") :delay 2000)
```

Use this if you want to execute a task at an absolute time：

```clojure
(run-task! #(println "Say hello at 2013-01-01T00:00:00 in beijing.") :at #inst "2013-01-01T00:00:00+08:00")
```

And, you can use the same timer in more than one tasks:

```clojure
(def greeting-timer (timer "The timer for greeting"))
(run-task! #(println "Say hello after 2 seconds.") :delay 2000 :by greeting-timer)
(run-task! #(println "Say hello every 5 seconds.") :period 5000 :by greeting-timer)
```

Finally, you can cancel a timer's tasks:

```clojure
(cancel! greeting-timer)
```

Documentation
=============

For more detailed information on **ruiyun.tools.timer**, please refer to the  [documentation].


Contributors
============
Forked from [ruiyun.tools.timer](https://github.com/Ruiyun/tools.timer), by [Ruiyun Wen](https://github.com/Ruiyun)

License
=======

Copyright (C) 2012 Ruiyun Wen

Distributed under the Eclipse Public License, the same as Clojure.
