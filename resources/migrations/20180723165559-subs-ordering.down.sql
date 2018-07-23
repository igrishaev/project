begin;

alter table subs
    drop column ordering;

drop table subs_ordering;

commit;
