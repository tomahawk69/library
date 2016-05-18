select "Version is";
PRAGMA user_version;
create table files (
         f_uuid varchar(36) primary key not null,
         f_file_path varchar(1024) not null,
         f_file_name varchar(255) not null,
         f_file_size integer null,
         f_file_date datetime null,
         f_file_md5 varchar(32) null,
         f_last_updated datetime null
);
create index files_f_file_path on files (f_file_path);
create table meta (
         f_name varchar(255) null,
         f_last_updated datetime null,
         f_last_refreshed datetime null
);
insert into meta values (null, null, null);
PRAGMA user_version = 1;
