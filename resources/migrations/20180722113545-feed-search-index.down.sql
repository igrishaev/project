begin;

drop index if exists feeds_trgm_idx;

drop extension if exists pg_trgm;

commit;
