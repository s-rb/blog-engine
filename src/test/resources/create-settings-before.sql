delete from global_settings;

insert into global_settings(id, code, name, value) values
(130, 'MULTIUSER_MODE', 'Многопользовательский режим', 'NO'),
(131, 'POST_PREMODERATION', 'Премодерация постов', 'YES'),
(132, 'STATISTICS_IS_PUBLIC', 'Показывать всем статистику блога', 'NO');