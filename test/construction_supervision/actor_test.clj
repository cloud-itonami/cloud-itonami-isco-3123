(ns construction-supervision.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [construction-supervision.actor :as actor]
            [construction-supervision.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-site! st {:site-id "site-1" :name "Riverside Development"})
    st))

(deftest commits-a-clean-low-risk-request
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:site-id "site-1" :op :coordinate :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "site-1"))))))

(deftest holds-on-unregistered-site-without-committing
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:site-id "no-such-site" :op :coordinate :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (empty? (store/records-of st "no-such-site")))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-then-commits-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        ;; near-active-equipment operation always escalates (governor invariant)
        request {:site-id "site-1" :op :operate-near-active-equipment :stake :high}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "site-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (some? (get-in resumed [:state :record])))
      (is (= 1 (count (store/records-of st "site-1")))))))
