
-- :name get-user-feeds :? :*
select
    f.*,
    row_to_json(s) as sub
from
    feeds f
    join subs s on s.feed_id = f.id
where
    s.user_id = :user_id


-- :name get-subscribed-entries :? :*
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


-- :name mark-read :! :n
update messages
set
  is_read = :is_read
  date_read = now(), -- todo
  updated_at = now()
where
  id = :message_id
  and user_id = :user_id


-- :name upsert3 :<!
insert into :i:table (
/*~ (let [[fields values] (apply map vector (:values params))] */
/*~ "a, b, c" */
)
/*~ ) ~*/
where true

-- :name test
select 1
