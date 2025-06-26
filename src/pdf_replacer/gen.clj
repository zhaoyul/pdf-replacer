(ns pdf-replacer.gen
  (:require [clj-pdf.core :as pdf]))

(defn sample-pdf
  ([out]
   (sample-pdf out "This is a sample PDF. 包含中文示例。"))
  ([out text]
   (pdf/pdf [{}
             [:paragraph text]]
            out)))

(defn -main [& [out]]
  (let [outfile (or out "sample.pdf")]
    (sample-pdf outfile)))
