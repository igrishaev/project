
-- :name unsub-del-sub :! :n
delete from subs s
where
    s.feed_id = :feed_id
    and s.user_id = :user_id


-- :name unsub-del-messages :! :n
delete from messages m
using entries e
where
    e.feed_id = :feed_id
    and m.entry_id = e.id
    and m.user_id = :user_id


-- :name search-feeds-by-term :? :*

/*~ (when (not-empty (:feed_ids params)) */
select
  f.*
from feeds f
where f.id in (:v*:feed_ids)

union
/*~ ) ~*/

select *
from feeds
where
  (coalesce(url_host, '') || ' ' || coalesce(title, '') || ' ' || coalesce(subtitle, ''))
  ilike :term

limit :limit


-- :name get-feeds-by-urls :? :*
select *
from feeds
where
  url_source in (:v*:urls)

-- :name bump-subs-counters :! :n
update subs s
set
  message_count_total = message_count_total + :delta_total,
  message_count_unread = message_count_unread + :delta_unread
where
  s.feed_id = :feed_id

-- :name subscribe-user-to-the-last-feed-entries :! :n
insert into
  messages (user_id, entry_id)
select
  :user_id as user_id,
  e.id as entry_id
from
  entries e
where
  e.feed_id = :feed_id
order by
  e.date_published_at desc nulls first
limit
  :limit
on conflict
  (user_id, entry_id)
do update
set
  is_read = false,
  date_read = null,
  updated_at = now()


-- :name get-last-entries :? :*
select e.*
from entries e
where e.feed_id = :feed_id
order by e.id desc
limit 5


-- :name get-user-feeds :? :*
select
    f.*,
    row_to_json(s) as sub
from
    feeds f
    join subs s on s.feed_id = f.id
where
    s.user_id = :user_id


-- :name get-single-full-feed :? :1
select
    f.*,
    row_to_json(s) as sub
from
    feeds f
    left join subs s
      on s.feed_id = f.id and s.user_id = :user_id
where
    f.id = :feed_id

-- :name get-full-entry :? :1
select
  e.*,
  row_to_json(m) as message
from
  messages m, entries e
where
  e.id = :entry_id
  and m.entry_id = e.id


-- :name get-subscribed-entries :? :*
select
  e.*,
  row_to_json(m) as message
from
  entries e
join messages m
  on m.entry_id = e.id and m.user_id = :user_id
where
  e.feed_id = :feed_id

/*~ (when (:unread_only params) */
  and not m.is_read
/*~ ) ~*/

/*~
(case (:ordering params)
  "new_first" */ order by e.date_published_at desc  /*~
  "old_first" */ order by e.date_published_at asc /*~
)
~*/

limit :limit
offset :offset

-- :name unsubscribe :! :n
delete from subs
where
    feed_id = :feed_id
    and user_id = :user_id


-- :name get-user-sub :? :1
select
    f.*,
    row_to_json(s) as sub
from
    feeds f
    join subs s on s.feed_id = f.id
where
    s.user_id = :user_id
    and s.id = :sub_id


-- todo remove that?
-- -- :name mark-read :! :n
-- update messages
-- set
--   is_read = :is_read
--   date_read = now(), -- todo
--   updated_at = now()
-- where
--   id = :message_id
--   and user_id = :user_id


-- :name upsert3 :<!
insert into :i:table (
/*~ (let [[fields values] (apply map vector (:values params))] */
/*~ "a, b, c" */
)
/*~ ) ~*/
where true
