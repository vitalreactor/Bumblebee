(ns api-query.impl
  (:require [clojure.data.json :as json]
            [clojure.walk :as walk])
  (:import (clojure.lang IPersistentCollection IPersistentVector IPersistentMap)
           (com.jayway.jsonpath JsonPath Filter)))

(defprotocol Isolatable
  (isolate [blob patterns] "Generate a seq of n-tuples by isolating patterns from blob."))

(defprotocol Groupable
  (group [tuples expr] "Subdivide a seq of n-tuples by evaling expr against each."))

(defprotocol Aggregator
  (aggregate-tuples [aggregator tuples] "Aggregate n-tuples by fn."))

(defprotocol Aggregatable
  (aggregate [data aggregator] "Aggregate data with aggregator"))

(defprotocol Pattern
  (extract [pattern data] "Pull out one unique value from data using pattern.")
  (value-name [pattern] "Return the name this pattern specifies."))

(defprotocol JSONCoercible
  (coerce-json [data] "Coerce data to JSON"))

(defprotocol JSONPathPatternSource
  (create-json-path [source] "Create a JsonPathPattern from source."))

(def unfiltered
  (make-array Filter 0))

(extend-protocol JSONCoercible
  IPersistentCollection
  (coerce-json [data] (json/write-str data)))

(deftype JSONPathPattern [pattern_string value_name]
  Pattern
  (extract [this data]
    (let [json (coerce-json data)]
      (JsonPath/read json pattern_string unfiltered)))
  (value-name [this] value_name))

(extend-protocol JSONPathPatternSource
  String
  (create-json-path [string] (JSONPathPattern. string nil))
  IPersistentVector
  (create-json-path [vector]
    (let [[pattern as name] vector]
      (if (= as :as)
        (JSONPathPattern. pattern name)
        (throw (ex-info (str "Invalid pattern spec: " vector) {:pattern vector
                                                               :as-value as}))))))

(defn read-json-path
  [source]
  (create-json-path source))

(defn aggregate-values
  [agg values]
  (case agg
    :avg (long (/ (apply + values) (count values)))))

(deftype PositionalAggregator [agg pos]
  Aggregator
  (aggregate-tuples [this tuples]
    (let [vs (map #(nth % pos) tuples)]
      (aggregate-values agg vs))))

(defn get-named-element
  [tuples-meta n tuple]
  (let [name-mapping (->> tuples-meta
                          ::names
                          (map-indexed (comp vec reverse list))
                          (into {}))]
    (get tuple (get name-mapping n))))

(deftype NameAggregator [agg name]
  Aggregator
  (aggregate-tuples [this tuples]
    (let [vs (map (partial get-named-element (meta tuples) name) tuples)]
      (aggregate-values agg vs))))

(defn read-aggregator
  [[agg pos]]
  (when-not (.startsWith (name pos) "%")
    (throw (ex-info (str "Invalid positional selector " pos) {:pos pos
                                                              :name-pos (name pos)})))
  (let [stem (.substring (name pos) 1)]
    (if (re-matches #"\d{1,2}" stem)
      (PositionalAggregator. agg (Integer/parseInt stem))
      (NameAggregator. agg (symbol stem)))))

(defn tuple-lambda-argument-data
  [arguments]
  (into {}
        (for [argument arguments]
          (let [stem (.substring (name argument) 1)
                ordinal (when (re-matches #"\d{1,2}" stem)
                          (Integer/parseInt stem))]
            [stem {:type (if ordinal
                           :numeric
                           :named)
                   :ordinal ordinal
                   :name stem
                   :anchor argument}]))))

(defn read-tuple-lambda
  [l]
  (let [arguments (distinct (filter #(and (symbol? %) (.startsWith (name %) "%")) (flatten l)))
        arg-ana (tuple-lambda-argument-data arguments)
        names-sym (gensym "names")
        tuple-sym (gensym "tuple")
        arg-vec [names-sym tuple-sym]
        replacements (into {}
                           (for [m (vals arg-ana)]
                             (let [replacement (case (:type m)
                                                 :numeric `(nth ~tuple-sym ~(:ordinal m))
                                                 :named `(nth ~tuple-sym (get ~names-sym (quote ~(symbol (:name m))))))]
                               [(:anchor m) replacement])))
        body (walk/postwalk-replace replacements l)]
    (eval (list 'fn arg-vec body))))

(extend-protocol Isolatable
  IPersistentCollection
  (isolate [coll patterns]
    (let [value-names (map value-name patterns)]
      (with-meta (vec (for [el coll]
                        (vec (for [pattern patterns]
                               (extract pattern el)))))
        (merge {::isolated-tuples true
                ::patterns patterns}
               (when-not (every? nil? value-names)
                 {::names value-names}))))))

(extend-protocol Groupable
  IPersistentCollection
  (group [tuples expr]
    (let [tuple-meta (meta tuples)
          name-mapping (if-let [names (::names tuple-meta)]
                         (into {} (map-indexed (comp vec reverse list) names))
                         {})
          groupings (group-by (partial expr name-mapping) tuples)]
      (with-meta (into {} (for [[group tuples] groupings] [group (with-meta tuples tuple-meta)]))
        {::grouped-tuples true
         ::expr expr}))))

(extend-protocol Aggregatable
  IPersistentVector
  (aggregate [data agg]
    (aggregate-tuples agg data))
  IPersistentMap
  (aggregate [data agg]
    (into {}
          (for [[k v] data]
            [k (aggregate-tuples agg v)]))))

