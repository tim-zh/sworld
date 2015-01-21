# --- !Ups

alter table "game_entities" add column "max_speed" double default 100 not null;

# --- !Downs

alter table "game_entities" drop column "max_speed";