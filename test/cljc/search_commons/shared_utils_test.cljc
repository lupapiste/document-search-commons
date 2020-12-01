(ns search-commons.shared-utils-test
  (:require [search-commons.shared-utils :as sut]
            #?(:clj [clojure.test :refer [deftest testing is are]]
               :cljs [cljs.test :refer [deftest testing is are] :include-macros true])))

(deftest property-id?-test
  (testing "property-id? returns"
    (testing "false for invalid property-id"
      (is (false? (sut/property-id? "foobar")))
      (is (false? (sut/property-id? nil)))
      (is (false? (sut/property-id? "")))
      (is (false? (sut/property-id? "92-403-2-"))))
    (testing "true for valid property-id"
      (testing "in db-format"
        (is (true? (sut/property-id? "09241600110123"))))
      (testing "in human format"
        (is (true? (sut/property-id? "92-403-2-279")))
        (is (true? (sut/property-id? "092-403-0002-0279")))))))

(deftest ->db-property-id-test
  (testing "->db-property-id returns"
    (testing "nil for invalid property-id"
      (is (nil? (sut/->db-property-id "foobar")))
      (is (nil? (sut/->db-property-id "92-403-2-")))
      (is (nil? (sut/->db-property-id nil)))))
  (testing "property-id in db-format"
    (testing "when already in db-format"
      (is (= "09241600110123" (sut/->db-property-id "09241600110123"))))
    (testing "in human format"
      (is (= "09240300020279" (sut/->db-property-id "92-403-2-279"))))))
