-- 创建数据库 (需要在psql或其他客户端中执行)

-- 部门表
CREATE TABLE department (
                            id BIGSERIAL PRIMARY KEY,
                            name VARCHAR(50) NOT NULL,
                            parent_id BIGINT NULL,
                            order_num INT DEFAULT 0,
                            status SMALLINT DEFAULT 1, -- 0停用 1正常
                            create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE department IS '部门表';
COMMENT ON COLUMN department.id IS '部门ID';
COMMENT ON COLUMN department.name IS '部门名称';
COMMENT ON COLUMN department.parent_id IS '父部门ID';
COMMENT ON COLUMN department.order_num IS '显示顺序';
COMMENT ON COLUMN department.status IS '状态(0停用 1正常)';
COMMENT ON COLUMN department.create_time IS '创建时间';
COMMENT ON COLUMN department.update_time IS '更新时间';

CREATE INDEX idx_department_parent_id ON department(parent_id);

-- 用户表
CREATE TABLE "user" (
                        id BIGSERIAL PRIMARY KEY,
                        username VARCHAR(30) NOT NULL,
                        password VARCHAR(100) NOT NULL,
                        real_name VARCHAR(50) NULL,
                        email VARCHAR(50) NULL,
                        phone VARCHAR(20) NULL,
                        avatar VARCHAR(255) NULL,
                        department_id BIGINT NULL,
                        status SMALLINT DEFAULT 1, -- 0停用 1正常
                        last_login_time TIMESTAMP NULL,
                        create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "user" IS '用户表';
COMMENT ON COLUMN "user".id IS '用户ID';
COMMENT ON COLUMN "user".username IS '用户名';
COMMENT ON COLUMN "user".password IS '密码';
COMMENT ON COLUMN "user".real_name IS '真实姓名';
COMMENT ON COLUMN "user".email IS '邮箱';
COMMENT ON COLUMN "user".phone IS '手机号';
COMMENT ON COLUMN "user".avatar IS '头像';
COMMENT ON COLUMN "user".department_id IS '所属部门ID';
COMMENT ON COLUMN "user".status IS '状态(0停用 1正常)';
COMMENT ON COLUMN "user".last_login_time IS '最后登录时间';
COMMENT ON COLUMN "user".create_time IS '创建时间';
COMMENT ON COLUMN "user".update_time IS '更新时间';

CREATE UNIQUE INDEX idx_user_username ON "user"(username);
CREATE INDEX idx_user_department_id ON "user"(department_id);

-- 角色表
CREATE TABLE role (
                      id BIGSERIAL PRIMARY KEY,
                      name VARCHAR(30) NOT NULL,
                      code VARCHAR(30) NOT NULL,
                      description VARCHAR(255) NULL,
                      order_num INT DEFAULT 0,
                      status SMALLINT DEFAULT 1, -- 0停用 1正常
                      create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE role IS '角色表';
COMMENT ON COLUMN role.id IS '角色ID';
COMMENT ON COLUMN role.name IS '角色名称';
COMMENT ON COLUMN role.code IS '角色编码';
COMMENT ON COLUMN role.description IS '描述';
COMMENT ON COLUMN role.order_num IS '显示顺序';
COMMENT ON COLUMN role.status IS '状态(0停用 1正常)';
COMMENT ON COLUMN role.create_time IS '创建时间';
COMMENT ON COLUMN role.update_time IS '更新时间';

CREATE UNIQUE INDEX idx_role_code ON role(code);

-- 资源表(菜单/按钮/API等)
CREATE TABLE resource (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(50) NOT NULL,
                          code VARCHAR(50) NOT NULL,
                          type SMALLINT NOT NULL, -- 1菜单 2按钮 3API
                          url VARCHAR(255) NULL,
                          method VARCHAR(10) NULL, -- GET/POST/PUT/DELETE等
                          parent_id BIGINT NULL,
                          icon VARCHAR(50) NULL,
                          order_num INT DEFAULT 0,
                          status SMALLINT DEFAULT 1, -- 0停用 1正常
                          create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE resource IS '资源表';
COMMENT ON COLUMN resource.id IS '资源ID';
COMMENT ON COLUMN resource.name IS '资源名称';
COMMENT ON COLUMN resource.code IS '资源编码';
COMMENT ON COLUMN resource.type IS '资源类型(1菜单 2按钮 3API)';
COMMENT ON COLUMN resource.url IS '资源路径';
COMMENT ON COLUMN resource.method IS '请求方法';
COMMENT ON COLUMN resource.parent_id IS '父资源ID';
COMMENT ON COLUMN resource.icon IS '图标';
COMMENT ON COLUMN resource.order_num IS '显示顺序';
COMMENT ON COLUMN resource.status IS '状态(0停用 1正常)';
COMMENT ON COLUMN resource.create_time IS '创建时间';
COMMENT ON COLUMN resource.update_time IS '更新时间';

CREATE UNIQUE INDEX idx_resource_code ON resource(code);
CREATE INDEX idx_resource_parent_id ON resource(parent_id);

-- 用户-角色关联表
CREATE TABLE user_role (
                           id BIGSERIAL PRIMARY KEY,
                           user_id BIGINT NOT NULL,
                           role_id BIGINT NOT NULL,
                           create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE user_role IS '用户角色关联表';
COMMENT ON COLUMN user_role.id IS 'ID';
COMMENT ON COLUMN user_role.user_id IS '用户ID';
COMMENT ON COLUMN user_role.role_id IS '角色ID';
COMMENT ON COLUMN user_role.create_time IS '创建时间';

CREATE UNIQUE INDEX idx_user_role_unique ON user_role(user_id, role_id);
CREATE INDEX idx_user_role_role_id ON user_role(role_id);

-- 角色-资源关联表
CREATE TABLE role_resource (
                               id BIGSERIAL PRIMARY KEY,
                               role_id BIGINT NOT NULL,
                               resource_id BIGINT NOT NULL,
                               create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE role_resource IS '角色资源关联表';
COMMENT ON COLUMN role_resource.id IS 'ID';
COMMENT ON COLUMN role_resource.role_id IS '角色ID';
COMMENT ON COLUMN role_resource.resource_id IS '资源ID';
COMMENT ON COLUMN role_resource.create_time IS '创建时间';

CREATE UNIQUE INDEX idx_role_resource_unique ON role_resource(role_id, resource_id);
CREATE INDEX idx_role_resource_resource_id ON role_resource(resource_id);

-- 部门-角色关联表
CREATE TABLE department_role (
                                 id BIGSERIAL PRIMARY KEY,
                                 department_id BIGINT NOT NULL,
                                 role_id BIGINT NOT NULL,
                                 create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE department_role IS '部门角色关联表';
COMMENT ON COLUMN department_role.id IS 'ID';
COMMENT ON COLUMN department_role.department_id IS '部门ID';
COMMENT ON COLUMN department_role.role_id IS '角色ID';
COMMENT ON COLUMN department_role.create_time IS '创建时间';

CREATE UNIQUE INDEX idx_department_role_unique ON department_role(department_id, role_id);
CREATE INDEX idx_department_role_role_id ON department_role(role_id);

-- 数据字典表(可选)
CREATE TABLE dict (
                      id BIGSERIAL PRIMARY KEY,
                      name VARCHAR(50) NOT NULL,
                      code VARCHAR(50) NOT NULL,
                      description VARCHAR(255) NULL,
                      status SMALLINT DEFAULT 1, -- 0停用 1正常
                      create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE dict IS '数据字典表';
COMMENT ON COLUMN dict.id IS '字典ID';
COMMENT ON COLUMN dict.name IS '字典名称';
COMMENT ON COLUMN dict.code IS '字典编码';
COMMENT ON COLUMN dict.description IS '描述';
COMMENT ON COLUMN dict.status IS '状态(0停用 1正常)';
COMMENT ON COLUMN dict.create_time IS '创建时间';
COMMENT ON COLUMN dict.update_time IS '更新时间';

CREATE UNIQUE INDEX idx_dict_code ON dict(code);

-- 字典项表(可选)
CREATE TABLE dict_item (
                           id BIGSERIAL PRIMARY KEY,
                           dict_id BIGINT NOT NULL,
                           label VARCHAR(50) NOT NULL,
                           value VARCHAR(50) NOT NULL,
                           order_num INT DEFAULT 0,
                           status SMALLINT DEFAULT 1, -- 0停用 1正常
                           create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE dict_item IS '字典项表';
COMMENT ON COLUMN dict_item.id IS '字典项ID';
COMMENT ON COLUMN dict_item.dict_id IS '字典ID';
COMMENT ON COLUMN dict_item.label IS '字典项标签';
COMMENT ON COLUMN dict_item.value IS '字典项值';
COMMENT ON COLUMN dict_item.order_num IS '排序';
COMMENT ON COLUMN dict_item.status IS '状态(0停用 1正常)';
COMMENT ON COLUMN dict_item.create_time IS '创建时间';
COMMENT ON COLUMN dict_item.update_time IS '更新时间';

CREATE INDEX idx_dict_item_dict_id ON dict_item(dict_id);

-- 操作日志表(可选)
CREATE TABLE operation_log (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NULL,
                               username VARCHAR(50) NULL,
                               operation VARCHAR(50) NULL,
                               method VARCHAR(200) NULL,
                               params TEXT NULL,
                               time BIGINT NULL, -- 执行时长(毫秒)
                               ip VARCHAR(50) NULL,
                               status SMALLINT NULL, -- 0失败 1成功
                               error_msg TEXT NULL,
                               create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE operation_log IS '操作日志表';
COMMENT ON COLUMN operation_log.id IS '日志ID';
COMMENT ON COLUMN operation_log.user_id IS '用户ID';
COMMENT ON COLUMN operation_log.username IS '用户名';
COMMENT ON COLUMN operation_log.operation IS '操作';
COMMENT ON COLUMN operation_log.method IS '方法名';
COMMENT ON COLUMN operation_log.params IS '参数';
COMMENT ON COLUMN operation_log.time IS '执行时长(毫秒)';
COMMENT ON COLUMN operation_log.ip IS 'IP地址';
COMMENT ON COLUMN operation_log.status IS '状态(0失败 1成功)';
COMMENT ON COLUMN operation_log.error_msg IS '错误消息';
COMMENT ON COLUMN operation_log.create_time IS '创建时间';

CREATE INDEX idx_operation_log_user_id ON operation_log(user_id);
CREATE INDEX idx_operation_log_create_time ON operation_log(create_time);