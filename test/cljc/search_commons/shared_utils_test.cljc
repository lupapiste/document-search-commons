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

(deftest rakennustunnus?-test
  (testing "rakennustunnus?"
    (testing "returns falsey for invalid values"
      (is (not (sut/rakennustunnus? nil)))
      (is (not (sut/rakennustunnus? "")))
      (is (not (sut/rakennustunnus? "foo")))
      (is (not (sut/rakennustunnus? "1a")))
      (is (not (sut/rakennustunnus? "1A"))))
    (testing "returns truthy for valid values"
      (is (sut/rakennustunnus? "182736459F"))
      (is (sut/rakennustunnus? "100012345N"))
      (is (sut/rakennustunnus? "1234567892")))))

(deftest ->tokens-test
  (testing "tokens"
    (testing "returns empty collection for empty input"
      (is (= [] (sut/->tokens "")))
      (is (= [] (sut/->tokens nil))))
    (testing "returns non-empty when given a non-empty string"
      (is (= ["foo"] (sut/->tokens "foo")))
      (is (= ["a" "b" "c" "d" "e" "f"] (sut/->tokens "a, b , c,d ,e ,, f "))))))
