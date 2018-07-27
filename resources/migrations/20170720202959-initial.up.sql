begin;

drop table if exists users;
create table users (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone,

    email       text not null,
    name        text,
    source      text,
    source_id   text,
    source_url  text,
    locale      text,
    avatar_url  text,
    gender      text,

    sync_interval    integer not null default 3600,
    sync_date_last   timestamp with time zone,
    sync_date_next   timestamp with time zone,
    sync_count_total integer not null default 0,

    auth_data   jsonb,

    unique (email)
);

drop table if exists feeds;
create table feeds (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone,

    url_source  text not null,
    url_host    text,
    url_favicon text,
    url_image   text,

    language    text,

    title       text,
    subtitle    text,

    link        text,

    upd_period  text,
    upd_freq    text,

    date_updated_at timestamp with time zone,

    http_status    integer,
    http_etag      text,
    http_modified  text,

    parse_ok       boolean not null default true,
    parse_err      text,

    last_entry_count  integer not null default 0,

    sync_interval    integer not null default 3600,
    sync_date_last   timestamp with time zone,
    sync_date_next   timestamp with time zone,
    sync_count_total integer not null default 0,
    sync_count_err   integer not null default 0,
    sync_error_msg   text,

    entry_count_total integer not null default 0,
    sub_count_total   integer not null default 0,

    unique (url_source)
);


drop table if exists entries;
create table entries (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone,

    feed_id     integer references feeds(id),

    guid        text not null,
    link        text,
    author      text,
    title       text,
    summary     text,

    enclosure_url   text,
    enclosure_mime  text,

    date_published_at   timestamp with time zone,
    date_updated_at     timestamp with time zone,

    unique (feed_id, guid)
);


drop table if exists subs;
create table subs (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone,

    feed_id     integer references feeds(id),
    user_id     integer references users(id),

    label       text,

    title       text,

    layout      text,
    ordering    text,
    auto_read   boolean not null default true,
    unread_only boolean not null default true,

    message_count_total integer not null default 0,
    message_count_unread integer not null default 0,

    unique (feed_id, user_id)
);

drop table if exists messages;
create table messages (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone,

    entry_id    integer references entries(id),
    user_id     integer references users(id),

    is_read     boolean not null default false,
    date_read   timestamp with time zone,

    unique (user_id, entry_id)
);

commit;
