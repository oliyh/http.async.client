(ns http.async.client.test-websockets
  "Tests for websocket clients."
  (:require [clojure.test :refer :all]
            [http.async.client :as client]
            [org.httpkit.server :as h])
  (:import [com.ning.http.client AsyncHttpClient]
           [java.util.concurrent TimeUnit LinkedBlockingQueue]))


(def ^:dynamic *client*)
(def ^:dynamic *server*)

(defn handler [request]
  (h/with-channel request channel

    (h/on-receive channel (fn [message]
                            (h/send! channel message)))

    ))

(defn start-server []
  (h/run-server handler {:port 8124}))


(defn- once-fixture [f]
  "Configures Logger before test here are executed, and closes AHC after tests are done."
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
  (let [m  {:open  (promise)
            :close (promise)
            :error (promise)
            :text  (q)
            :byte  (q)}
        ws (client/websocket *client* (str "ws://localhost:8124" path)
                             :open  (fn [soc]
                                      (deliver (:open m) soc))
                             :close (fn [ws code reason]
                                      (deliver (:close m) [ws code reason]))
                             :error (fn [ws ex]
                                      (deliver (:error m) [ws ex]))
                             :text  (fn [soc t]
                                      (.offer (:text m) t))
                             :byte  (fn [soc b]
                                      (.offer (:byte m) b)))]
    (assoc m :ws ws)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; The tests

(use-fixtures :once once-fixture)

(deftest test-echo
  (testing "simple echo test"
    (let [ws (ws-client "/echo")]
      (client/send (:ws ws) :text "hello")
      (is (= "hello" (q-get (:text ws))))
      )))
