# --- !Ups

create table "users" (
	"id" BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	"version" BIGINT DEFAULT 0 NOT NULL,
	"name" VARCHAR NOT NULL,
	"password" VARCHAR NOT NULL,
  "location" VARCHAR NOT NULL,
  "x" DOUBLE DEFAULT 0 NOT NULL,
  "y" DOUBLE DEFAULT 0 NOT NULL);

# --- !Downs

drop table "users";