
# swamm
## JAVA API 接口生成工具

swamm 是根据是使javadoc 中的doclet api 获取源码的方法入参，返回结果 ，解样出接口 

## 使用方法
使用方式
  
```
    swamm.sh class=UserApiService(接口名，可以带号分隔) src/main/java(源码位置) com.xxx.xxx.client(包名)
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