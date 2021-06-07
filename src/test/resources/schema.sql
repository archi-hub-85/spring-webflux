create table AUTHORS (
    ID integer not null AUTO_INCREMENT,
    NAME varchar(255) not null,
    primary key (ID)
);

create table BOOKS (
    ID integer not null AUTO_INCREMENT,
    TITLE varchar(255) not null,
    YEAR integer not null,
    AUTHOR_ID integer not null,
    FILENAME varchar(255),
    MIMETYPE varchar(255),
    CONTENT blob,
    primary key (ID),
    foreign key (AUTHOR_ID) references AUTHORS(ID)
);
