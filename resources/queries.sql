
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
