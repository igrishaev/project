begin;

--

create or replace function date_updated_at_fn()
returns trigger as $$
begin
    new.date_updated_at = current_timestamp;
    return new;
end
$$ language plpgsql;

create trigger users_date_updated_at_trg
    before update on users
    for each row execute procedure date_updated_at_fn();

create trigger sources_date_updated_at_trg
    before update on sources
    for each row execute procedure date_updated_at_fn();

create trigger messages_date_updated_at_trg
    before update on messages
    for each row execute procedure date_updated_at_fn();

create trigger subscriptions_date_updated_at_trg
    before update on subscriptions
    for each row execute procedure date_updated_at_fn();

create trigger notifications_date_updated_at_trg
    before update on notifications
    for each row execute procedure date_updated_at_fn();

--

commit;
