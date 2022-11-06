-- resources/migrations/20221105083629-create-accounts-table.up.sql

create table if not exists accounts (
  id            bigint unsigned auto_increment primary key,
  acct          varchar(250)    not null unique,
  name          varchar(100)    not null,
  uri           varchar(1000)   not null unique,
  inbox         varchar(1000)   not null,
  shared_inbox  varchar(1000)   not null,
  public_key    text,
  display_name  varchar(250),
  created_at    timestamp       not null default current_timestamp,
  updated_at    timestamp       not null default current_timestamp on update current_timestamp
) character set 'utf8mb4';
