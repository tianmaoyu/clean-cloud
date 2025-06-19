-- 初始化部门数据
INSERT INTO department (id, name, parent_id, order_num, status) VALUES
                                                                    (1, '总公司', NULL, 0, 1),
                                                                    (2, '技术部', 1, 1, 1),
                                                                    (3, '市场部', 1, 2, 1),
                                                                    (4, '研发中心', 2, 1, 1),
                                                                    (5, '测试部', 2, 2, 1);

-- 重置序列(确保后续插入的自增ID正确)
SELECT setval('department_id_seq', (SELECT MAX(id) FROM department));

-- 初始化角色数据
INSERT INTO role (id, name, code, description, order_num, status) VALUES
                                                                      (1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限', 1, 1),
                                                                      (2, '部门管理员', 'DEPT_ADMIN', '管理部门内用户和权限', 2, 1),
                                                                      (3, '普通用户', 'USER', '普通用户权限', 3, 1),
                                                                      (4, '访客', 'GUEST', '只读权限', 4, 1);

SELECT setval('role_id_seq', (SELECT MAX(id) FROM role));

-- 初始化用户数据(密码为123456的加密结果)
INSERT INTO "user" (id, username, password, real_name, email, phone, department_id, status) VALUES
                                                                                                (1, 'admin', '$2a$10$Egp1/gvFlt7zhlXVfEFl4Ou3oZQ6/5R2T3YIVR3WYVvxkYb5GpF.y', '系统管理员', 'admin@example.com', '13800138000', 1, 1),
                                                                                                (2, 'tech_leader', '$2a$10$Egp1/gvFlt7zhlXVfEFl4Ou3oZQ6/5R2T3YIVR3WYVvxkYb5GpF.y', '技术总监', 'tech@example.com', '13800138001', 2, 1),
                                                                                                (3, 'dev1', '$2a$10$Egp1/gvFlt7zhlXVfEFl4Ou3oZQ6/5R2T3YIVR3WYVvxkYb5GpF.y', '开发人员1', 'dev1@example.com', '13800138002', 4, 1),
                                                                                                (4, 'tester1', '$2a$10$Egp1/gvFlt7zhlXVfEFl4Ou3oZQ6/5R2T3YIVR3WYVvxkYb5GpF.y', '测试人员1', 'tester1@example.com', '13800138003', 5, 1);

SELECT setval('user_id_seq', (SELECT MAX(id) FROM "user"));

-- 初始化用户角色关联
INSERT INTO user_role (user_id, role_id) VALUES
                                             (1, 1),
                                             (2, 2),
                                             (3, 3),
                                             (4, 3);

-- 初始化资源数据
INSERT INTO resource (id, name, code, type, url, method, parent_id, order_num, status) VALUES
-- 一级菜单
(1, '系统管理', 'system', 1, '/system', NULL, NULL, 1, 1),
(2, '用户管理', 'user', 1, '/system/user', NULL, 1, 1, 1),
(3, '角色管理', 'role', 1, '/system/role', NULL, 1, 2, 1),
(4, '部门管理', 'dept', 1, '/system/dept', NULL, 1, 3, 1),
(5, '资源管理', 'resource', 1, '/system/resource', NULL, 1, 4, 1),
(6, '仪表盘', 'dashboard', 1, '/dashboard', NULL, NULL, 0, 1),
(7, '个人中心', 'profile', 1, '/profile', NULL, NULL, 5, 1),

-- 用户管理下的按钮
(8, '用户查询', 'user:query', 2, NULL, 'GET', 2, 1, 1),
(9, '用户新增', 'user:add', 2, NULL, 'POST', 2, 2, 1),
(10, '用户修改', 'user:edit', 2, NULL, 'PUT', 2, 3, 1),
(11, '用户删除', 'user:delete', 2, NULL, 'DELETE', 2, 4, 1),

-- API接口
(12, '获取用户列表API', 'api:user:list', 3, '/api/users', 'GET', NULL, 0, 1),
(13, '添加用户API', 'api:user:add', 3, '/api/users', 'POST', NULL, 0, 1),
(14, '更新用户API', 'api:user:update', 3, '/api/users/*', 'PUT', NULL, 0, 1),
(15, '删除用户API', 'api:user:delete', 3, '/api/users/*', 'DELETE', NULL, 0, 1);

SELECT setval('resource_id_seq', (SELECT MAX(id) FROM resource));

-- 初始化角色资源关联
INSERT INTO role_resource (role_id, resource_id) VALUES
-- 超级管理员拥有所有权限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7),
(1, 8), (1, 9), (1, 10), (1, 11),
(1, 12), (1, 13), (1, 14), (1, 15),

-- 部门管理员拥有部分权限
(2, 6), (2, 7),
(2, 8), (2, 12),

-- 普通用户拥有基本权限
(3, 6), (3, 7),

-- 访客只有仪表盘权限
(4, 6);

-- 初始化部门角色关联
INSERT INTO department_role (department_id, role_id) VALUES
-- 技术部拥有部门管理员角色
(2, 2),
-- 研发中心和测试部有普通用户角色
(4, 3),
(5, 3);

-- 初始化字典数据(可选)
INSERT INTO dict (id, name, code, description) VALUES
                                                   (1, '用户状态', 'user_status', '用户账号状态'),
                                                   (2, '性别', 'gender', '用户性别');

SELECT setval('dict_id_seq', (SELECT MAX(id) FROM dict));

-- 初始化字典项数据(可选)
INSERT INTO dict_item (dict_id, label, value, order_num) VALUES
                                                             (1, '正常', '1', 1),
                                                             (1, '停用', '0', 2),
                                                             (2, '男', '1', 1),
                                                             (2, '女', '2', 2),
                                                             (2, '未知', '0', 3);

SELECT setval('dict_item_id_seq', (SELECT MAX(id) FROM dict_item));


CREATE TABLE products (
                          id SERIAL PRIMARY KEY,
                          name TEXT NOT NULL,
                          details JSONB
);