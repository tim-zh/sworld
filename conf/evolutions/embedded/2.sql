# --- !Ups

create table "game_entity" (
	"id" BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  "name" VARCHAR NOT NULL,
  "location" VARCHAR NOT NULL,
  "x" DOUBLE DEFAULT 0 NOT NULL,
  "y" DOUBLE DEFAULT 0 NOT NULL);

alter table "users" drop column "location";
alter table "users" drop column "x";
alter table "users" drop column "y";

delete from "users";
alter table "users" add column "entity_id" bigint not null;

# --- !Downs

drop table "game_entity";

alter table "users" drop column "entity_id";

alter table "users" add column "location" varchar default 'default' not null;
alter table "users" add column "x" double default 0 not null;
alter table "users" add column "y" double default 0 not null;