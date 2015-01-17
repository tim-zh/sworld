# --- !Ups

create table "game_entities" (
	"id" bigint not null auto_increment primary key,
  "type" varchar not null,
  "name" varchar not null,
  "location" varchar not null,
  "x" double default 0 not null,
  "y" double default 0 not null,
  "view_radius" double default 100 not null);

alter table "users" drop column "location";
alter table "users" drop column "x";
alter table "users" drop column "y";

delete from "users";
alter table "users" add column "entity_id" bigint not null;

# --- !Downs

drop table "game_entities";

alter table "users" drop column "entity_id";

alter table "users" add column "location" varchar default 'default' not null;
alter table "users" add column "x" double default 0 not null;
alter table "users" add column "y" double default 0 not null;