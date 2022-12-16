#### job_1 公共字段自动填充

>在crud操作时，某些表的存在一些公共的字段操作，如插入和修改表数据时涉及的创建人，创建时间，更新人，更新时间等。若能对这些字段自动填充，可减少重复工作。
>
>使用MybatisPlus提供的公共字段填充功能，可解决上述问题。

##### 实体对象@TableField注解

```java
    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) // 更新和插入时自动填充
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
```

##### 实现MetaObjectHandler接口

> 实现MetaObjectHandler的insertFill和updateFill方法

```java
package com.hippo.reggietakeout.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", new Long(1));
        metaObject.setValue("updateUser", new Long(1));
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", new Long(1));
    }
}
```

##### 应用线程传递当前用户id参数

创建一个静态类BaseContext维护一个threadlocal变量（用户id）

```java
package com.hippo.reggietakeout.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void set(Long id){
        threadLocal.set(id);
    }

    public static Long get(){
        return threadLocal.get();
    }
}
```

在登录拦截器的prehandle方法中实现保存用户id到BaseContext

```java
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("拦截到资源 {}",request.getRequestURI());
        /**
         * 判断是否已登录(session的employee属性是否为空)
         * 否，将处理结果写入response，返回false
         */
        if(request.getSession().getAttribute("employee") == null){
            log.info("用户未登录");
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
            return false;
        }
        // 确认用户已登录后再保存id
        BaseContext.set((Long)request.getSession().getAttribute("employee"));
        log.info("设置用户id");
        return true;
    }
```

在MetaObjectHandler中访问BaseContext获得用户id

```java
    public void insertFill(MetaObject metaObject) {
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.get());
        metaObject.setValue("updateUser", BaseContext.get());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.get());
    }
```

#### job_2 category crud

##### category 实体 

``` java
package com.hippo.reggietakeout.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Category implements Serializable {
    private Long id;
    private short type;
    private String name;
    private short sort;
    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) // 更新和插入时自动填充
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getSort() {
        return sort;
    }

    public void setSort(short sort) {
        this.sort = sort;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }

    public Long getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(Long updateUser) {
        this.updateUser = updateUser;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", sort=" + sort +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createUser=" + createUser +
                ", updateUser=" + updateUser +
                '}';
    }
}
```

##### CategoryMapper

```JAVA
package com.hippo.reggietakeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hippo.reggietakeout.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
```

##### CategoryController

```java
package com.hippo.reggietakeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hippo.reggietakeout.common.R;
import com.hippo.reggietakeout.entity.Category;
import com.hippo.reggietakeout.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    @PostMapping
    public R<String> newCategory(@RequestBody Category category){
        categoryMapper.insert(category);
        return R.success("succeed");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){

        // 分页构造器
        Page pageInfo = new Page<>(page, pageSize);

        // 条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        // 按更新时间排序
        queryWrapper.orderByDesc(Category::getUpdateTime);

        categoryMapper.selectPage(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(@RequestBody Category category){
        categoryMapper.updateById(category);
        return R.success("succeed");
    }

    @DeleteMapping
    public R<String> delete(String ids){
        categoryMapper.deleteById(ids);
        return R.success("succeed");
    }
}
```

