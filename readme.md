
mvn surefire-report:report


 clean-support 
  1: 不想夸事务, 不想夸服务事务, 要在一个业务事务中处理;
  2: 业务无关的 log, 打印, 监控 有数据库记录 各个服务都要使用的,又不想使用 openfegin
 


表- 纬度表,实时表:
 1: 纬度表:有状体,有更改,curd; 6字段,可能还有 deleted; version
 2: 实时表:只记录当前瞬时状态,只有 cr; 不会更改; 如 日志,流水,操作记录; 可以多冗余子段;
表- 结构化,半结构化,动态
 1. 结构化: 
 2. 半结构化, 动态部分使用json,动态扩展

接口- 业务接口, "数据":
 1: 数据接口: 只是纯粹的 crud page 单表下; 可以使用模版代码进行生成; 方法名称统一名命
 2: 业务接口-写: 逻辑处理,多个表下关系处理; 行业名称命名,代码 4段式写;
 3: 业务接口-读: 多个表关联-查询统计报表/一次或者多次查询返回数据,视图

义务接口和数据库接口的-界线
  如:xx_head, xx_item 两个表,item 会依赖head 而存在,创建一个 item 必有有个 head;即可以是数据,也可以是业务接口

jar包- 工具-业务
 1: 纯工具: 基本不写数据库
 2: 为业务而服务-support-要写数据库的功能, 如: api日志,系统本身一些信息
 2: 纯业务
 
jar包- api, service,app
  app: 是独立运行的 : 比 service 多了一个 对外接口层: consumer,controller,scheduler
  api: 其他 service模块 调用的 :  fegin,enums,param,entity,vo,msg
  service: 纯业务其他app 调用 : service,mapper,converter,config

jar包对接方向- 对外提供,外部其提供
  open-app/openClient: 对外app,统一接口入口
  third-service/thirdClient: 第三方系统对接

povo:
  entity: 表一一一对应
  vo: 输出 优先使用entity,不适合在使用vo
  param: 输入 优先使用entity,不适合时在用param
  dto: [建议少用]义务接口-写, entity/vo/param 都不适合则用dto
  view: 义务接口-读, 只是为了提醒开发者,这个是一个 数据库视图 实体的一一映射
  msg: 消息
  
