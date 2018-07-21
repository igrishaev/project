
-- :name dev-sync-all-subs-counters :! :n
update subs s
set
  updated_at = now(),
  message_count_total = q.count_total,
  message_count_unread = q.count_unread
from (
  select
    e.feed_id as feed_id,
    count(m.id) as count_total,
    count(m.id) filter (where m.is_read = false) as count_unread
  from entries e
  left join messages m on m.entry_id = e.id
  group by e.feed_id
) as q
where
  s.feed_id = q.feed_id
