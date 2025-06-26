(ns pdf-replacer.core
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import [org.apache.pdfbox.pdmodel PDDocument PDPage PDPageContentStream]
           [org.apache.pdfbox.pdmodel.font PDType0Font]
           [org.apache.pdfbox.text PDFTextStripper]))

(defn load-config []
  (let [f (io/file "config.edn")]
    (when (.exists f)
      (-> f slurp edn/read-string))))

(defn default-font-path []
  (or (System/getenv "PDF_REPLACER_FONT_PATH")
      (:font-path (load-config))))

(defn replace-text
  "Replace occurrences of strings in a PDF.
   replacements should be a map of string to string.
   font-path is optional path to a TTF/OTF font that supports the text."
  [in-file out-file replacements & {:keys [font-path font-size]
                                    :or {font-size 12}}]
  (with-open [doc (PDDocument/load (io/file in-file))]
    (let [stripper (doto (PDFTextStripper.)
                     (.setSortByPosition true))
          text (.getText stripper doc)
          replaced (reduce (fn [t [k v]] (str/replace t k v)) text replacements)]
      (with-open [out-doc (PDDocument.)]
        (let [page (PDPage.)]
          (.addPage out-doc page)
          (let [fp (or font-path (default-font-path))]
            (when-not fp
              (throw (ex-info "Font path not configured" {})))
            (with-open [content (PDPageContentStream. out-doc page)]
              (let [font (PDType0Font/load out-doc (io/file fp))]
                (.beginText content)
                (.setFont content font font-size)
                (.setLeading content (* 1.2 font-size))
                (.newLineAtOffset content 50 750)
                (doseq [line (str/split-lines replaced)]
                  (.showText content line)
                  (.newLine content))
                (.endText content)))
            (.save out-doc out-file))))))

(defn -main [& args]
  (let [[in out & kvs] args
        pairs (partition 2 kvs)
        repl-map (into {} pairs)]
    (replace-text in out repl-map)))
