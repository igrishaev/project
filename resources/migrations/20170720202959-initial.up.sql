begin;

drop table if exists users;
create table users (
    id          serial primary key,
    created_at  timestamp with time zone
                not null default current_timestamp,
    updated_at  timestamp with time zone null,
    deleted     boolean not null default false,

    email       text not null,
    name        text null,
    source      text null,
    source_id   text null,
    source_url  text null,
    locale      text null,
    avatar_url  text null,
    gender      text null,

    sync_interval    integer not null default 3600,
    sync_date_last   timestamp with time zone null,
    sync_date_next   timestamp with time zone null,
    sync_count_total integer not null default 0,

    auth_data   jsonb null,

    constraint  email_unique unique (email)
);

drop table if exists feeds;
create table feeds (
    id          serial primary key,
    created_at  timestamp with time zone
                not null default current_timestamp,
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

    parse_ok       boolean not null default true,
    parse_err      text null,

    sync_interval    integer not null default 3600,
    sync_date_last   timestamp with time zone null,
    sync_date_next   timestamp with time zone null,
    sync_count_total integer not null default 0,
    sync_count_err   integer not null default 0,
    sync_error_msg   text null,

    entry_count_total integer not null default 0,

    constraint feeds_url_source_unique unique (url_source) -- todo index
);


drop table if exists entries;
create table entries (
    id          serial primary key,
    created_at  timestamp with time zone
                not null default current_timestamp,
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
    created_at  timestamp with time zone
                not null default current_timestamp,
    updated_at  timestamp with time zone null,
    deleted     boolean not null default false,

    feed_id     integer not null references feeds(id),
    user_id     integer not null references users(id),

    title       text null,

    message_count_total integer not null default 0,
    message_count_unread integer not null default 0
);

create unique index subs_feed_user_unique ON subs
    (feed_id, user_id) where (not deleted);


drop table if exists messages;
create table messages (
    id          serial primary key,
    created_at  timestamp with time zone
                not null default current_timestamp,
    updated_at  timestamp with time zone null,
    deleted     boolean not null default false,

    entry_id    integer not null references entries(id),
    sub_id      integer not null references subs(id),

    is_read     boolean not null default false,
    date_read   timestamp with time zone null
);

create unique index messages_sub_entry_unique ON messages
    (sub_id, entry_id) where (not deleted);

commit;
