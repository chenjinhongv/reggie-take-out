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
