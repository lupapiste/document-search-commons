(ns search-commons.i18n)

(defn find-string [key data]
  (let [str-key (if (keyword? key) (name key) key)]
    (if-let [result (some #(get data %) [str-key
                                         (str "attachmentType." str-key)
                                         (str "attachmentType." str-key "._group_label")
                                         (str "operations." str-key)
                                         (str "operations.tree." str-key)])]
      result
      str-key)))

(defn t [k data]
  (find-string k data))
