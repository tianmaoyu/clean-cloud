select count(*) from sys_user;
select count(*) from sys_user_1;
select count(*) from sys_user_2;
select count(*) from sys_user_3;
select count(*) from sys_user_4;
select count(*) from sys_user_default;

-- 根据 department_id进行分区
ALTER TABLE sys_user RENAME TO sys_userodl;

begin ;
create table public.sys_user
(
    id            bigint    default nextval('user_id_seq'::regclass) not null,
    name          varchar(30)                                        not null,
    password      varchar(100)                                       not null,
    email         varchar(50),
    department_id bigint,
    status        smallint  default 1,
    create_time   timestamp default CURRENT_TIMESTAMP,
    update_time   timestamp default CURRENT_TIMESTAMP,
    age           integer,
    user_type     varchar(16),
    sex           integer,
    create_id     bigint,
    update_id     bigint,
    constraint sys_user_pk primary key (id, department_id)
) partition by list (department_id);

comment on table public.sys_user is '用户表';

comment on column public.sys_user.id is '用户ID';

comment on column public.sys_user.name is '用户名';

comment on column public.sys_user.password is '密码';

comment on column public.sys_user.email is '邮箱';

comment on column public.sys_user.department_id is '所属部门ID';

comment on column public.sys_user.status is '状态(0停用 1正常)';

comment on column public.sys_user.create_time is '创建时间';

comment on column public.sys_user.update_time is '更新时间';

alter table public.sys_user
    owner to postgres;

create index idx_user_id_department_id
    on public.sys_user (id,department_id);

commit ;


CREATE TABLE sys_user_1 PARTITION OF sys_user FOR VALUES IN (1);
CREATE TABLE sys_user_2 PARTITION OF sys_user FOR VALUES IN (2);
CREATE TABLE sys_user_3 PARTITION OF sys_user FOR VALUES IN (3);
CREATE TABLE sys_user_default PARTITION OF sys_user DEFAULT;


insert into public.sys_user select * from public.sys_user_odl;

explain SELECT id,name,password,age,email,user_type,sex,department_id,create_id,create_time,update_id,update_time FROM sys_user WHERE id=1



-- 将数据从默认分区迁移到新分区
-- 1: 临时表 2: 分区表的特点数据迁移到临时表; 3:删除默认分区表中 特定数据; 4:创建分区表, 5:数据迁移到分区表; 6:删除临时表
begin;

-- 创建新表
CREATE TABLE sys_user_temp_4 (
                                 id            bigint    NOT NULL,
                                 name          varchar(30) NOT NULL,
                                 password      varchar(100) NOT NULL,
                                 email         varchar(50),
                                 department_id bigint,
                                 status        smallint DEFAULT 1,
                                 create_time   timestamp DEFAULT CURRENT_TIMESTAMP,
                                 update_time   timestamp DEFAULT CURRENT_TIMESTAMP,
                                 age           integer,
                                 user_type     varchar(16),
                                 sex           integer,
                                 create_id     bigint,
                                 update_id     bigint
);

-- 插入临时表
INSERT INTO sys_user_temp_4
SELECT * FROM sys_user_default WHERE department_id = 4;
-- 删除默认分区中的数据
DELETE FROM sys_user_default WHERE department_id = 4;
-- 创建分区表
CREATE TABLE sys_user_4 PARTITION OF sys_user FOR VALUES IN (4);

-- 插入新数据到分区表 从临时表
INSERT INTO sys_user_4 SELECT * FROM sys_user_temp_4;
-- 删除临时表
DROP TABLE sys_user_temp_4;

commit ;

