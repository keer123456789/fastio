# FAST-IO

对象存储系统

| http状态码 | 解释         |
|---------|------------|
| 200     | OK         |
| 201     | PUT成功      |
| 404     | object不存在  |
| 409     | 冲突（锁/状态冲突） |
| 500     | 其他         |

| 方法     | path                                                       | 说明      |
|--------|------------------------------------------------------------|---------|
| PUT    | /data/{bucket}/{objectKey}                                 | 上传对象    |
| GET    | /data/{bucket}/{objectKey}                                 | 获取对象    |
| HEAD   | /data/{bucket}/{objectKey}                                 | 获取对象元信息 |
| DELETE | /data/{bucket}/{objectKey}                                 | 删除对象    |
| POST   | /data/multi/{bucket}/{objectKey}                           | 创建分片任务  |
| PUT    | /data/multi/{bucket}/{objectKey}?uploadId=xxx&partNumber=1 | 上传分片    |
| POST   | /data/multi/{bucket}/{objectKey}?uploadId=xxx              | 合并分片    |
| DELETE | /data/multi/{bucket}/{objectKey}?uploadId=xxx              | 删除分片任务  |

| 方法     | path                                       |
|--------|--------------------------------------------|
| PUT    | /admin/buckets/{bucket}                    |
| DELETE | /admin/buckets/{bucket}                    |
| GET    | /admin/buckets                             |
| HEAD   | /admin/buckets/{bucket}                    |
| GET    | /admin/buckets/{bucket}/stat               |
| GET    | /admin/buckets/{bucket}/objects?prefix=a/b |

TODO
rockdb 深入用法
日志打印
配置