begin;

drop index if exists feeds_trgm_idx;
create index feeds_trgm_idx on feeds using gin (
    (coalesce(url_host, '') || ' ' || coalesce(title, '') || ' ' || coalesce(subtitle, ''))
    gin_trgm_ops);

commit;
