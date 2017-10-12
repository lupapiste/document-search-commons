(ns search-commons.utils.state
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [search-commons.routing :as routing]
            [ajax.core :refer [GET POST]]
            [reagent.core :as reagent]
            [alandipert.storage-atom :as storage]
            [clojure.string :as string]
            [lupapiste-commons.attachment-types :as attachment-types]
            [clojure.set :as set]))

(defonce translations (reagent/atom {:current-lang :unset
                                     :translations {}}))

(defonce config (reagent/atom {}))

(def empty-search-results
  {:results []
   :took nil
   :onkalo-took nil
   :onkalo-results []
   :loading? false
   :has-more? false
   :onkalo-has-more? false
   :seen-results #{}})

(defonce search-results (reagent/atom empty-search-results))

(defonce selected-result-id (reagent/atom nil))

(def empty-search-query
  {:text nil
   :fields #{}
   :timespan {:from nil
              :to nil}
   :operation nil
   :usage nil
   :page 0
   :type '()
   :coordinates []
   :organization ""
   :tokenize? false
   :targets #{:lupapiste :onkalo}})

(defonce search-query (reagent/atom empty-search-query))

(defonce last-search (reagent/atom empty-search-query))

(defonce saved-search (storage/local-storage (atom {}) :saved-search))

(defonce saved-seen-results (storage/local-storage (atom #{}) :saved-seen-results))

(defonce start-date (reagent/atom nil))

(defonce closed-start-date (reagent/atom nil))

(add-watch start-date :set-date-from #(swap! search-query assoc-in [:timespan :from] (when %4 (.getTime %4))))

(add-watch closed-start-date :set-closed-date-from
           #(swap! search-query assoc-in [:timespan :closed-from] (when %4 (.getTime %4))))

(defonce end-date (reagent/atom nil))

(defonce closed-end-date (reagent/atom nil))

(add-watch end-date :set-date-to #(swap! search-query assoc-in [:timespan :to] (when %4 (.getTime %4))))

(add-watch closed-end-date :set-closed-date-to
           #(swap! search-query assoc-in [:timespan :closed-to] (when %4 (.getTime %4))))

(defonce operations (reagent/atom []))

(defonce show-search-map (reagent/atom false))

(defonce map-selected-result-ids (reagent/atom #{}))

(defonce multi-select-mode (reagent/atom false))

(defonce multi-selected-results (reagent/atom #{}))

(def unique-results
  (reaction
    (let [{:keys [results onkalo-results]} @search-results
          onkalo-ids (into #{} (map :id onkalo-results))
          unique-mongo-results (remove #(contains? onkalo-ids (:id %)) results)]
      (concat onkalo-results unique-mongo-results))))

(def filtered-results
  (reaction
    (if (empty? @map-selected-result-ids)
      @unique-results
      (filter #(contains? @map-selected-result-ids (:id %)) @unique-results))))

(def result-groups
  (reaction
    (reduce
      (fn [acc {:keys [grouping-key] :as val}]
        (update acc grouping-key concat [val]))
      {}
      @filtered-results)))

(defn flatten-attachments [groups]
  (reduce
    (fn [acc [group values]]
      (reduce
        (fn [acc value]
          (conj acc [group value]))
        acc
        values))
    #{}
    (partition 2 groups)))

(def selected-permit-types
  (reaction
    (let [selected-org (:organization @search-query)
          all-orgs (get-in @config [:user :organizations])]
      (->> (if (string/blank? selected-org)
             all-orgs
             (filter (fn [[id _]] (= id selected-org)) all-orgs))
           (map (fn [[_ data]] (:permit-types data)))
           (apply set/union)))))

(def document-types #{[:hakemus] [:ilmoitus] [:neuvontapyyntö] [:case-file]})

(def available-attachment-types
  (reaction
    (let [types @selected-permit-types
          all? (empty? types)]
      (cond-> document-types
              (or all? (types "R")) (set/union (flatten-attachments attachment-types/Rakennusluvat-v2))
              (or all? (types "YA")) (set/union (flatten-attachments attachment-types/YleistenAlueidenLuvat-v2))))))

(defn find-result [id results]
  (first (filter #(= id (:id %)) results)))

(def selected-result
  (reaction
    (let [id @selected-result-id
          {:keys [results onkalo-results]} @search-results]
      (when id (or (find-result id onkalo-results) (find-result id results))))))

(def total-result-count
  (reaction
    (count @unique-results)))

(add-watch show-search-map :set-coordinates #(swap! search-query (fn [v] (if %4
                                                                           v
                                                                           (assoc v :coordinates [])))))

(defn language-header []
  {"Accept-Language" (-> @translations :current-lang name)})

(defn fetch-operations []
  (GET (routing/path "/operations")
       {:handler #(reset! operations %)
        :headers (language-header)}))

(defn fetch-translations [lang]
  (GET (routing/path (str "/i18n/" (name lang)))
       :headers (language-header)
       :handler #(swap! translations assoc :translations %1 :current-lang lang)))

(defn set-lang! [lang]
  (fetch-translations lang))

(defn search-error-handler [{:keys [status status-text]}]
  (swap! search-results assoc :loading? false))

(defn search-lupapiste []
  (POST (routing/path "/search")
        {:params @search-query
         :headers (language-header)
         :handler (fn [{:keys [has-more? took results]}]
                    (if (and (empty? results) has-more?)
                      ;; Filtering might cause the next page to be empty, automatically fetch more
                      (search-lupapiste)
                      (swap! search-results merge {:loading? false
                                                   :has-more? has-more?
                                                   :took took
                                                   :results (concat (:results @search-results) results)})))
         :error-handler search-error-handler}))

(defn search-onkalo []
  (POST (routing/path "/search-onkalo")
        {:params @search-query
         :headers (language-header)
         :handler (fn [{:keys [has-more? took results]}]
                    (swap! search-results merge {:loading? false
                                                 :onkalo-has-more? has-more?
                                                 :onkalo-took took
                                                 :onkalo-results (concat (:onkalo-results @search-results) results)}))
         :error-handler search-error-handler}))

(defn new-search []
  (swap! search-query assoc :page 0)
  (reset! selected-result-id nil)
  (reset! last-search @search-query)
  (reset! saved-search @search-query)
  (reset! multi-select-mode false)
  (reset! multi-selected-results #{})
  (swap! search-results merge {:loading? true
                               :has-more? false
                               :results []
                               :took nil
                               :onkalo-took nil
                               :onkalo-results []})
  (when (and (contains? (:targets @search-query) :lupapiste)
             (get-in @config [:config :lupapiste-enabled?]))
    (search-lupapiste))
  (when (and (contains? (:targets @search-query) :onkalo)
             (get-in @config [:config :onkalo-enabled?]))
    (search-onkalo)))

(defn fetch-more []
  (swap! search-query assoc :page (inc (:page @search-query)))
  (swap! search-results assoc :loading? true)
  (when (and (contains? (:targets @search-query) :lupapiste)
             (:has-more? @search-results))
    (search-lupapiste))
  (when (and (contains? (:targets @search-query) :onkalo)
             (:onkalo-has-more? @search-results))
    (search-onkalo)))

(defn update-search-field [key value]
  (swap! search-query assoc key value))

(defn mark-result-seen [id]
  (swap! search-results assoc :seen-results (conj (:seen-results @search-results) id))
  (swap! saved-seen-results conj id))

(def multi-select-count
  (reaction
    (count @multi-selected-results)))

(defn multi-selected-results-contain? [doc-id]
  (some #(= doc-id (:doc-id %)) @multi-selected-results))

(defn multi-select-result [doc-id file-id filename org-id archived?]
  (let [source (if archived? "onkalo" "lupapiste")
        doc-entry {:source source :org-id org-id :doc-id doc-id :file-id file-id :filename filename}]
    (if (multi-selected-results-contain? doc-id)
      (swap! multi-selected-results disj doc-entry)
      (when (< @multi-select-count 200) (swap! multi-selected-results conj doc-entry)))))

(defn multi-select-result-group [all-selected? result-group]
  (let [select (fn [{:keys [id fileId filename tiedostonimi organization source-system]}]
                 (multi-select-result id
                                      (or fileId id)
                                      (or tiedostonimi filename)
                                      organization
                                      (= :onkalo source-system)))]
    (if all-selected?
      (doall (for [result result-group] (select result)))
      (when (<= (+ (count result-group) @multi-select-count) 200)
        (doall (for [result result-group]
                 (when-not (multi-selected-results-contain? (:id result))
                   (select result))))))))

(defn multi-select-all-results []
  (let [results-set (set (for [result @filtered-results]
                           (let [source (if (= :onkalo (:source-system result)) "onkalo" "lupapiste")
                                 org-id (:organization result)
                                 doc-id (:id result)
                                 file-id (or (:fileId result) doc-id)
                                 filename (or (:filename result) (:tiedostonimi result))]
                             {:source source :org-id org-id :doc-id doc-id :file-id file-id :filename filename})))]
    (reset! multi-selected-results results-set)))

(defn toggle-multi-select-mode []
  (reset! selected-result-id nil)
  (reset! multi-select-mode (not @multi-select-mode)))

(defn reset-state []
  (storage/remove-local-storage! :saved-search)
  (storage/remove-local-storage! :saved-seen-results)
  (reset! show-search-map false)
  (reset! search-results empty-search-results)
  (reset! last-search empty-search-query)
  (reset! search-query (assoc empty-search-query :tokenize? (:tokenize? @search-query)))
  (reset! start-date nil)
  (reset! end-date nil)
  (reset! closed-start-date nil)
  (reset! closed-end-date nil)
  (reset! multi-select-mode false)
  (reset! map-selected-result-ids #{})
  (reset! multi-selected-results #{}))

(defn reset-date-atoms []
  (let [query @search-query]
    (doseq [[ra key] [[start-date :from] [end-date :to]]]
      (when-let [ts (get-in query [:timespan key])]
        (reset! ra (js/Date. ts))))))

(defn load-saved-search []
  (when-let [saved @saved-search]
    (if-not (and (string/blank? (:text saved)) (empty? (:coordinates saved)))
      (do (reset! search-query saved)
          (reset-date-atoms)
          (when (seq (:coordinates saved))
            (reset! show-search-map true))
          (new-search))
      (reset-state)))
  (when-let [seen-results @saved-seen-results]
    (swap! search-results assoc :seen-results seen-results)))

(defn fetch-user-and-config []
  (GET (routing/path "/user-and-config")
       {:handler (fn [data]
                   (reset! config data)
                   (load-saved-search))
        :headers (language-header)}))

(defn update-onkalo-result-data [id new-s2-metadata]
  (swap! search-results (fn [{:keys [onkalo-results] :as res}]
                          (->> onkalo-results
                               (map (fn [result]
                                      (if (= id (:id result))
                                        (assoc result :metadata new-s2-metadata
                                                      :contents (:contents new-s2-metadata)
                                                      :type (:type new-s2-metadata))
                                        result)))
                               (assoc res :onkalo-results)))))

(def results-for-map
  (reaction
    (->> (map #(select-keys % [:id :location]) @unique-results)
         (remove nil?))))

(defn selected-language [query]
  (#{:fi :sv :en} (keyword (first (keys query)))))

