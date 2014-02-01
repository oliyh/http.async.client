(ns http.async.client.test-websockets
  "Tests for websocket clients."
  (:require [clojure.test :refer :all]
            [http.async.client :as client]
            [org.httpkit.server :as h]
            [org.httpkit.timer :as timer])
  (:import [com.ning.http.client AsyncHttpClient]
           [java.util.concurrent TimeUnit LinkedBlockingQueue]))


(def ^:dynamic *client*)
(def ^:dynamic *server*)

(defn handler [request]
  (let [path    (:uri request)
        key     (get-in request [:headers "sec-websocket-key"])
        channel (:async-channel request)]
    (cond
     
      ;; Send an invalid Upgrade header
      (= path "/bad-upgrade-header")
      (do (.sendHandshake channel
                          {"Upgrade"              "baloney"
                           "Connection"           "Upgrade"
                           "Sec-WebSocket-Accept" (h/accept key)})
          (h/send! channel "bye" true))

      :else
      (h/with-channel request channel
        (case path
        
          "/echo"
          (do
            (h/on-receive channel
                          (fn [message]
                            (h/send! channel message))))

          "/error"
          (timer/schedule-task 10 (.serverClose channel 1011))

          )))))

(defn start-server []
  (h/run-server handler {:port 8124}))


(defn- once-fixture [f]
  (binding [*client* (client/create-client :connection-timeout 1000
                                           :request-timeout    1000)
            *server* (start-server)]
    (try (f)
         (finally
          (do
            (.close ^AsyncHttpClient *client*)
            (*server*))))))

(defn- q []
  (java.util.concurrent.LinkedBlockingQueue.))

(defn- q-get
  ([q] (q-get q 1000))
  ([q ms] (.poll q ms TimeUnit/MILLISECONDS)))

(defn- ws-client
  "Helper function for tests."
  [path]
  (let [m  {:open  (q)
            :close (q)
            :error (q)
            :text  (q)
            :byte  (q)}
        ws (client/websocket *client* (str "ws://localhost:8124" path)
                             :open  (fn [soc]
                                      (.add (:open m) soc))
                             :close (fn [soc code reason]
                                      (.add (:close m) [soc code reason]))
                             :error (fn [soc ex]
                                      (.add (:error m) [soc ex]))
                             :text  (fn [soc t]
                                      (.add (:text m) t))
                             :byte  (fn [soc b]
                                      (.add (:byte m) b)))]
    (assoc m :soc ws)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; The tests

(use-fixtures :once once-fixture)

(deftest test-lifecycle
  (testing "open callback gets called"
    (let [ws  (ws-client "/echo")
          soc (q-get (:open ws))]
      (is soc)
      (is (client/open? soc))
      (testing "close callback gets called"
        (client/close (:soc ws))
        (let [[soc code reason] (q-get (:close ws))]
          (is (= 1000 code))
          (is (.startsWith reason "Normal")) ; This is probably overspecifying...
          ))))
  
  (testing "error callback gets called"
    ;; ...except it doesn't :(
    ;; TODO - figure out what conditions fire the error callback
    #_(let [ws  (ws-client "/error")
            err (q-get (:error ws))]
        (is err)
        (let [[soc ex] err]
          (println "Error:" err)
          (is (not (client/open? soc)))
          (is (instance? Exception ex))
          (is (= "foo" (.getMessage ex))))))

  )

(deftest test-echo
  (testing "simple echo test"
    (let [ws (ws-client "/echo")]
      (client/send (:soc ws) :text "hello")
      (is (= "hello" (q-get (:text ws))))
      )))

(deftest test-text-and-byte-callbacks
  (testing "Either the text or the byte callback should be called; not both"
    (let [ws (ws-client "/echo")]
      (testing "text handler"
        (client/send (:soc ws) :text "hey")
        (let [t (q-get (:text ws))
              b (q-get (:byte ws))]
          (is (= "hey" t))
          (is (nil? b))))
      (testing "bytes handler"
        (let [bytes-sent (.getBytes "banana" "utf-8")]
          (client/send (:soc ws) :byte bytes-sent)
          (let [t (q-get (:text ws))
                b (q-get (:byte ws))]
            (is (nil? t))
            (is (= (type bytes-sent) (type b)))
            (is (= (seq bytes-sent) (seq b)))))))))

(deftest test-invalid-args
  (testing "send should throw on bad input"
    (let [ws  (ws-client "/echo")
          soc (:soc ws)]
      (is (thrown-with-msg? IllegalArgumentException #":text or :byte"
                            (client/send soc :bytes "foo")))
      (are [t d] (thrown? ClassCastException (client/send soc t d))
           :byte "foo"
           :text (.getBytes "foo")
           :text {:foo "bar"})
      (is (thrown? Exception (client/send {} :text "yo")))
      (is (thrown? Exception (client/send nil :text "hi")))
      )))
