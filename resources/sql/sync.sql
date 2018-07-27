

--
-- Feeds
--


-- :name sync-feed-entry-count :! :n
-- :doc ok
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
    e.feed_id = :feed_id
  group by e.feed_id
) as q
where
  id = q.feed_id


-- :name sync-feed-sub-count :! :n
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
    s.feed_id = :feed_id
  group by s.feed_id
) as q
where
  id = q.feed_id


-- :name rotate-feeds-to-update :? :*
update feeds
set
  updated_at = now(),
  sync_date_next = now() + sync_interval * interval '1 second'
from (
  select
    id as feed_id
  from feeds
  where
    sync_date_next is null
    or sync_date_next < now()
  order by
    sync_date_next asc nulls first
  limit :limit
) as q
where
  id = q.feed_id
returning
  id, url_source


--
-- Users
--


-- :name rotate-users-to-update :? :*
update users
set
  updated_at = now(),
  sync_date_next = now() + sync_interval * interval '1 second'
from (
  select
    id as user_id
    from users
  where
    sync_date_next is null
    or sync_date_next < now()
  order by
    sync_date_next asc nulls first
  limit :limit
) as q
where
  id = q.user_id
returning
  id


-- :name sync-user-new-messages :! :n
insert into messages (user_id, entry_id)
select
  s.user_id as user_id,
  e.id as entry_id
from subs s
join entries e on e.feed_id = s.feed_id
left join messages m on m.entry_id = e.id and m.user_id = s.user_id
where
    s.user_id = :user_id
/*~ (when (:feed_id params) */
    and s.feed_id = :feed_id
/*~ ) ~*/
    and m.id is null


-- :name sync-subs-counters :! :n
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
  from
    subs s, entries e, messages m
  where
    s.feed_id = e.feed_id
    and m.entry_id = e.id
/*~ (when (:feed_id params) */
    and s.feed_id = :feed_id
/*~ ) ~*/
/*~ (when (:user_id params) */
    and s.user_id = :user_id
/*~ ) ~*/
  group by s.id
) as q
where
  id = q.sub_id


-- :name sync-user-sync-counters :! :n
update users
set
  updated_at = now(),
  sync_date_last = now(),
  sync_date_next = now() + sync_interval * interval '1 second',
  sync_count_total = sync_count_total + 1
where
  id = :user_id
