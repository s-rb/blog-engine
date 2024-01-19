create table captcha_codes
(
    id          integer  not null,
    code        TINYTEXT not null,
    secret_code TINYTEXT not null,
    time        DATETIME not null,
    primary key (id)
) engine = InnoDB;

create table global_settings
(
    id    integer      not null,
    code  VARCHAR(255) not null,
    name  VARCHAR(255) not null,
    value VARCHAR(255) not null,
    primary key (id)
) engine = InnoDB;

create table hibernate_sequence
(
    next_val bigint
) engine = InnoDB;

insert into hibernate_sequence
values (1);

create table post_comments
(
    id        integer  not null,
    text      TEXT     not null,
    time      DATETIME not null,
    parent_id integer,
    post_id   integer,
    user_id   integer,
    primary key (id)
) engine = InnoDB;

create table post_votes
(
    id      integer  not null,
    time    DATETIME not null,
    value   tinyint  not null,
    post_id integer,
    user_id integer,
    primary key (id)
) engine = InnoDB;

create table posts
(
    id                integer      not null,
    is_active         bit          not null,
    moderation_status varchar(255) not null,
    moderator_id      integer,
    text              TEXT         not null,
    time              DATETIME     not null,
    title             VARCHAR(255) not null,
    view_count        integer      not null,
    user_id           integer      not null,
    primary key (id)
) engine = InnoDB;

create table tag2post
(
    id      integer not null,
    post_id integer not null,
    tag_id  integer not null,
    primary key (id)
) engine = InnoDB;

create table tags
(
    id   integer      not null,
    name VARCHAR(255) not null,
    primary key (id)
) engine = InnoDB;

create table users
(
    id           integer      not null,
    code         VARCHAR(255),
    email        VARCHAR(255) not null,
    password     VARCHAR(255) not null,
    is_moderator bit          not null,
    name         VARCHAR(255) not null,
    photo        TEXT,
    reg_time     DATETIME     not null,
    primary key (id)
) engine = InnoDB;

alter table users
    add constraint UK_6dotkott2kjsp8vw4d0m25fb7 unique (email);
alter table users
    add constraint UK_3g1j96g94xpk3lpxl2qbl985x unique (name);
alter table post_comments
    add constraint FKc3b7s6wypcsvua2ycn4o1lv2c foreign key (parent_id) references post_comments (id);
alter table post_comments
    add constraint FKaawaqxjs3br8dw5v90w7uu514 foreign key (post_id) references posts (id);
alter table post_comments
    add constraint FKsnxoecngu89u3fh4wdrgf0f2g foreign key (user_id) references users (id);
alter table post_votes
    add constraint FK9jh5u17tmu1g7xnlxa77ilo3u foreign key (post_id) references posts (id);
alter table post_votes
    add constraint FK9q09ho9p8fmo6rcysnci8rocc foreign key (user_id) references users (id);
alter table posts
    add constraint FK5lidm6cqbc7u4xhqpxm898qme foreign key (user_id) references users (id);
alter table posts
    add constraint FK6m7nr3iwh1auer2hk7rd05riw foreign key (moderator_id) references users (id);
alter table tag2post
    add constraint FKpjoedhh4h917xf25el3odq20i foreign key (post_id) references posts (id);
alter table tag2post
    add constraint FKjou6suf2w810t2u3l96uasw3r foreign key (tag_id) references tags (id);