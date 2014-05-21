(ns bumblebee.impl-test
  (:use [bumblebee.impl :as impl]
        midje.sweet)
  (:require [bumblebee.test-json :as tj]))

(facts "about isolation"
       (fact "it extracts data with jsonpaths"
             (isolate tj/test-payload [#bumblebee/jsonpath "$.date" #bumblebee/jsonpath "$.value"]) =>
             [[1398358339110 179] [1398368339110 182] [1398378339110 147] [1398388339110 168] [1398398339110 174]])
       (fact "it attaches metadata to the extracted data"
             (isolate tj/test-payload [#bumblebee/jsonpath "$.date" #bumblebee/jsonpath "$.value"]) =>
             (comp ::impl/isolated-tuples meta))
       (fact "it names tuple elements"
             (isolate tj/test-payload [#bumblebee/jsonpath ["$.date" :as date] #bumblebee/jsonpath ["$.value" :as value]])
             => #(-> % meta ::impl/names (= '[date value]))))

(facts "about grouping"
       (fact "it groups data by expr, returning a map of expr -> tuple"
             (-> tj/test-payload
                 (isolate [#bumblebee/jsonpath "$.date" #bumblebee/jsonpath "$.value"])
                 (group #bumblebee/tlambda (long (/ %1 86400000)))) =>
                 {16184 [[1398358339110 179] [1398368339110 182] [1398378339110 147]]
                  16185 [[1398388339110 168] [1398398339110 174]]})
       (fact "the grouping fn can accept named elements of the tuple"
             (-> tj/test-payload
                 (isolate [#bumblebee/jsonpath ["$.date" :as date] #bumblebee/jsonpath ["$.value" :as value]])
                 (group #bumblebee/tlambda (long (/ %date 86400000)))) =>
                 {16184 [[1398358339110 179] [1398368339110 182] [1398378339110 147]]
                  16185 [[1398388339110 168] [1398398339110 174]]})
       (fact "it generates metadata on the returned map"
             (-> tj/test-payload
                 (isolate [#bumblebee/jsonpath "$.date" #bumblebee/jsonpath "$.value"])
                 (group #bumblebee/tlambda (long (/ %1 86400000)))) =>
                 (comp ::impl/grouped-tuples meta))
       (fact "it preserves the metadata of each underlying collection of tuples"
             (-> tj/test-payload
                 (isolate [#bumblebee/jsonpath "$.date" #bumblebee/jsonpath "$.value"])
                 (group #bumblebee/tlambda (long (/ %1 86400000)))) =>
                 (partial every? #(-> % (nth 1) meta ::impl/isolated-tuples))))

(facts "about time grouping"
       (fact "it groups data by day"
             (-> tj/test-payload
                 (isolate [#bumblebee/jsonpath ["$.date" :as date] #bumblebee/jsonpath ["$.value" :as value]])
                 (group #bumblebee/timeseries {:element %date
                                               :resolution :day}))
             =>
             {#inst "2014-04-24T00:00:00.000-00:00" [[1398358339110 179] [1398368339110 182] [1398378339110 147]]
              #inst "2014-04-25T00:00:00.000-00:00" [[1398388339110 168] [1398398339110 174]]})
       (fact "it groups data by millisecond"
             (-> tj/test-payload
                 (isolate [#bumblebee/jsonpath ["$.date" :as date] #bumblebee/jsonpath ["$.value" :as value]])
                 (group #bumblebee/timeseries {:element %date
                                               :resolution :millisecond}))
             =>
             {#inst "2014-04-24T16:52:19.110-00:00" [[1398358339110 179]]
              #inst "2014-04-24T19:38:59.110-00:00" [[1398368339110 182]]
              #inst "2014-04-24T22:25:39.110-00:00" [[1398378339110 147]]
              #inst "2014-04-25T01:12:19.110-00:00" [[1398388339110 168]]
              #inst "2014-04-25T03:58:59.110-00:00" [[1398398339110 174]]})
       (fact "it groups data by month"
             (-> tj/test-payload
                 (isolate [#bumblebee/jsonpath ["$.date" :as date] #bumblebee/jsonpath ["$.value" :as value]])
                 (group #bumblebee/timeseries {:element %date
                                               :resolution :month}))
             =>
             {#inst "2014-04-01T00:00:00.000-00:00" [[1398358339110 179]
                                                     [1398368339110 182]
                                                     [1398378339110 147]
                                                     [1398388339110 168]
                                                     [1398398339110 174]]}))

(facts "about aggregating"
       (fact "it aggregates data, returning a single value"
             (-> tj/test-payload
                 (isolate [#bumblebee/jsonpath "$.date"
                           #bumblebee/jsonpath "$.value"])
                 (aggregate #bumblebee/aggregator [:avg %2]))
             => 170)
       (fact "it aggregates grouped data, returning a single value per group"
             (-> tj/test-payload
                 (isolate [#bumblebee/jsonpath "$.date"
                           #bumblebee/jsonpath "$.value"])
                 (group #bumblebee/tlambda (long (/ %1 86400000)))
                 (aggregate #bumblebee/aggregator [:avg %2]))
             => {16184 169
                 16185 171})
       (fact "it aggregates grouped data, returning a single value per group using names"
             (-> tj/test-payload
                 (isolate [#bumblebee/jsonpath ["$.date" :as date]
                           #bumblebee/jsonpath ["$.value" :as value]])
                 (group #bumblebee/tlambda (long (/ %date 86400000)))
                 (aggregate #bumblebee/aggregator [:avg %value]))
             => {16184 169
                 16185 171}))

