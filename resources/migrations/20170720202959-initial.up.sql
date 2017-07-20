begin;

create table users (
    id serial primary key
);

create table sources (
    id              serial primary key,
    date_created_at timestamp not null default current_timestamp,
    date_updated_at timestamp not null default current_timestamp,
    date_last_sync  timestamp not null default current_timestamp,
    date_next_sync  timestamp not null default current_timestamp,
    title           text not null default '',
    description     text not null default '',
    url_src         text not null default '' unique,
    url_site        text not null default '',
    url_favicon     text not null default '',
    url_banner      text not null default '',
    last_update_ok  boolean not null default false,
    last_update_msg text not null default '',
    update_count    integer not null default 0,
    message_count   integer not null default 0,
    active          boolean not null default true
);

create table messages (
    id                serial primary key,
    date_created_at   timestamp not null default current_timestamp,
    date_updated_at   timestamp not null default current_timestamp,
    source_id         integer not null references sources(id),
    title             text not null default '',
    guid              text not null default '',
    date_published_at timestamp not null default current_timestamp,
    url_link          text not null default '',
    url_cover         text not null default '',
    body_html         text not null default '',
    body_text         text not null default '',
    unique            (guid, source_id)
);

create table subscriptions (
    id               serial primary key,
    user_id          integer not null references users(id),
    source_id        integer not null references sources(id),
    date_created_at  timestamp not null default current_timestamp,
    date_updated_at  timestamp not null default current_timestamp,
    total_msg_count  integer not null default 0,
    unread_msg_count integer not null default 0,
    date_last_sync   timestamp not null default current_timestamp,
    date_next_sync   timestamp not null default current_timestamp,
    active           boolean not null default true,
    unique           (user_id, source_id)
);

create table notifications (
    id              serial primary key,
    user_id         integer not null references users(id),
    message_id      integer not null references messages(id),
    date_created_at timestamp not null default current_timestamp,
    date_updated_at timestamp not null default current_timestamp,
    is_read         boolean not null default false,
    date_read_at    timestamp null,
    unique (user_id, message_id)
);

commit;
