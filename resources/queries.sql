
-- :name source-exists? :? :1
select 1
from sources
where url_source = :url

-- :name get-source-by-url :? :1
select *
from sources
where url_source = :url

-- :name get-source-last-messages :? :n
select m.*
from messages m
where m.source_id = :source_id
order by
    date_published_at desc
limit :limit

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
