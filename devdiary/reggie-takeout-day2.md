#### job_1 登录功能完善

> 用户请求数据时，对用户的登录状态进行校验，若用户未登录，则返回登录页面，使用拦截器实现该功能

##### 添加拦截器

```java
package com.hippo.reggietakeout.interceptor;

import com.alibaba.fastjson.JSON;
import com.hippo.reggietakeout.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 判断是否已登录(session的employee属性是否为空)
         * 否，将处理结果写入response，返回false
         */
        if(request.getSession().getAttribute("employee") == null){
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
            return false;
        }
        return true;
    }
}
```

##### 配置注册拦截器

> 登录资源不拦截，静态资源不拦截

```java
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor()).addPathPatterns("/**").excludePathPatterns(
                "/employee/login",
                "/employee/logout",
            	"/backend/**",
                "/front/**"
        );
        log.info("登录状态拦截器添加完成...");
    }
```

#### job_2 新建员工

##### 新建员工功能实现

> 需补齐创建人，更新人，创建时间，更新时间等字段信息，并设置初始密码（md5加密）

```JAVA
    @PostMapping
    public R<String> register(HttpServletRequest request, @RequestBody Employee questEmployee){

        // 补齐表字段
        questEmployee.setCreateTime(LocalDateTime.now());
        questEmployee.setUpdateTime(LocalDateTime.now());
        questEmployee.setCreateUser((long)request.getSession().getAttribute("employee"));
        questEmployee.setUpdateUser((long)request.getSession().getAttribute("employee"));
        questEmployee.setPassword(DigestUtils.md5DigestAsHex(DEFAULT_PASSWORD.getBytes()));
        employeeMapper.insert(questEmployee);
        return R.success("created user "+ questEmployee.getUsername());
    }
```

##### 全局异常处理

> @ControllerAdvice主要用来处理全局数据，一般搭配@ExceptionHandler、@ModelAttribute、@InitBinder使用
>
> @ExceptionHandler：处理全局异常
>
> @ModelAttribute：预设全局变量
>
> @InitBinder：请求参数预处理

```java
package com.hippo.reggietakeout.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> sqlExceptionHandler(SQLIntegrityConstraintViolationException ex){
        String errorMsg = ex.getMessage();
        if(errorMsg.contains("Duplicate entry")){
            String[] ss = errorMsg.split(" ");
            return R.error(ss[2] + " already exist");
        }

        return R.error("unknown sql error");
    }
}
```

#### job_3 员工信息列表分页查询

##### MybatisPlus分页插件配置

```java
package com.hippo.reggietakeout.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        log.info("分页拦截器配置完成");
        return mybatisPlusInterceptor;
    }
}
```

##### 员工分页查询接口实现

> - 创建分页构造器
> - *创建查询条件
> - 调用employMapper的selectPage，入参：
>   - 分页构造器
>   - 查询条件构造器
>   - selectPage自动使用查询到的数据包装传入的分页构造器
> - 返回已分页构造器作为data的R对象

```java
    @GetMapping("page")
    public R<Page> page(int page, int pageSize, String name){

        // 分页构造器
        Page pageInfo = new Page(page, pageSize);

        // 条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();

        // 添加查询条件
        queryWrapper.like(!StringUtils.isEmpty(name), Employee::getName, name);

        // 按更新时间排序
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        employeeMapper.selectPage(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }
```

#### job_4 员工启用禁用

##### 更新用户接口功能实现

```java
    @PutMapping
    public R<String> changeStatus(HttpServletRequest request, @RequestBody Employee questEmployee){
        // 设置更新人，更新时间
        questEmployee.setUpdateTime(LocalDateTime.now());
        questEmployee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        int i = employeeMapper.updateById(questEmployee);
        return R.success("succeed");
    }
```

##### 消息转换器

> 将控制器返回的long型数据转换为字符串类型，解决前端js处理long型数据精度丢失问题（主要）
>
> 其他，时间数据类型转换为固定格式字符串等

###### 消息转换器实现

```java
package com.hippo.reggietakeout.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * 对象映射器:基于jackson将Java对象转为json，或者将json转为Java对象
 * 将JSON解析为Java对象的过程称为 [从JSON反序列化Java对象]
 * 从Java对象生成JSON的过程称为 [序列化Java对象到JSON]
 */
public class JacksonObjectMapper extends ObjectMapper {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public JacksonObjectMapper() {
        super();
        //收到未知属性时不报异常
        this.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

        //反序列化时，属性不存在的兼容处理
        this.getDeserializationConfig().withoutFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);


        SimpleModule simpleModule = new SimpleModule()
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)))

                .addSerializer(BigInteger.class, ToStringSerializer.instance)
                .addSerializer(Long.class, ToStringSerializer.instance)
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        //注册功能模块 例如，可以添加自定义序列化器和反序列化器
        this.registerModule(simpleModule);
    }
}
```

###### 消息转换器配置生效

>在WebMvcConfig中重写extendMessageConverters进行如下配置

```java
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        // 设置对象转换器
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        // 将自定义的转换器对象添加的原转换器列表中
        converters.add(0, messageConverter);
//        super.extendMessageConverters(converters);
    }
```

#### job_5 员工编辑功能

##### 按id查询用户信息

> 从get请求路由中获取用户id
>
> 使用{变量}替换需要引用的路径中的变量
>
> @PathVariable(变量名)指定方法参数引用的变量

```java
    @GetMapping("/{id}")
    public R<Employee> findById(@PathVariable("id") Long id){
        Employee employee = employeeMapper.selectById(id);
        if(employee != null){
            return R.success(employee);
        }else{
            return R.error("user not exist");
        }
    }
```

