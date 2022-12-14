#### TODO_1 后台系统登录

##### 创建后台用户Employee实体

```JAVA
package com.hippo.reggietakeout.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Employee implements Serializable {

    private static final long serialVersionUID=1L;
    private Long id;
    private String name;
    private String username;
    private String password;
    private String phone;
    private String sex;
    private String idNumber;
    private Integer Status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;
    @TableField(fill = FieldFill.INSERT)
    private Long updateUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public Integer getStatus() {
        return Status;
    }

    public void setStatus(Integer status) {
        Status = status;
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
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", sex='" + sex + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", Status=" + Status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createUser=" + createUser +
                ", updateUser=" + updateUser +
                '}';
    }
}
```

##### 创建employee表操作接口EmployeeMapper

```java
package com.hippo.reggietakeout.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hippo.reggietakeout.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.List;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee>{
}
```

##### EmployeeController登录功能实现

> 登录流程
>
> - 登录密码md5加密
> - 按照请求用户名查询账户信息
> - 检查账号是否存在：存在，下一步；不存在，返回账户不存在，结束；
> - 检查密码是否正确：正确，下一步；错误，返回密码错误信息，结束；
> - 检查用户是否被禁用：否，下一步；是，返回账户被禁用信息，结束；
> - 将用户id添加到session中（可作为后续登录状态校验），返回用户信息。

```
package com.hippo.reggietakeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hippo.reggietakeout.common.R;
import com.hippo.reggietakeout.entity.Employee;
import com.hippo.reggietakeout.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class employeeController {

    @Autowired
    private EmployeeMapper employeeMapper;
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee questEmployee){

        System.out.println(questEmployee);
        // 查看session检查登录状态
        System.out.println(request.getSession().getAttribute("employee"));
        // 密码md5加密
        String password = questEmployee.getPassword();
        System.out.println(password);
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        System.out.println(password);


        // 按用户名查询用户
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, questEmployee.getUsername());
        Employee emp = employeeMapper.selectOne(queryWrapper);

        // 登录逻辑判断过程
        if(emp == null){ // 用户是否存在
            return R.error("user do not exist");
        } else if (! password.equals(emp.getPassword())) { // 密码是否正确
            return R.error("incorrect password");
        } else if (emp.getStatus() != 1) { // 用户是否被禁用
            return R.error("user forbidden");
        } else {
            request.getSession().setAttribute("employee", emp.getId());
            return R.success(emp);
        }
    }
}
```

#### TUDO_2 后台系统退出登录

##### EmployeeController退出登录功能实现

> 退出登录流程
>
> - 清理session中的用户信息
> - 返回退出成功信息

```java
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("succeed");
    }
```

