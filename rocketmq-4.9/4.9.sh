docker pull apache/rocketmq:4.9.6
docker pull apacherocketmq/rocketmq-dashboard:latest

docker network create rocketmq-net

docker run -d \
--name rmqnamesrv \
--net rocketmq-net \
-p 9876:9876 \
apache/rocketmq:4.9.6 sh mqnamesrv

#echo "brokerIP1=127.0.0.1" > broker.conf

docker run -d \
--name rmqbroker \
--net rocketmq-net \
-p 10912:10912 \
-p 10911:10911 \
-p 10909:10909 \
-e "NAMESRV_ADDR=rmqnamesrv:9876" \
-e "JAVA_OPT_EXT=-server -Xms128m -Xmx128m" \
-v $(pwd)/broker.conf:/home/rocketmq/rocketmq-4.9.6/conf/broker.conf \
apache/rocketmq:4.9.6 sh mqbroker \
-c /home/rocketmq/rocketmq-4.9.6/conf/broker.conf

docker run -d \
--name rocketmq-dashboard \
--net rocketmq-net \
-p 8080:8080 \
-e "JAVA_OPTS=-Drocketmq.namesrv.addr=rmqnamesrv:9876 -Dserver.port=8080" \
apacherocketmq/rocketmq-dashboard:latest