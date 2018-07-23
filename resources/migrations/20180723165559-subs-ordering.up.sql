begin;

create table subs_ordering (
    type text primary key
);

insert into subs_ordering
values ('new_first'), ('old_first');

alter table subs
    add column ordering text null references subs_ordering(type);

commit;
