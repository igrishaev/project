
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
  e.id desc
limit :limit

on conflict (user_id, entry_id)
do update set
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
    join subs s on s.feed_id = f.id
where
    s.user_id = :user_id
    and f.id = :feed_id


-- :name get-subscribed-entries :? :*
select
  e.*,
  row_to_json(m) as message
from
  messages m, entries e
where
  e.feed_id = :feed_id
  and m.entry_id = e.id
  and m.user_id = :user_id
/*~ (when (:unread_only params) */
  and not m.is_read
/*~ ) ~*/
/*~ (when (= (:ordering params) "new_first") */
  order by e.id asc
/*~ ) ~*/
/*~ (when (= (:ordering params) "old_first") */
  order by e.id desc
/*~ ) ~*/

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
