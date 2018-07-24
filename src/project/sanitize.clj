(ns project.sanitize
  (:require [autoclave.core :refer (html-policy html-sanitize)]))


(def re-youtube
  #"(?i)\Qhttps://www.youtube.com/embed/\E[a-zA-Z0-9_-]+")


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


(def p-html
  (html-policy
   :allow-elements tags-allowed
   :allow-attributes ["href" :on-elements ["a"]]
   :allow-attributes ["src" :on-elements ["img"]]
   :allow-attributes ["src" :matching [re-youtube] :on-elements ["iframe"]]
   :allow-standard-url-protocols))


(def san-html (partial html-sanitize p-html))


(def san-bare (partial html-sanitize))


(def sample
  "<a href=\"http://github.com/\">GitHub</a>

<iframe src='https://www.youtube.com/embed/8NN2PFjs0A0' width='32'>fuck</iframe>

  <h1 >sdfsdfs</h1>

  <img src='http://sdfsdfsf' width='sdfsdf'>


")
