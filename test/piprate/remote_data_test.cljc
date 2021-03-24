(ns piprate.remote-data-test
  (:require
   [clojure.test :refer [deftest testing is]]
   [cognitect.anomalies :as anom]
   [piprate.remote-data :as rd]))

(def sample-anomaly {::anom/category ::anom/busy
                     ::anom/message "my sample anomaly"})

(deftest creation
  (testing "not-requested"
    (is (= {::rd/status ::rd/not-requested}
           (rd/not-requested)))

    (is (= {::rd/status ::rd/not-requested
            :request-id "req-id"
            :args       "foo-args"}
           (rd/not-requested {:request-id "req-id" :args "foo-args"}))))

  (testing "loading"
    (is (= {::rd/status ::rd/loading}
           (rd/loading)))

    (is (= {::rd/status ::rd/loading
            :request-id "req-id"
            :args       "foo-args"}
           (rd/loading {:request-id "req-id" :args "foo-args"}))))

  (testing "success"
    (is (= {::rd/status ::rd/success
            ::rd/data   {:a :b}}
           (rd/success {:a :b})))

    (is (= {::rd/status ::rd/success
            ::rd/data   {:a :b}
            :request-id "req-id"
            :args       "foo-args"}
           (rd/success {:request-id "req-id" :args "foo-args"} {:a :b}))))

  (testing "error"
    (is (= {::rd/status ::rd/error
            ::rd/error  {::anom/category ::anom/busy}}
           (rd/error {::anom/category ::anom/busy})))

    (is (= {::rd/status ::rd/error
            ::rd/error  {::anom/category ::anom/busy}
            :request-id "req-id"
            :args       "foo-args"}
           (rd/error {:request-id "req-id" :args "foo-args"} {::anom/category ::anom/busy})))))


(deftest transition-to-success
  (let [current-state (rd/loading {:foo :bar})]
    (is (= {::rd/status ::rd/success
            ::rd/data   {:a :b}
            :foo        :bar}
           (rd/success current-state {:a :b})))))


(deftest transition-to-error
  (let [current-state (rd/loading {:foo :bar})]
    (is (= {::rd/status ::rd/error
            ::rd/error   {::anom/category ::anom/busy}
            :foo        :bar}
           (rd/error current-state {::anom/category ::anom/busy})))))


(deftest transation-to-loading
  (let [current-state (rd/error sample-anomaly)]
    (is (= {::rd/status ::rd/loading
            :foo        :bar}
           (rd/loading current-state {:foo :bar})))))


(deftest response
  (testing "gives success status when response is not an anomaly"
    (let [resp {:foo :bar}]
      (is (= (rd/success resp) (rd/response resp)))

      (let [current-state (rd/loading  {:xtra "keep me"})]
        (is (= (rd/success current-state resp) (rd/response current-state resp))))))

  (testing "gives error status when response is anomaly"
    (let [resp sample-anomaly]
      (is (= (rd/error resp) (rd/response resp)))

      (let [current-state (rd/loading  {:xtra "keep me"})]
        (is (= (rd/error current-state resp) (rd/response current-state resp)))))))


(deftest predicates
  (testing "loading?"
    (is (rd/loading? (rd/loading)))
    (is (not (rd/loading? (rd/error "error"))))
    (is (not (rd/loading? (rd/success "data"))))
    (is (not (rd/loading? (rd/not-requested)))))

  (testing "error?"
    (is (rd/error? (rd/error "error")))
    (is (not (rd/error? (rd/loading))))
    (is (not (rd/error? (rd/success "data"))))
    (is (not (rd/error? (rd/not-requested))))))
