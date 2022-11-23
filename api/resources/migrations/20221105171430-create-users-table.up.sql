-- resources/migrations/20221105171430-create-users-table.up.sql

create table users (
  id            bigint unsigned auto_increment primary key,
  account_id    bigint unsigned not null references accounts(id),
  private_key   text            not null,
  email         tinytext        not null,
  password      text            not null,
  created_at    timestamp       not null default current_timestamp,
  updated_at    timestamp       not null default current_timestamp on update current_timestamp
) character set 'utf8mb4';
