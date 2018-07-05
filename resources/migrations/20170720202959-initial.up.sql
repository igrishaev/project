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
)


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
)


drop table if exists subs;
create table subs (
    id          serial primary key,
    created_at  timestamp with time zone not null default current_timestamp,
    updated_at  timestamp with time zone null,
    deleted     boolean not null default false,

    feed_id     integer not null references feeds(id),
    user_id     integer not null references users(id),

    title       text not null
)

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
)

create unique index messages_entry_user_unique ON subs (entry_id, user_id) where not deleted;

commit;


------------------


-- begin;

-- create table users (
--     id              serial primary key,
--     date_created_at timestamp not null default current_timestamp,
--     date_updated_at timestamp not null default current_timestamp
-- );

-- (sql/format {:insert-into :aaa :values {:foo 42}})
-- (sql/format {:set {:foo #sql/raw EXCLUDED.foo :id #sql/raw EXCLUDED.id}})

-- :on-conflict {:target :foo
--               :do :nothing}

-- create table sources (
--     id                serial primary key,
--     date_created_at   timestamp not null default current_timestamp,
--     date_updated_at   timestamp not null default current_timestamp,
--     date_last_sync    timestamp not null default current_timestamp,
--     date_next_sync    timestamp not null default current_timestamp,
--     title             text not null default '',
--     language          text not null default '',
--     description       text not null default '',
--     url_source        text not null default '',
--     url_site          text not null default '',
--     url_favicon       text not null default '',
--     url_image         text not null default '',
--     date_published_at timestamp not null default current_timestamp,
--     last_update_ok    boolean not null default false,
--     last_update_msg   text not null default '',
--     update_count      integer not null default 0,
--     message_count     integer not null default 0,
--     active            boolean not null default true,
--     unique            (url_source)
-- );

-- create table messages (
--     id                serial primary key,
--     date_created_at   timestamp not null default current_timestamp,
--     date_updated_at   timestamp not null default current_timestamp,
--     source_id         integer not null references sources(id),
--     title             text not null default '',
--     guid              text not null default '',
--     author            text not null default '',
--     date_published_at timestamp not null default current_timestamp,
--     url_link          text not null default '',
--     url_cover         text not null default '',
--     body              text not null default '',
--     unique            (source_id, guid)
-- );

-- create table subscriptions (
--     id               serial primary key,
--     user_id          integer not null references users(id),
--     source_id        integer not null references sources(id),
--     date_created_at  timestamp not null default current_timestamp,
--     date_updated_at  timestamp not null default current_timestamp,
--     total_msg_count  integer not null default 0,
--     unread_msg_count integer not null default 0,
--     date_last_sync   timestamp not null default current_timestamp,
--     date_next_sync   timestamp not null default current_timestamp,
--     active           boolean not null default true,
--     unique           (user_id, source_id)
-- );

-- create table notifications (
--     id              serial primary key,
--     user_id         integer not null references users(id),
--     message_id      integer not null references messages(id),
--     subscription_id integer not null references subscriptions(id),
--     date_created_at timestamp not null default current_timestamp,
--     date_updated_at timestamp not null default current_timestamp,
--     is_read         boolean not null default false,
--     unique          (user_id, message_id)
-- );

-- create table tags (
--     id              serial primary key,
--     date_created_at timestamp not null default current_timestamp,
--     name            text not null,
--     unique          (name)
-- );

-- create table source_tags (
--     id              serial primary key,
--     date_created_at timestamp not null default current_timestamp,
--     source_id       integer not null references sources(id),
--     tag_id          integer not null references tags(id),
--     unique          (source_id, tag_id)
-- );

-- create table message_tags (
--     id              serial primary key,
--     date_created_at timestamp not null default current_timestamp,
--     message_id      integer not null references messages(id),
--     tag_id          integer not null references tags(id),
--     unique          (message_id, tag_id)
-- );

-- commit;
