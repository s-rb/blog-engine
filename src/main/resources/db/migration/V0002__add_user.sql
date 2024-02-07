SELECT setval('hibernate_sequence', 2, false);
insert into users (email, password, is_moderator, name, reg_time, id)
values ('surkov.r.b@gmail.com', 'FE5C313BB3F8D944F506B0628092C48C', true, 'Roman Surkov', NOW(), 1);