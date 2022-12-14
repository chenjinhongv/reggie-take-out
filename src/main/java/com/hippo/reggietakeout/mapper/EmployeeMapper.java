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
