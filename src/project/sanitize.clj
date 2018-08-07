(ns project.sanitize

  (:import org.jsoup.Jsoup
           (org.jsoup.safety Whitelist Cleaner)
           org.jsoup.nodes.Entities$EscapeMode))


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


(defn array
  [& vals]
  (into-array String vals))


(def wl-html
  (doto (new Whitelist)
    (.addTags (apply array tags-allowed))

    (.addAttributes "img" (array "src"))
    (.addAttributes "iframe" (array "src" "allowfullscreen"))
    (.addAttributes "a" (array "href"))

    (.addProtocols "a" "href" (array "ftp" "http" "https" "mailto"))
    (.addProtocols "img" "src" (array "http" "https"))
    (.addProtocols "iframe" "src" (array "http" "https"))))


(def cl-html
  (new Cleaner wl-html))


(def re-youtube
  #"(?i)youtube.com/embed")

(def re-vk
  #"(?i)vk.com/video_ext.php")

(def re-coube
  #"(?i)coub.com/embed")

(def re-soundcloud
  #"(?i)soundcloud.com/player")

(def re-vimeo
  #"(?i)player.vimeo.com/video")


(defn media-src?
  [src]
  (or (re-find re-youtube src)
      (re-find re-vk src)
      (re-find re-coube src)
      (re-find re-soundcloud src)
      (re-find re-vimeo src)))


(defn process-iframes
  [soup]
  (doseq [el (.select soup "iframe")]
    (let [src (.absUrl el "src")]
      (if (media-src? src)
        (.text el "")
        (.remove el)))))


(def xhtml Entities$EscapeMode/xhtml)

(defn san-html
  [html page-url]
  (when html
    (let [page-url (or page-url "")
          src (Jsoup/parse html page-url)]
      (process-iframes src)
      (let [out (.clean cl-html src)]
        (.. out outputSettings (escapeMode xhtml))
        (.. out body html)))))


(def wl-none (Whitelist/none))

(defn san-none
  [html]
  (when html
    (Jsoup/clean html wl-none)))


(def sample
  "
  <title>Ivan Grishaev's blog  </title>
  <a href=\"http://github.com/?foo=42&test=''123-42'\">GitHub</a>

  <a href='xxx/yyy/test.img'>GitHub</a>

<iframe src='https://www.youtube.com/embed/8NN2PFjs0A0' width='32'>fuck</iframe>

  <script>script</script>

  <p>paragraph</p>

  <h1 >sdfsdfs</h1>

<iframe src='https://vk.com/video_ext.php?oid=-30493961&id=456241043&hash=a3148de1cd04c77b' width='640' height='360' frameborder='0' allowfullscreen></iframe>

  <iframe src='/test' allowfullscreen></iframe>

  <img src='/images/test.jpg' width='42'>

<iframe src='//coub.com/embed/5oy44?muted=false&autostart=false&originalSize=false&startWithHD=false' allowfullscreen frameborder='0' width='626' height='480' allow='autoplay'></iframe>


")
