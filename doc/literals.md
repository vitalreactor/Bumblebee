# Bumblebee Reader Literals

## bumblebee/jsonpath

    #bumblebee/jsonpath <string>

Creates a JSONPath isolating pattern. The data to be queried will be coerced to JSON, and then the JSONPath expression will be evaluated against the JSON.

    #bumblebee/jsonpath [<pattern> :as <%symbol>]

Creates a JSONPath isolating pattern as with a string, but names the isolated variable for use in e.g. grouping and aggregating.

## bumblebee/tlambda

    #bumblebee/tlambda <sexpr>

Creates an anonymous, 2-arity lambda for processing Bumblebee tuples, useful for grouping. The s-expression may contain %<number> or %<symbol> symbols, in which case:

- %<number> symbols will evaluate to the nth item of the tuple, e.g. %0 will be the first element of the tuple.
- %<symbol> symbols will evaluate to named elements of the tuple.

## bumblebee/aggregator

    #bumblebee/aggregator [<fn> <tuple element>]

Creates an aggregator which will reduce a seq of tuples to a single value.

A bare set of tuples will aggregate to a single value.

A map of group values to tuples will aggregate to a map of group values to single values.

Tuple element specifies which element of the tuple should be aggregated as a number or symbol; e.g. %1, or %value

Supported aggregation functions include:

- :avg - Return the average of the specified element across all tuples.

## bumblebee/timeseries

    #bumblebee/timeseries {:element %<number or symbol>
                           :resolution <one of #{:millisecond :second :minute :hour :day :week :month :year}>
	                   [:tz-offset <-12 - 12>]
	                   [:start <one of #{:monday :sunday}>]}
