(ns project.utils)

(defmacro safe [& body] ;; todo move
  `(try
     ~@body
     (catch Exception e#
       nil)))
