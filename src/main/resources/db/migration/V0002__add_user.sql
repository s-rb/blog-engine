update hibernate_sequence set next_val= 2 where next_val=1;
insert into users (email, password, is_moderator, name, reg_time, id)
values ('surkov.r.b@gmail.com', 'FE5C313BB3F8D944F506B0628092C48C', 1, 'Roman Surkov', NOW(), 1);