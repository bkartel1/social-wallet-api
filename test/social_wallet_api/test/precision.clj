(ns social-wallet-api.test.handler
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

(against-background [(before :contents (h/init
                                        (config-read social-wallet-api.test.handler/test-app-name)
                                        social-wallet-api.test.handler/test-app-name))
                     (after :contents (h/destroy))]
                    (facts "Check different amount inputs"
                           (for-all
                            [large-double (gen/double* {:max Double/MAX_VALUE :min Double/MIN_VALUE})]
                            {:num-tests 100
                             :max-size 20
                             :seed 1510160943861}
                            (fact "A really large number with 16,8 digits"
                                 (let [amount large-double  ;999999999999999.99999999 
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
                                   (:amount body) => amount)))
                           

                           (fact "Check string inputs"
)
                           (fact "Check that the amount returned after the creation of a transanction in mongo is the same as the input one"
)))
