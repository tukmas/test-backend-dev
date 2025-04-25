create table author
(
    id     serial primary key,
    fio    varchar(255)  not null,
    creation_time  timestamp  not null
);