(ns social-wallet-api.test.precision
  (:require [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [social-wallet-api.handler :as h]
            [auxiliary.config :refer [config-read]]
            [taoensso.timbre :as log]
            [cheshire.core :as cheshire]
            [clojure.test.check.generators :as gen]
            [midje.experimental :refer [for-all]]))

(def test-app-name "social-wallet-api-test")

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(def Satoshi 0.00000001)

(against-background [(before :contents (h/init
                                        (config-read social-wallet-api.test.handler/test-app-name)
                                        social-wallet-api.test.handler/test-app-name))
                     (after :contents (h/destroy))]
                    (facts "Check different doubles"
                           (for-all
                            [rand-double (gen/double* {:min Satoshi
                                                       :max 9999999999999999.99999999
                                                       :Nan? false
                                                       :infinite? false})]
                            {:num-tests 100
                             #_:max-size #_20
                             #_:seed #_1510160943861}
                            (fact "A really large number with 16,8 digits"
                                  (let [amount (str rand-double)  
                                       response (h/app
                                                 (->
                                                  (mock/request :post "/wallet/v1/transactions/new")
                                                  (mock/content-type "application/json")
                                                  (mock/body  (cheshire/generate-string {:blockchain :mongo
                                                                                         :from-id "test-1"
                                                                                         :to-id "test-2"
                                                                                         :amount (log/spy amount)
                                                                                         :tags ["blabla"]}))))
                                       body (parse-body (:body response))]
                                   (:status response) => 200
                                   (:amount body) => rand-double)))
                           

                           #_(fact "Check other inputs"
                                 (for-all
                                  [other (gen/one-of [gen/string gen/boolean gen/uuid gen/byte ])]
                                  #_{:num-tests 100
                                     :max-size 20
                                     #_:seed #_1510160943861}
                                  (fact "A really large number with 16,8 digits"
                                        (let [amount other 
                                              response (h/app
                                                        (->
                                                         (mock/request :post "/wallet/v1/transactions/new")
                                                         (mock/content-type "application/json")
                                                         (mock/body  (cheshire/generate-string {:blockchain :mongo
                                                                                                :from-id "test-1"
                                                                                                :to-id "test-2"
                                                                                                :amount amount
                                                                                                :tags ["blabla"]}))))
                                              body (parse-body (:body response))]
                                          (:status response) => 200
                                          (:amount body) => amount))))
                           (fact "Check that the amount returned after the creation of a transanction in mongo is the same as the input one"
)))
