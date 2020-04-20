SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE global_settings;
TRUNCATE TABLE captcha_codes;
TRUNCATE TABLE post_comments;
TRUNCATE TABLE post_votes;
TRUNCATE TABLE tag2post;
truncate  TABLE tags;
TRUNCATE TABLE tag2post;
TRUNCATE TABLE posts;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS=1;

insert into global_settings(id, code, name, value) values
(130, 'MULTIUSER_MODE', 'Многопользовательский режим', 'NO'),
(131, 'POST_PREMODERATION', 'Премодерация постов', 'YES'),
(132, 'STATISTICS_IS_PUBLIC', 'Показывать всем статистику блога', 'NO');

INSERT INTO captcha_codes(id, code, secret_code, time)
values (31, 'daney', '0pjkbqtfzehqo3p65pislx', NOW()),
(32, 'fapey', 'cfgcftcraaz2xg5xlirlak', NOW());

INSERT INTO users(id, email, password, is_moderator, name, reg_time)
values (21, 'mail@mail.ru', '5F4DCC3B5AA765D61D8327DEB882CF99', 1, 'mail', NOW()),
(22, 'new@mail.ru', '5F4DCC3B5AA765D61D8327DEB882CF99', 0, 'new', NOW());

INSERT INTO users(id, code, email, password, is_moderator, name, reg_time)
values (23, 'restorecode', 'restoreuser@mail.ru', 'restoreuserpassword', 1, 'restoreuser', NOW());

insert into posts(id, is_active, moderation_status, text, time, title, view_count, user_id, moderator_id) values
(10, 1, 'ACCEPTED', 'Огромное количество текста Поиск1. Прямо здесь. Основное тело текста', '2020-04-15 21:48:25.0', 'Заголовок поста 1', 2, 21, 21),
(11, 1, 'DECLINED', 'Огромное количество текста Поиск12. Прямо здесь. Основное тело текста', '2020-04-14 21:48:25.0', 'Заголовок поста 28', 0, 21, 21),
(12, 1, 'NEW', 'Огромное количество текста. Поиск2 Прямо здесь. Основное тело текста', '2020-03-15 21:48:25.0', 'Заголовок поста 3', 0, 21, null),
(13, 1, 'ACCEPTED', 'Огромное количество текста. Поиск15 Прямо здесь. Основное тело текста', '2020-04-16 21:48:25.0', 'Заголовок поста 16', 4, 22, 21),
(14, 1, 'ACCEPTED', 'Огромное количество текста. Поиск6 Прямо здесь. Основное тело текста', '2020-04-12 21:48:25.0', 'Заголовок поста 17', 6, 21, 23),
(15, 1, 'ACCEPTED', 'Огромное количество текста. Прямо здесь. Основное тело текста', '2020-04-11 21:48:25.0', 'Заголовок поста Поиск1112', 0, 21, 21),
(16, 1, 'ACCEPTED', 'Огромное количество текста. Прямо здесь. Основное тело текста', '2020-04-10 21:48:25.0', 'Заголовок поста 3', 7, 21, 23),
(17, 1, 'ACCEPTED', 'Огромное количество текста. Поиск102 Прямо здесь. Основное тело текста', '2020-04-09 21:48:25.0', 'Заголовок поста 4', 8, 22, 21),
(18, 0, 'ACCEPTED', 'Огромное количество текста. Прямо здесь. Основное тело текста', '2020-04-08 21:48:25.0', 'Заголовок поста Поиск15', 10, 22, 21);

insert into post_votes(id, time, value, post_id, user_id) values
(100, '2020-04-15 22:48:25.0', 1, 10, 21),
(101, '2020-04-15 22:48:25.0', 1, 13, 21),
(102, '2020-04-14 22:48:25.0', 1, 14, 21),
(103, '2020-04-13 22:48:25.0', -1, 15, 21),
(105, '2020-04-13 22:48:25.0', 1, 11, 21),
(104, '2020-04-12 22:48:25.0', -1, 16, 21),
(106, '2020-04-15 22:49:25.0', 1, 10, 22),
(107, '2020-04-15 22:47:25.0', 1, 13, 22),
(108, '2020-04-12 22:46:25.0', -1, 16, 22);

insert into tags(id, name) values
(14, 'Habr'),
(15, 'Tag'),
(16, 'Shop'),
(17, 'Java'),
(18, 'Idea'),
(19, 'Питон');

insert into tag2post(id, post_id, tag_id) values
(10, 10, 14),
(11, 10, 15),
(12, 13, 14),
(13, 11, 15),
(14, 17, 17),
(15, 16, 16),
(16, 16, 18),
(17, 17, 18),
(18, 15, 18),
(19, 14, 19),
(20, 18, 18);

insert into post_comments(id, text, time, post_id, user_id) values
(101, 'Тело 1 комментария к посту с очень интересным содержимым', '2020-04-15 22:48:25.0', 10, 21),
(102, 'Тело 2 комментария к посту с очень интересным содержимым', '2020-04-15 22:49:25.0', 13, 21),
(103, 'Тело 3 комментария к посту с очень интересным содержимым', '2020-04-15 23:10:25.0', 13, 22),
(105, 'Тело 5 комментария к посту с очень интересным содержимым', '2020-04-15 23:25:25.0', 14, 22);

insert into post_comments(id, text, time, parent_id, user_id) values
(104, 'Тело 4 комментария к посту с очень интересным содержимым', '2020-04-15 23:20:25.0', 103, 21);