# --- !Ups

create table "users" (
	"id" bigint not null auto_increment primary key,
	"version" bigint default 0 not null,
	"name" varchar not null,
	"password" varchar not null,
  "location" varchar not null,
  "x" double default 0 not null,
  "y" double default 0 not null);

# --- !Downs

drop table "users";