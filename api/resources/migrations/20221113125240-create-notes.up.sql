-- resources/migrations/20221113125240-create-notes.up.sql

create table notes (
  id           bigint unsigned auto_increment primary key,
  uri          varchar(1000)   not null unique comment 'ActivityPub URI',
  url          varchar(1000)   not null comment 'Human-readable URI (probably HTML)',
  content      text(1000),
  spoiler      text(200),
  local        boolean         not null default false,
  account_id   bigint unsigned references accounts(id) on delete cascade,
  visibility   enum ('public', 'unlisted', 'limited', 'direct')
                               not null default 'public',
  published_at datetime        not null default current_timestamp,
  updated_at   timestamp       not null default current_timestamp on update current_timestamp
) character set 'utf8mb4';
