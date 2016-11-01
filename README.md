
# swamm
## JAVA API 接口生成工具

swamm 是根据是使javadoc 中的doclet api 获取源码的方法入参，返回结果 ，解样出接口 

## 使用方法
使用方式
  
```
    swamm.sh class=CommentApiService:projectId=10  src/main/java com.***.***.client
```

如下接品定义中
```
    /**
     * 新增用户接口，这里是接口的描述
     * @title 新增用户
     * @param user
     * @param user name 用户名称，这里优先级最高，如果填写了用户的子属性，将不再解析User类中的属性
     */
    void insert(User user);
```

* title：为接口名称，如果没填写，默认使用注释的文本，就是 "新增用户接口，这里是接口的描述"
* param: 为参数定义，可以填写参数的描述， 如果user 且没有在@param 中定义子类型， 就会解析User对象的属性， 如果 @ignore 标签，这个属性不会被解析

```
    /**
     * 用户名称
     */
    private String            name;
    
    /**
     * 用户密码
     */
    private String            password;
    
    private String            phone;
    
    private String            address;
    
    private String            sex;
    
    /**
     * @ignore
     */
    private Date              gmtCreate;
    /**
     * @ignore
     */
    private Date              gmtModify;

```

* @return 方法返回定义， 这个方法返回参数是Pagination<UserDto>， 返回参数会按照泛型解析返回结束，解析方式同入参， 后面会支持 @return 标签中定义子属性，还在设想中。。。

```
    /**
     * 分页查询用户
     *
     * @title 用户列表接口
     * @param query 查询参数对象
     * @param query @name hello
     * @param query @currentPage
     * @param query @pageSize
     * @return ret
     */
    Pagination<UserDto> listUserPage(UserQuery query);
    
    
    public class Pagination<T> {
    
        /**
         * 当前页，从1开始
         */
        private int                     currentPage = 1;
    
        /**
         * 每页大小
         */
        private int                     pageSize    = 10;
    
        /**
         * results
         */
        private List<T>                 data;
    
        private String                  result;
    
        private long                    totalCount;
        
        // 后面省略。。。。。。。。
     }
```


# 解析结果示例：

