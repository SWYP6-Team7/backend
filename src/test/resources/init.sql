create table categories
(
    community_category_number int auto_increment
        primary key,
    community_category_name   varchar(255) not null
);

insert into categories values (1, "전체");
insert into categories values (2, "잡담");
insert into categories values (3, "여행팁");
insert into categories values (4, "후기");

