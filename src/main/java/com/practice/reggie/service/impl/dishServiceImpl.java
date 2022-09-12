package com.practice.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.practice.reggie.Handler.CustomException;
import com.practice.reggie.mapper.dishMapper;
import com.practice.reggie.model.Category;
import com.practice.reggie.model.Dish;
import com.practice.reggie.model.DishFlavor;
import com.practice.reggie.model.SetmealDish;
import com.practice.reggie.model.dto.DishDto;
import com.practice.reggie.service.dishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class dishServiceImpl extends ServiceImpl<dishMapper, Dish> implements dishService {
    @Autowired
    private dishFlavorServiceImpl dishFlavorService;
    @Autowired
    private setMealDishServiceImpl setMealDishService;
//    @Autowired   //导入报错
//    private categoryServiceImpl categoryService;
    /**
     * 新增菜品，同时添加菜品对应的口味
     * @param dishDto
     * @return
     */
    //因为是多张表进行操作，所以要进行事务操作的控制(Transactional)，以防数据操作异常,保证数据一致性
    @Transactional //在主启动类开启事务支持（EnableTransactionManagement），才能生效

    @Override
    public boolean saveWithInFlavor(DishDto dishDto) {
        //保存菜品，因为是dish的继承类，可以直接save，save方法自动扫描于数据库对应的字段
        boolean save1 = super.save(dishDto);
        //保存菜品对应的口味
        Long id = dishDto.getId();//菜品id
        List<DishFlavor> flavors = dishDto.getFlavors();
        //将每一个dishFlavor对象附上对应的菜品id（口味对应菜品id）
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(id);
        }
        boolean save2 = dishFlavorService.saveBatch(flavors);
        if (save1&&save2)
        {
            return true;
        }
        return false;
    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @Override
    @Transactional
    public DishDto getByIdWithFlavor(Long id) {
        //1.创建返回对象
        DishDto dishDto=new DishDto();
        //2.查询dish
        Dish dish = super.getById(id);
        //3.先拷贝dish到dishDto
        BeanUtils.copyProperties(dish,dishDto);
        //4.查询当前菜品对应的口味信息，注意一个dish可能有多个dishFlavor数据
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        queryWrapper.eq(DishFlavor::getIsDeleted,0);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        //前端使用的categoryId显示分类名，故不用给dishDto赋值分类名
        return dishDto;
    }

    /**
     * 修改dish，并且修改对应口味
     * @param dishDto
     * @return
     */
    @Override
    @Transactional
    public boolean updateWithFlavor(DishDto dishDto) {
        //更新修改dish
        boolean b = super.updateById(dishDto);
        //可以先清理当前菜品原口味数据delete
        //设置条件找到对应数据并删除
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //再添加当前提交过来的口味数据insert
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
        }
        boolean b1 = dishFlavorService.saveBatch(flavors);
        if (b&&b1)
            return true;
        return false;
    }

    /**
     * （批量）删除dish及其相关信息
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public boolean deleteWithFlavorAndSetmeal(ArrayList<Long> ids) {
        //1.检查当前菜品是否有包含于相关套餐中
        for (Long id : ids) {
            LambdaQueryWrapper<SetmealDish> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getDishId,id);
            int count = setMealDishService.count(queryWrapper);
            if (count>0)
            {
                throw new CustomException("当前菜品关联了套餐,不能删除");
            }
            break;
        }
        //2.修改isDeleted字段
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setIsDeleted(1);
            dish.setId(id);
            boolean b = super.updateById(dish);

            //删除dish相关口味数据
            LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId,id);
            DishFlavor dishFlavor = new DishFlavor();
            dishFlavor.setIsDeleted(1);
            boolean b1 = dishFlavorService.update(dishFlavor,queryWrapper);
            if (!b&&!b1)
                return false;
        }
        return true;
    }


}
