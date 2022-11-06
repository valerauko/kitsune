-- resources/migrations/20221105171430-create-users-table.up.sql

create table if not exists users (
  id            bigint unsigned auto_increment primary key,
  account_id    bigint unsigned references accounts(id) on delete cascade,
  private_key   text            not null,
  created_at    timestamp       not null default current_timestamp,
  updated_at    timestamp       not null default current_timestamp on update current_timestamp
) character set 'utf8mb4';