```
[
  {
    "desc": "",
    "methodModels": [
      {
        "desc": "",
        "name": "createClassify",
        "paramModels": [
          {
            "desc": "classifyDto",
            "innerField": [
              {
                "desc": "type",
                "name": "type",
                "typeName": "java.lang.String"
              },
              {
                "desc": "name",
                "name": "name",
                "typeName": "java.lang.String"
              },
              {
                "desc": "level",
                "name": "level",
                "typeName": "java.lang.Long"
              },
              {
                "desc": "sort",
                "name": "sort",
                "typeName": "java.lang.Long"
              },
              {
                "desc": "parentId",
                "name": "parentId",
                "typeName": "java.lang.Long"
              },
              {
                "desc": "isView",
                "name": "isView",
                "typeName": "java.lang.String"
              },
              {
                "desc": "children",
                "name": "children",
                "typeName": "java.util.List"
              },
              {
                "desc": "firstName",
                "name": "firstName",
                "typeName": "java.lang.String"
              },
              {
                "desc": "firstId",
                "name": "firstId",
                "typeName": "java.lang.String"
              },
              {
                "desc": "firstParentId",
                "name": "firstParentId",
                "typeName": "java.lang.String"
              },
              {
                "desc": "secondName",
                "name": "secondName",
                "typeName": "java.lang.String"
              },
              {
                "desc": "secondId",
                "name": "secondId",
                "typeName": "java.lang.String"
              },
              {
                "desc": "secondParentId",
                "name": "secondParentId",
                "typeName": "java.lang.String"
              },
              {
                "desc": "thirdName",
                "name": "thirdName",
                "typeName": "java.lang.String"
              },
              {
                "desc": "thirdId",
                "name": "thirdId",
                "typeName": "java.lang.String"
              },
              {
                "desc": "thirdParentId",
                "name": "thirdParentId",
                "typeName": "java.lang.String"
              },
              {
                "desc": "Id",
                "name": "Id",
                "typeName": "java.lang.Long"
              },
              {
                "desc": "gmtCreate",
                "name": "gmtCreate",
                "typeName": "java.util.Date"
              },
              {
                "desc": "gmtModified",
                "name": "gmtModified",
                "typeName": "java.util.Date"
              },
              {
                "desc": "isDeleted",
                "name": "isDeleted",
                "typeName": "java.lang.String"
              },
              {
                "desc": "creator",
                "name": "creator",
                "typeName": "java.lang.String"
              },
              {
                "desc": "modifier",
                "name": "modifier",
                "typeName": "java.lang.String"
              }
            ],
            "name": "classifyDto",
            "typeName": "com.xxx.pangu.client.dto.ClassifyDto"
          },
          {
            "desc": "pvg",
            "innerField": [
              {
                "desc": "language",
                "name": "language",
                "typeName": "java.lang.String"
              },
              {
                "desc": "userId",
                "name": "userId",
                "typeName": "java.lang.String"
              },
              {
                "desc": "authToken",
                "name": "authToken",
                "typeName": "java.lang.String"
              },
              {
                "desc": "siteCode",
                "name": "siteCode",
                "typeName": "java.lang.String"
              }
            ],
            "name": "pvg",
            "typeName": "com.xxx.pangu.client.dto.PvgDto"
          }
        ],
        "returnModel": {
          "desc": "",
          "innerField": [
            {
              "desc": "success",
              "name": "success",
              "typeName": "boolean"
            },
            {
              "desc": "result",
              "innerField": [
                {
                  "desc": "type",
                  "name": "type",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "name",
                  "name": "name",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "level",
                  "name": "level",
                  "typeName": "java.lang.Long"
                },
                {
                  "desc": "sort",
                  "name": "sort",
                  "typeName": "java.lang.Long"
                },
                {
                  "desc": "parentId",
                  "name": "parentId",
                  "typeName": "java.lang.Long"
                },
                {
                  "desc": "isView",
                  "name": "isView",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "children",
                  "innerField": [
                    {
                      "desc": "type",
                      "name": "type",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "name",
                      "name": "name",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "level",
                      "name": "level",
                      "typeName": "java.lang.Long"
                    },
                    {
                      "desc": "sort",
                      "name": "sort",
                      "typeName": "java.lang.Long"
                    },
                    {
                      "desc": "parentId",
                      "name": "parentId",
                      "typeName": "java.lang.Long"
                    },
                    {
                      "desc": "isView",
                      "name": "isView",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "children",
                      "name": "children",
                      "typeName": "java.util.List"
                    },
                    {
                      "desc": "firstName",
                      "name": "firstName",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "firstId",
                      "name": "firstId",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "firstParentId",
                      "name": "firstParentId",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "secondName",
                      "name": "secondName",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "secondId",
                      "name": "secondId",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "secondParentId",
                      "name": "secondParentId",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "thirdName",
                      "name": "thirdName",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "thirdId",
                      "name": "thirdId",
                      "typeName": "java.lang.String"
                    },
                    {
                      "desc": "thirdParentId",
                      "name": "thirdParentId",
                      "typeName": "java.lang.String"
                    }
                  ],
                  "name": "children",
                  "typeName": "java.util.List"
                },
                {
                  "desc": "firstName",
                  "name": "firstName",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "firstId",
                  "name": "firstId",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "firstParentId",
                  "name": "firstParentId",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "secondName",
                  "name": "secondName",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "secondId",
                  "name": "secondId",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "secondParentId",
                  "name": "secondParentId",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "thirdName",
                  "name": "thirdName",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "thirdId",
                  "name": "thirdId",
                  "typeName": "java.lang.String"
                },
                {
                  "desc": "thirdParentId",
                  "name": "thirdParentId",
                  "typeName": "java.lang.String"
                }
              ],
              "name": "result",
              "typeName": "com.xxx.pangu.client.dto.ClassifyDto"
            },
            {
              "desc": "errCode",
              "name": "errCode",
              "typeName": "java.lang.String"
            },
            {
              "desc": "errMsg",
              "name": "errMsg",
              "typeName": "java.lang.String"
            }
          ],
          "typeName": "com.xxx.common.dto.Result"
        },
        "title": "createClassify"
      }
]

```
