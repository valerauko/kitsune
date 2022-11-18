-- resources/migrations/20221118051519-create-boosts.up.sql

create table boosts (
  id           bigint unsigned auto_increment primary key,
  uri          varchar(1000)   not null unique,
  local        boolean not     null default false,
  account_id   bigint unsigned references accounts(id) on delete cascade,
  note_id      bigint unsigned references notes(id) on delete cascade,
  published_at datetime        not null default current_timestamp
) character set 'utf8mb4';
