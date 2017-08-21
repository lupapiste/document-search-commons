(ns search-commons.zip-files
  (:require [clojure.string :as s]
            [clojure.java.io :as io])
  (:import [java.util.zip ZipOutputStream ZipEntry]
           [java.io File InputStream]
           [java.text Normalizer Normalizer$Form]))

;;; loaned from sade.files

(defn ^java.io.File temp-file
  "Creates a file that will be deleted when the JVM exits.
   Note: consider using with-temp-file instead!"
  ([^String prefix ^String suffix]
   (doto (java.io.File/createTempFile prefix suffix) (.deleteOnExit)))
  ([^String prefix ^String suffix ^java.io.File directory]
   (doto (java.io.File/createTempFile prefix suffix directory) (.deleteOnExit))))

(defn ^java.io.InputStream temp-file-input-stream
  "File given as parameter will be deleted after the returned stream is closed."
  [^java.io.File file]
  {:pre [(instance? java.io.File file)]}
  (let [i (io/input-stream file)]
    (proxy [java.io.FilterInputStream] [i]
      (close []
        (proxy-super close)
        (when (= (io/delete-file file :could-not) :could-not)
          (println "Could not delete temporary file: %s" (.getAbsolutePath file)))))))

;;; loaned from sade.strings

(defn last-n [n ^String s]
  (when s
    (apply str (take-last n s))))

(defn de-accent
  "Replaces accent characters with base letters"
  [^String s]
  (when s (let [normalized (Normalizer/normalize s Normalizer$Form/NFD)]
            (s/replace normalized #"\p{InCombiningDiacriticalMarks}+" ""))))

(def windows-filename-max-length 255)

(defn encode-filename
  "Replaces all non-ascii chars and other that the allowed punctuation with dash.
   UTF-8 support would have to be browser specific, see http://greenbytes.de/tech/tc2231/"
  [unencoded-filename]
  (when-let [de-accented (de-accent unencoded-filename)]
    (s/replace
      (last-n windows-filename-max-length de-accented)
      #"[^a-zA-Z0-9\.\-_ ]" "-")))

;;;

(defn- append-stream [zip filename in]
  (when in
    (.putNextEntry zip (ZipEntry. (encode-filename filename)))
    (io/copy in zip)
    (.closeEntry zip)))

(defn ^java.io.File zip-files
  "Takes list of documents containting file id, filename and file stream, returns files zipped"
  [docs-list]
  (let [temp-file (temp-file "lupapiste.dokmenttihaku." ".zip.tmp")]
    (with-open [zip (ZipOutputStream. (io/output-stream temp-file))]
      (doseq [doc docs-list]
        (append-stream zip (str (:file-id doc) "_" (:filename doc)) (:stream doc)))
      (.finish zip))
    temp-file))
