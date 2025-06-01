# 创建自定义网络（方便容器间通信）
docker network create xxl-network

# 启动 MySQL 5.7
docker run -d \
--name mysql \
--network xxl-network \
-e MYSQL_ROOT_PASSWORD=123456 \
-e MYSQL_DATABASE=xxl_job \
-p 3306:3306 \
mysql:5.7

# 等待几秒让 MySQL 初始化完成
sleep 10

# 启动 xxl-job-admin 并连接 MySQL 5.7
docker run -d \
--name xxl-job-admin \
--network xxl-network \
-p 8088:8080 \
-e PARAMS="--spring.datasource.url=jdbc:mysql://mysql:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8 --spring.datasource.username=root --spring.datasource.password=123456 --spring.datasource.driver-class-name=com.mysql.jdbc.Driver" \
-v /usr/local/xxl-job/admin/logs:/data/applogs \
xuxueli/xxl-job-admin:2.3.0