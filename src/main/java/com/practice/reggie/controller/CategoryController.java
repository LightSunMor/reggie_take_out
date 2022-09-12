package com.practice.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.practice.reggie.model.Category;
import com.practice.reggie.model.R;
import com.practice.reggie.service.impl.categoryServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.LongAccumulator;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private categoryServiceImpl categoryService;

    /**
     * category页面分页查询接口
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    R<Page> findByPage_cate(String page,String pageSize)
    {
        log.info("分类页面分页拿到page：{}，pageSize{}",page,pageSize);
        //1.创建Page分页对象
        Page<Category> categoryPage=new Page<>(Long.parseLong(page), Long.parseLong(pageSize));
        //2.指定分页查询条件
        LambdaQueryWrapper<Category> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        lambdaQueryWrapper.eq(Category::getIsDeleted,0);
        //2.执行分页查询操作
        Page<Category> categoryPage1 = categoryService.page(categoryPage,lambdaQueryWrapper);
        //3.返回值
        return R.success(categoryPage1);
    }

    /**
     * 新增菜品和套餐分类接口
     * @param category
     * @return
     */
    @PostMapping
    R<String> insert_cate(@RequestBody Category category)
    {
        //1.拿到对象插入
        boolean b = categoryService.save(category);
        //2.判断是否插入成功
        if (b)
        {
            return R.success("新增成功");
        }
        return R.error("新增失败");
    }

    /**
     * 根据id拿到对应数据 接口
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    R<Category> selectOne_cate(@PathVariable("id") Long id)
    {
        //1.根据拿到的id查出全部信息
        log.info("分类修改拿到的id：{}",id);
        Category byId = categoryService.getById(id);
        //2.返回信息
        return R.success(byId);
    }

    /**
     * 修改接口
     * @param category
     * @return
     */
    @PutMapping
    R<String> update_cate(@RequestBody Category category)
    {
        //1.根据id修改
        boolean b = categoryService.updateById(category);
        //返回结果
        if (b)
        {
            return R.success("修改成功");
        }
        return R.error("修改失败");
    }

    /**
     * 删除接口
     * @param ids
     * @return
     */
    @DeleteMapping
    R<String> delete_cate(@RequestParam("ids") Long ids)
    {
        boolean b = categoryService.removeById(ids);
        if (b)
        {
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }

    /**
     * 返回分类的所有名称list
     * @return
     */
    @GetMapping("/list")
    R<List<Category>> getCategoryList(Integer type)  //不能加@RequesBody,因为前端传输的数据并有没封装为一个整体json
    {
        log.info("分类list拿到type为{}",type);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        queryWrapper.eq(type!=null,Category::getType,type);
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //查询数据
        List<Category> categoryList = categoryService.list(queryWrapper);
        return R.success(categoryList);
    }
}
