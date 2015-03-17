# --- !Ups

alter table "game_entities" add column "voice_radius" double default 100 not null;

# --- !Downs

alter table "game_entities" drop column "voice_radius";