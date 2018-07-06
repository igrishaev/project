(ns project.error
  "A set of wrappers for exceptions.")

(def ^:dynamic
  *msg-limit* 255)

(defmacro with-msg-limit
  [limit & body]
  `(binding [*msg-limit* ~limit]
     ~@body))

(defn error!
  ([msg]
   (throw (Exception. msg)))
  ([tpl & args]
   (error! (apply format tpl args))))

(defn- shrink
  [str limit]
  (if (> (count str) limit)
    (subs str 0 limit)
    str))

(defn ^String exc-msg
  "Returns a message string for an exception instance."
  [^Exception e]
  (let [class (-> e .getClass .getCanonicalName)
        message (-> e .getMessage (or "<no message>"))]
    (shrink (format "%s: %s" class message) *msg-limit*)))

(defmacro with-catch
  [& body]
  `(try
     ~@body
     (catch Exception e#)))
