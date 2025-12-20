# FAST-IO

对象存储系统

| http状态码 | 解释         |
|---------|------------|
| 200     | OK         |
| 201     | PUT成功      |
| 404     | object不存在  |
| 409     | 冲突（锁/状态冲突） |
| 500     | 其他         |

| 方法     | path                                                 |
|--------|------------------------------------------------------|
| PUT    | /data/{bucket}/{objectKey}                           |
| GET    | /data/{bucket}/{objectKey}                           |
| HEAD   | /data/{bucket}/{objectKey}                           |
| DELETE | /data/{bucket}/{objectKey}                           |
| POST   | /data/{bucket}/{objectKey}?uploads                   |
| POST   | /data/{bucket}/{objectKey}?uploadId=xxx&partNumber=1 |
| POST   | /data/{bucket}/{objectKey}?uploadId=xxx              |

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