(ns project.sanitize
  (:require [autoclave.core :refer (html-policy html-sanitize)])
  (:import java.net.URL))


(def tags-allowed
  [
   "a"
   "b"
   "blockquote"
   "br"
   "h1"
   "h2"
   "h3"
   "h4"
   "h5"
   "h6"
   "i"
   "iframe"
   "img"
   "li"
   "p"
   "pre"
   "small"
   "span"
   "strike"
   "strong"
   "sub"
   "sup"
   "table"
   "tbody"
   "td"
   "tfoot"
   "th"
   "thead"
   "tr"
   "u"
   "ul"
   ])


(def re-youtube
  #"(?i)\Qhttps://www.youtube.com/embed/\E.+")


(defn to-abs-url
  [page-url]
  (let [url (URL. page-url)]
    (fn [element-name attr-name value]
      (str (URL. url value)))))


(defn make-html-policy
  [page-url]

  (html-policy
   :allow-elements tags-allowed

   :allow-attributes
   ["href"
    :matching [(to-abs-url page-url)]
    :on-elements ["a"]]

   :allow-attributes
   ["src"
    :matching [(to-abs-url page-url)]
    :on-elements ["img"]]

   :allow-attributes
   ["src"
    :matching [re-youtube]
    :on-elements ["iframe"]]

   :allow-standard-url-protocols))


(defn san-html
  [page-url html]
  (html-sanitize
   (make-html-policy page-url)
   html))


(def san-bare (partial html-sanitize))


(def sample
  "<a href=\"http://github.com/\">GitHub</a>

<iframe src='https://www.youtube.com/embed/8NN2PFjs0A0' width='32'>fuck</iframe>

  <h1 >sdfsdfs</h1>

  <img src='/images/test.jpg' width='sdfsdf'>


")
