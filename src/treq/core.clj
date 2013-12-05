(ns treq.core
  (:refer-clojure :exclude [resolve])
  (:require [torpo.uri :as uri])
  (:require [shaky.core :as shaky]))





(defn pick "Currently assumes that 'uri' is flat (does not contain a :source key)."
  [remoting-fn uri pick-map]
  (:pick (:result (remoting-fn (uri/prepare-for-transmission (uri/merge uri {:params {:pick pick-map}}))))))





(defn ring-request-to-resolution "Converts :params of raw-ring-req to a resolution where the params are the :source."
  [raw-ring-req]
  (let [parsed-params (-> (:params raw-ring-req) shaky/parse-request-params)
        with-source (select-keys parsed-params [:source])]
    (if (seq with-source)
      with-source
      {:source parsed-params}))) ;;If there is no :source among the params, assume the params are the source. Allows for flat params without angel brackets in urls.

(defn filtered-ring-request-to-resolution "Converts :params of raw-ring-req to a resolution where the params are the :source."
  [raw-ring-req & selected-params]
  {:source (apply hash-map
                  (apply concat (filter (fn [[k v]] (some #(= k %) selected-params))
                                        (seq (:source (ring-request-to-resolution raw-ring-req))))))})





(defn find-selector [resolution location]
  ;;get-in will throw exception if any part of the map hierarchy is not associative
  (if-let     [source-selector (try (get-in (:source resolution) location) (catch Exception e nil))] source-selector
    ;;the following line is useful if dependencies are resolved in a certain order and subsequent resolutions depends on earlier resolutions
    (when-let [result-selector (try (get-in (:result resolution) location) (catch Exception e nil))] result-selector)))

(defn resolve "Runs 'resolvers' on 'resolution'. The goal of each resolver is to produce a result based on :source of 'resolution'. In detail, for all 'location's in each resolver, picks a selector at the corresponding location in :source of 'resolution'. The 'resolution' and the picked selector is passed to each function in 'access-fns'. The value of the first access-fn that returns non-nil will be inserted into :result of 'resolution' using 'insert-fn'. If an error occurs, the error will be placed in :errors of 'resolution' instead of the corresponding result value."
  [resolution resolvers]
  (reduce (fn [resolution {:keys [locations insert-fn access-fns]}]
            (reduce (fn [res location]
                      (if-let [selector (find-selector resolution location)]
                        (if-let [access-fn-result (first (drop-while nil? (map #(% resolution selector) access-fns)))]
                          ;;access-fn-result should be associative to avoid exceptions when associng in deeply
                          (update-in res [:result] #((or insert-fn assoc-in) % location access-fn-result))
                          (update-in res [:errors] #((or insert-fn assoc-in) % location {:errors :access-function-did-not-return-any-data})))
                        res))
                    resolution
                    locations))
          resolution
          resolvers))
