DELETE FROM `users`;

INSERT INTO users(id, email, password, is_moderator, name, reg_time)
values (21, 'mail@mail.ru', '5F4DCC3B5AA765D61D8327DEB882CF99', 1, 'mail', NOW()),
(22, 'new@mail.ru', '5F4DCC3B5AA765D61D8327DEB882CF99', 0, 'new', NOW());

INSERT INTO users(id, code, email, password, is_moderator, name, reg_time)
values (23, 'restorecode', 'restoreuser@mail.ru', 'restoreuserpassword', 1, 'restoreuser', NOW());