(ns bumblebee.test-json)

(def test-payload
  [{:type "observation"
    :tracker "53591a32-30e7-4413-996c-066268348a98"
    :date 1398358339110
    :value 179
    :meta {:origin "sms"}}
   {:type "observation"
    :tracker "53591a32-30e7-4413-996c-066268348b01"
    :date 1398368339110
    :value 182
    :meta {:origin "sms"}}
   {:type "observation"
    :tracker "53591a32-30e7-4413-996c-066268348b97"
    :date 1398378339110
    :value 147
    :meta {:origin "sms"}}
   {:type "observation"
    :tracker "53591a32-30e7-4413-996c-066268348c42"
    :date 1398388339110
    :value 168
    :meta {:origin "sms"}}
   {:type "observation"
    :tracker "53591a32-30e7-4413-996c-066268348d03"
    :date 1398398339110
    :value 174
    :meta {:origin "sms"}}])
