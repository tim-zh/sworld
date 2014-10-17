# --- !Ups

create table "users" (
	"id" BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	"version" BIGINT DEFAULT 0 NOT NULL,
	"name" VARCHAR NOT NULL,
	"password" VARCHAR NOT NULL);

# --- !Downs

drop table "users";