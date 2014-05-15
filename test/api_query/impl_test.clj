(ns api-query.impl-test
  (:use [api-query.impl :as impl]
        midje.sweet)
  (:require [api-query.test-json :as tj]))

(facts "about isolation"
       (fact "it extracts data with jsonpaths"
             (isolate tj/test-payload [#api-query/jsonpath "$.date" #api-query/jsonpath "$.value"]) =>
             [[1398358339110 179] [1398368339110 182] [1398378339110 147] [1398388339110 168] [1398398339110 174]])
       (fact "it attaches metadata to the extracted data"
             (isolate tj/test-payload [#api-query/jsonpath "$.date" #api-query/jsonpath "$.value"]) =>
             (comp ::impl/isolated-tuples meta))
       (fact "it names tuple elements"
             (isolate tj/test-payload [#api-query/jsonpath ["$.date" :as date] #api-query/jsonpath ["$.value" :as value]])
             => #(-> % meta ::impl/names (= '[date value]))))

(facts "about grouping"
       (fact "it groups data by expr, returning a map of expr -> tuple"
             (-> tj/test-payload
                 (isolate [#api-query/jsonpath "$.date" #api-query/jsonpath "$.value"])
                 (group #api-query/tlambda (long (/ %0 86400000)))) =>
                 {16184 [[1398358339110 179] [1398368339110 182] [1398378339110 147]]
                  16185 [[1398388339110 168] [1398398339110 174]]})
       (fact "the grouping fn can accept named elements of the tuple"
             (-> tj/test-payload
                 (isolate [#api-query/jsonpath ["$.date" :as date] #api-query/jsonpath ["$.value" :as value]])
                 (group #api-query/tlambda (long (/ %date 86400000)))) =>
                 {16184 [[1398358339110 179] [1398368339110 182] [1398378339110 147]]
                  16185 [[1398388339110 168] [1398398339110 174]]})
       (fact "it generates metadata on the returned map"
             (-> tj/test-payload
                 (isolate [#api-query/jsonpath "$.date" #api-query/jsonpath "$.value"])
                 (group #api-query/tlambda (long (/ %0 86400000)))) =>
                 (comp ::impl/grouped-tuples meta))
       (fact "it preserves the metadata of each underlying collection of tuples"
             (-> tj/test-payload
                 (isolate [#api-query/jsonpath "$.date" #api-query/jsonpath "$.value"])
                 (group #api-query/tlambda (long (/ %0 86400000)))) =>
                 (partial every? #(-> % (nth 1) meta ::impl/isolated-tuples))))

(facts "about aggregating"
       (fact "it aggregates data, returning a single value"
             (-> tj/test-payload
                 (isolate [#api-query/jsonpath "$.date"
                           #api-query/jsonpath "$.value"])
                 (aggregate #api-query/aggregator [:avg %1]))
             => 170)
       (fact "it aggregates grouped data, returning a single value per group"
             (-> tj/test-payload
                 (isolate [#api-query/jsonpath "$.date"
                           #api-query/jsonpath "$.value"])
                 (group #api-query/tlambda (long (/ %0 86400000)))
                 (aggregate #api-query/aggregator [:avg %1]))
             => {16184 169
                 16185 171})
       (fact "it aggregates grouped data, returning a single value per group using names"
             (-> tj/test-payload
                 (isolate [#api-query/jsonpath ["$.date" :as date]
                           #api-query/jsonpath ["$.value" :as value]])
                 (group #api-query/tlambda (long (/ %date 86400000)))
                 (aggregate #api-query/aggregator [:avg %value]))
             => {16184 169
                 16185 171}))

