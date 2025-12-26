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
| PUT    | /data/object/{bucket}/{objectKey}                          | 上传对象    |
| GET    | /data/object/{bucket}/{objectKey}                          | 获取对象    |
| HEAD   | /data/object/{bucket}/{objectKey}                          | 获取对象元信息 |
| DELETE | /data/object/{bucket}/{objectKey}                          | 删除对象    |
| POST   | /data/multi/{bucket}/{objectKey}                           | 创建分片任务  |
| PUT    | /data/multi/{bucket}/{objectKey}?uploadId=xxx&partNumber=1 | 上传分片    |
| POST   | /data/multi/{bucket}/{objectKey}?uploadId=xxx              | 合并分片    |
| DELETE | /data/multi/{bucket}/{objectKey}?uploadId=xxx              | 删除分片任务  |

| 方法     | path                                       | 说明          |
|--------|--------------------------------------------|-------------|
| PUT    | /admin/buckets/{bucket}                    | 创建桶         |
| DELETE | /admin/buckets/{bucket}                    | 删除桶         |
| GET    | /admin/buckets                             | 获取桶列表       |
| GET    | /admin/buckets/{bucket}/stat               | 获取其中一个桶的元信息 |
| GET    | /admin/buckets/{bucket}/objects?prefix=a/b | 获取桶中的对象元信息  |

TODO
rockdb 深入用法
日志打印
配置