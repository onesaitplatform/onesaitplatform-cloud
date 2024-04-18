create table MASTER_USER (USER_ID varchar(50) not null, CREATED_AT datetime not null, UPDATED_AT datetime not null, ACTIVE BIT not null, EMAIL varchar(255) not null, EXTRA_FIELDS longtext, FULL_NAME varchar(255) not null, PASSWORD varchar(128) not null, primary key (USER_ID)) ENGINE=InnoDB
create table MASTER_USER_TOKEN (ID varchar(50) not null, CREATED_AT datetime not null, UPDATED_AT datetime not null, TOKEN varchar(255) not null, MASTER_USER_ID varchar(50) not null, TENANT_ID varchar(50) not null, primary key (ID)) ENGINE=InnoDB
create table TENANT (ID varchar(50) not null, CREATED_AT datetime not null, UPDATED_AT datetime not null, NAME varchar(50) not null, VERTICAL_ID varchar(50) not null, primary key (ID)) ENGINE=InnoDB
create table TENANT_USERS (TENANT_ID varchar(50) not null, MASTER_USER_ID varchar(50) not null, primary key (MASTER_USER_ID, TENANT_ID)) ENGINE=InnoDB
create table VERTICAL (ID varchar(50) not null, CREATED_AT datetime not null, UPDATED_AT datetime not null, NAME varchar(50) not null, SCHEMA_DB varchar(50) not null, primary key (ID)) ENGINE=InnoDB
alter table TENANT add constraint UK_62q1hqoajy15pceaikrv8fros unique (NAME)
alter table VERTICAL add constraint UK_k1i8c185keaageewa1l0mmnn unique (NAME)
alter table VERTICAL add constraint UK_459trmgh9c02dxbweduf76qkr unique (SCHEMA_DB)
alter table MASTER_USER_TOKEN add constraint FK34x4rsql8pdtuyqvn4dmoi7cd foreign key (MASTER_USER_ID) references MASTER_USER (USER_ID) on delete cascade
alter table MASTER_USER_TOKEN add constraint FKbtxq46w877bxsh0v9hki6dyji foreign key (TENANT_ID) references TENANT (ID) on delete cascade
alter table TENANT add constraint FKp9wf76hrih9d9i8x6vnvlr3bk foreign key (VERTICAL_ID) references VERTICAL (ID)
alter table TENANT_USERS add constraint FKhe2o3dj9at8grenn4dyygud1u foreign key (MASTER_USER_ID) references MASTER_USER (USER_ID)
alter table TENANT_USERS add constraint FKlbyeohkv7mc3npoirkqvkqcsl foreign key (TENANT_ID) references TENANT (ID)
