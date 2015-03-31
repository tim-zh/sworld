# --- !Ups

alter table "game_entities" add column "radius" double;
update "game_entities" set "radius" = 8;
alter table "game_entities" alter column "radius" set not null;

# --- !Downs

alter table "game_entities" drop column "radius";