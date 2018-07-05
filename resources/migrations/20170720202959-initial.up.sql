begin;

drop table if exists users;
create table users (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone null,
    deleted     boolean not null default false,
    email       text not null,
    name        text null,

    constraint  email_unique unique (email)
);

drop table if exists feeds;
create table feeds (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone null,
    deleted     boolean not null default false,

    url_source  text not null,
    url_host    text null,
    url_favicon text null,
    url_image   text null,

    language    text null,

    title       text null,
    subtitle    text null,

    link        text null,

    upd_period  text null,
    upd_freq    text null,

    date_updated_at   timestamp with time zone null,

    http_status    integer null,
    http_etag      text null,
    http_modified  text null,

    constraint feeds_url_source_unique unique (url_source)
);


drop table if exists entries;
create table entries (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone null,
    deleted     boolean not null default false,

    feed_id     integer not null references feeds(id),

    guid        text not null,
    link        text null,
    author      text null,
    title       text null,
    summary     text null,

    enclosure_url   text null,
    enclosure_mime  text null,

    date_published_at   timestamp with time zone null,
    date_updated_at     timestamp with time zone null,

    constraint entries_feed_guid_unique unique (feed_id, guid)
);


drop table if exists subs;
create table subs (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone null,
    deleted     boolean not null default false,

    feed_id     integer not null references feeds(id),
    user_id     integer not null references users(id),

    title       text not null
);

create unique index subs_feed_user_unique ON subs (feed_id, user_id) where not deleted;


drop table if exists messages;
create table messages (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone null,
    deleted     boolean not null default false,

    entry_id    integer not null references entries(id),
    user_id     integer not null references users(id),

    is_read     boolean not null default false,
    date_read   timestamp with time zone null
);

create unique index messages_entry_user_unique ON messages (entry_id, user_id) where not deleted;


commit;
