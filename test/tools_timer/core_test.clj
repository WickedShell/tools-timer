(ns tools-timer.core-test
    (:require [clojure.test :refer :all]
              [tools-timer.core :refer :all])
    (:import [java.util Date Timer]))

(deftest single-run-task
  (testing "Fire a one shot run task, with and without delays"
           (let [run? (atom false)
                 timer-name "named-timer-test"
                 named-timer (timer :name timer-name)
                 thread-name-1 (promise)
                 thread-name-2 (promise)]
             ; validate having timer objects
             (is (instance? Timer (timer)) "Failed to create a timer")
             (is (instance? Timer named-timer) "Failed to create a timer")

             ; run immediately
             (run-task! #(reset! run? true))
             (Thread/sleep 100)
             (is @run? "No arguments, expected to be a run immediately task")

             ;run after a delay
             (reset! run? false)
             (run-task! #(reset! run? true) :delay 200)
             (Thread/sleep 100)
             (is (not @run?) "Task completed prematruely")
             (Thread/sleep 200)
             (is @run? "Task didn't fire within 100 ms of expected time")

             ; run immediately on a timer
             (run-task! #(deliver thread-name-1 (.getName (Thread/currentThread))) :by named-timer)
             (run-task! #(deliver thread-name-2 (.getName (Thread/currentThread))) :delay 100 :by named-timer)
             (Thread/sleep 200)
             (is (realized? thread-name-1) "No arguments, expected to be a run on our existing timer")
             (is (realized? thread-name-2) "No arguments, expected to be a run on our existing timer")
             (is (= @thread-name-1 @thread-name-2 timer-name) "Run on different threads")

             )))

(deftest cancel-task
  (testing "Cancel a task after it has been created"
           (let [not-run? (atom true)]
             ; test a one shot task
             (cancel! (run-task! #(reset! not-run? false) :delay 100))
             (Thread/sleep 200)
             (is @not-run? "Cancel a one shot task"))

           ; cancel after a delay
           (let [not-run? (atom true)
                 task (run-task! #(reset! not-run? false) :delay 200)]
             (Thread/sleep 100)
             (cancel! task)
             (Thread/sleep 200)
             (is @not-run? "Task didn't cancel")
             )))

(deftest daemon-timer-test
  (testing "Create and run daemon timers"
           (let [daemon-is-daemon? (atom nil)
                 non-daemon-is-daemon? (atom nil)
                 daemon-timer (timer :is-daemon true)
                 non-daemon-timer (timer :name "named")]
             (is (instance? Timer daemon-timer))
             (is (instance? Timer non-daemon-timer))
             (run-task! #(reset! daemon-is-daemon? (.isDaemon(Thread/currentThread))) :by daemon-timer)
             (run-task! #(reset! non-daemon-is-daemon? (.isDaemon(Thread/currentThread))) :by non-daemon-timer)
             (Thread/sleep 100)
             (is @daemon-is-daemon? "Failed to make a daemon timer")
             (is (= @non-daemon-is-daemon? false) "Failed to make non daemon timer"))))

(deftest periodic-test
  (testing "Run a periodic task and cancel it"
           (let [run-count (atom 0)
                 period-timer (run-task! #(swap! run-count inc) :period 100)]
             (Thread/sleep 450)
             (cancel! period-timer)
             (is (= @run-count 5))
             (Thread/sleep 100)
             (is (= @run-count 5) "Timer is still running"))))

(deftest absolute-time-test
  (testing "Running a task at a specified time"
           (let [run-date (promise)
                 now (new Date)
                 request-date (new Date (+ 200 (.getTime now)))]
             (run-task! #(deliver run-date (new Date)) :at request-date)
             (Thread/sleep 400)
             (is (realized? run-date) "Task never ran")
             (is (< (Math/abs (- (.getTime ^Date @run-date) (.getTime request-date))) 20) "Unable to run the task within a 25ms window"))))

(deftest task-cancel
  (testing "Creates and cancels multiple tasks on a single timer"
           (let [task-timer (timer)
                 run-count (atom 0)
                 task-1 (timer-task #(swap! run-count inc))
                 task-2 (timer-task #(swap! run-count inc))]
             (run-task! task-1 :by task-timer :period 100)
             (run-task! task-2 :by task-timer :delay 200)
             (Thread/sleep 100)
             (cancel-task! task-2)
             (Thread/sleep 350)
             (cancel-task! task-1)
             (is (= @run-count 5) "Failed to cancel enough tasks"))))

(deftest timer-inputs
  (testing "Validates the inputs to timer"
           (is (thrown? AssertionError (timer :name :name)))
           (is (thrown? AssertionError (timer :is-daemon :false)))))

(deftest timer-task-inputs
  (testing "Validates the pre/post conditions"
           (is (thrown? AssertionError (timer-task nil)))
           (is (thrown? AssertionError (timer-task #(println "test") :on-exception "fails")))))

(deftest run-task-inputs
  (testing "Validates the inputs to run-task!"
           (is (thrown? AssertionError (run-task! nil)))
           (is (thrown? AssertionError (run-task! #(println "test") :by :timer)))
           (is (thrown? AssertionError (run-task! #(println "test") :at 11325)))
           (is (thrown? AssertionError (run-task! #(println "test") :delay -1)))
           (is (thrown? AssertionError (run-task! #(println "test") :delay :delay)))
           (is (thrown? AssertionError (run-task! #(println "test") :period 0)))
           (is (thrown? AssertionError (run-task! #(println "test") :period :period)))))
