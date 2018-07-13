(ns project.sync
  (:require [project.fetch :as fetch]
            [project.db :as db]
            [project.models :as models]
            [project.error :as e]
            [project.time :refer [parse-iso-now]]
            [project.util :as u]

            [clojure.tools.logging :as log]
            [medley.core :refer [distinct-by]]))

(defn map-pred
  [params]
  (fn [other]
    (= params
       (select-keys other (keys params)))))

(defn map-find
  [params items]
  (-> (map-pred params)
      (filter items)
      first))

(defn clean-val
  [val]
  (when val
    (not-empty (clojure.string/trim val))))


(defn get-guid
  [entry]
  (let [{:keys [id
                link
                wp_uuid
                title
                summary]} entry]
    (or (clean-val id)
        (clean-val link)
        (clean-val wp_uuid)
        (clean-val title)
        (clean-val summary)
        (u/uuid))))

(defn data->feed
  [data]
  (let [{:keys [feed
                entries

                bozo
                bozo_exception

                status
                headers

                sy_updateperiod
                sy_updatefrequency

                ]} data

        {:keys [etag last-modified]} headers

        {:keys [language
                title
                subtitle
                link
                updated_parsed
                image]} feed

        {image_href :href} image

        ]

    {:url_image image_href
     :language language

     :title title
     :subtitle subtitle

     :link link

     :upd_period sy_updateperiod
     :upd_freq sy_updatefrequency

     :date_updated_at (parse-iso-now updated_parsed)

     :last_entry_count (count entries)

     :http_status status
     :http_etag etag
     :http_modified last-modified

     :parse_ok (= bozo 0)
     :parse_err bozo_exception

     }))

(defn data->entry
  [entry]

  (let [{:keys [link
                href
                links
                author
                title
                summary
                published_parsed
                updated_parsed]} entry

        enc (map-find {:rel "enclosure"} links)
        {enc_url :href enc_mime :type} enc

        ]

    {:guid (get-guid entry)
     :link link
     :author author
     :title title
     :summary summary

     :enclosure_url enc_url
     :enclosure_mime enc_mime

     :date_published_at (parse-iso-now published_parsed)
     :date_updated_at (parse-iso-now updated_parsed)

     }))

(defn update-feed-entry-count
  [feed_id]
  (db/execute!
   ["
update feeds
set
  updated_at = now(),
  entry_count_total = q.entry_count
from (
  select
    e.feed_id as feed_id,
    count(e.id) as entry_count
  from entries e
  where
    e.feed_id = ?
  group by e.feed_id
) as q
where
  id = q.feed_id
" feed_id]))

(defn update-feed-sub-count
  [feed_id]
  (db/execute!
   ["
update feeds
set
  updated_at = now(),
  sub_count_total = q.sub_count
from (
  select
    s.feed_id as feed_id,
    count(s.id) as sub_count
  from subs s
  where
    s.feed_id = ?
  group by s.feed_id
) as q
where
  id = q.feed_id
" feed_id]))

(defn sync-feed
  [feed]
  (let [{feed-url :url_source feed-id :id} feed
        data (fetch/fetch feed-url)]

    ;; todo cleanup nils

    (let [feed-db (data->feed data)

          entries (map data->entry (:entries data))
          entries (distinct-by :guid entries)]

      (db/with-tx

        (models/update-feed feed-id feed-db)

        (when-not (empty? entries)
          (models/upsert-entry
           (for [e entries]
             (assoc e :feed_id feed-id))))

        (update-feed-entry-count feed-id)
        (update-feed-sub-count feed-id)))))

(defn sync-feed-safe
  [feed]
  (let [{url :url_source id :id} feed
        fields (transient {})]

    (try
      (sync-feed feed)

      (catch Throwable e
        (let [err-msg (e/exc-msg e)]

          (log/errorf "Sync error, feed: %s, e: %s" url err-msg)
          (assoc!
           fields
           :sync_count_err (db/raw "sync_count_err + 1")
           :sync_error_msg err-msg)))

      (finally
        (assoc!
         fields
         :updated_at :%now
         :sync_date_last :%now
         :sync_date_next
         (db/raw "now() + sync_interval * interval '1 second'")

         :sync_count_total (db/raw "sync_count_total + 1"))

        (let [fields (persistent! fields)]
          (models/update-feed id fields))))))

(defn sync-feed-url
  [url]
  (let [feed (models/get-or-create-feed url)]
    (sync-feed-safe feed)))

(defn sync-subs-messages
  [user_id]
  (db/execute!
   ["
insert into messages (user_id, entry_id)

select
  s.user_id as user_id,
  e.id as entry_id

from
  subs s
  join entries e
    on e.feed_id = s.feed_id
  left join messages m
    on m.entry_id = e.id and m.user_id = s.user_id

where
  s.user_id = ?
  and m.id is null

" user_id]))

(defn sync-subs-counters
  [user_id]
  (db/execute!
   ["
update subs
set
  updated_at = now(),
  message_count_total = q.count_total,
  message_count_unread = q.count_unread
from (
  select
    s.id as sub_id,
    count(m.id) as count_total,
    count(m.id) filter (where not m.is_read) as count_unread
  from subs s
  join messages m on m.sub_id = s.id
  where
    s.user_id = ?
  group by s.id
) as q
where
  id = q.sub_id
" user_id]))

(defn sync-user-counters
  [user_id]
  (db/execute!
   ["
update users
set
  updated_at = now(),
  sync_date_last = now(),
  sync_date_next = now() + sync_interval * interval '1 second',
  sync_count_total = sync_count_total + 1
where id = ?
" user_id]))

(defn get-users-to-sync
  []
  (db/query
   "
select *
from users
where
  and (sync_date_next is null
       or sync_date_next < now())
order by
  sync_date_next asc nulls first
limit 100
"))

(defn get-feeds-to-sync
  []
  (db/query
   "
select *
from feeds
where
  and (sync_date_next is null
       or sync_date_next < now())
order by
  sync_date_next asc nulls first
limit 100
"))

(defn sync-user
  ;; todo wrap with log etc
  [user_id]
  (db/with-tx
    (sync-subs-messages user_id)
    (sync-subs-counters user_id)
    (sync-user-counters user_id)))

(defn sync-user-safe
  ;; todo wrap with log etc
  [user]
  (sync-user (:id user)))

;;
;; Local import
;;

(defn feed-import
  []
  (with-open [rdr (clojure.java.io/reader "rss.txt")]
    (doseq [url (line-seq rdr)]
      (println url)
      (sync-feed-url url))))
