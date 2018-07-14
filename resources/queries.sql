
-- :name unsubscribe :! :n
delete from subs
where
    feed_id = :feed_id
    and user_id = :user_id

-- :name get-user-subs :? :n
select
    f.*,
    row_to_json(s) as sub
from
    feeds f
    join subs s on s.feed_id = f.id
where
    s.user_id = :user_id


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

-- :name get-messages :? :n
-- :doc todo add from_id
select
  e.*,
  row_to_json(m) as message
from
  entries e
join messages m
  on m.entry_id = e.id and not m.is_read
where
  e.feed_id = :feed_id
order by
  e.id desc


-- :name mark-read :! :n
update messages
set
  is_read = :is_read
  date_read = now(), -- todo
  updated_at = now()
where
  id = :message_id
  and user_id = :user_id


-- :name sync-subs-messages :! :n
insert into messages (user_id, entry_id)

select
  s.user_id as user_id,
  e.id as entry_id

from
  subs s
  join entries e on e.feed_id = s.feed_id
  left join messages m on m.entry_id = e.id and m.user_id = s.user_id

where
  s.user_id = :user_id
  and m.id is null

-- :name sync-feed-entry-count :! :n
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


-- :name get-feeds-to-sync :? :n
select *
  from feeds
where
  sync_date_next is null
  or sync_date_next < now()
order by
  sync_date_next asc nulls first
limit 100


-- :name sync-subs-counters :! :n
-- :todo wrong
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
    s.user_id = :user_id
  group by s.id
) as q
where
  id = q.sub_id


-- :name sync-user-counters :! :n
update users
set
  updated_at = now(),
  sync_date_last = now(),
  sync_date_next = now() + sync_interval * interval '1 second',
  sync_count_total = sync_count_total + 1
where id = :user_id


-- :name get-users-to-sync :? :n
select *
from users
where
  and (sync_date_next is null or sync_date_next < now())
order by
  sync_date_next asc nulls first
limit 100
