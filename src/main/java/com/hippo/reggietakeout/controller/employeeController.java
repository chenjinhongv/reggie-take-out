package com.hippo.reggietakeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hippo.reggietakeout.common.R;
import com.hippo.reggietakeout.entity.Employee;
import com.hippo.reggietakeout.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.util.validation.metadata.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class employeeController {

    private final String DEFAULT_PASSWORD="123456";
    @Autowired
    private EmployeeMapper employeeMapper;
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee questEmployee){

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

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("succeed");
    }

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

    @PutMapping
    public R<String> changeStatus(HttpServletRequest request, @RequestBody Employee questEmployee){
        // 设置更新人，更新时间
        questEmployee.setUpdateTime(LocalDateTime.now());
        questEmployee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        int i = employeeMapper.updateById(questEmployee);
        return R.success("succeed");
    }

    @GetMapping("/{id}")
    public R<Employee> findById(@PathVariable("id") Long id){
        Employee employee = employeeMapper.selectById(id);
        if(employee != null){
            return R.success(employee);
        }else{
            return R.error("user not exist");
        }
    }
}
