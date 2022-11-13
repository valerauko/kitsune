-- resources/migrations/20221113115721-create-follows-table.up.sql

create table follows (
  follower_id bigint unsigned not null references accounts(id),
  followed_id bigint unsigned not null references accounts(id),
  accepted_at timestamp not null default current_timestamp,
  unique follow (follower_id, followed_id)
) character set 'utf8mb4';
