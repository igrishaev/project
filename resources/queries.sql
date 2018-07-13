
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
  join entries e
    on e.feed_id = s.feed_id
  left join messages m
    on m.entry_id = e.id and m.user_id = s.user_id

where
  s.user_id = :user_id
  and m.id is null
