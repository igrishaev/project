
-- :name source-exists? :? :1
select 1
from sources
where url_source = :url

-- :name insert-messages :<!
insert into messages
       (source_id, guid, title, url_link, body_html, date_published_at)
values :tuple*:messages
on conflict (guid, source_id)
do update
set title = EXCLUDED.title,
    url_link = EXCLUDED.url_link,
    body_html = EXCLUDED.body_html,
    date_published_at = EXCLUDED.date_published_at
returning *

-- :name get-sources-to-sync :?
select id
from sources
where
    date_next_sync < current_timestamp
    and active
order by
    date_next_sync
limit :limit

-- :name mark-sources-synced :! :n
update sources
set date_next_sync = :next_sync
where id in (:v*:ids)


select
    s.*
from subscriptions s
left join messages m on s.feed_id = f.id
left join notifications n on n.message_id = m.id
    and n.user_id = :user_id
    and not n.is_read
where
    s.user_is = :user_id


--
select
    s.*,
    count(n.id) as foo
from subscriptions s
left join notifications n on s.feed_id = n.feed_id
     and n.user_id = :user_id
     and not n.is_read
where
    s.user_is = :user_id
group by s.id
