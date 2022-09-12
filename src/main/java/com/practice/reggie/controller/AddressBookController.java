package com.practice.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.practice.reggie.model.AddressBook;
import com.practice.reggie.model.R;
import com.practice.reggie.model.User;
import com.practice.reggie.service.impl.addressBookServiceImpl;
import com.practice.reggie.util.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {
    @Resource
    private addressBookServiceImpl addressBookService;
    /**
     * 获取所有的地址
     * @param user
     * @return
     */
    @PostMapping("/list")
    R<List<AddressBook>> selectAddressBook(@RequestBody User user)
    {
        //封装service方法，根据phone拿到该用户对应的所有地址
        log.info("获取所有地址，拿到对象：{}",user.toString());
        List<AddressBook> addressBooks = addressBookService.selectAllAddressByUser(user);
        return R.success(addressBooks);
    }

    /**
     * 获取最新的地址
     * @return
     */
    @GetMapping("/lastUpdate")
    R<AddressBook> getNewAddress()
    {
        AddressBook one = addressBookService.getOne(new LambdaQueryWrapper<AddressBook>().orderByDesc(AddressBook::getUpdateTime));
        return R.success(one);
    }

    /**
     * 新增地址,在这之前一定有经过list请求
     * @param addressBook
     * @return
     */
    @PostMapping()
    R<String> insert_address(@RequestBody AddressBook addressBook)
    {
        log.info("新增地址，传输addressBook：{}",addressBook.toString());
        if (BaseContext.getCurrentId() !=null) //防止异常
        {
            //封装用户id
            addressBook.setUserId(BaseContext.getCurrentId());
        }
        //保存新地址
        boolean save = addressBookService.save(addressBook);
        if (save)
        {
            log.info("保存新地址已成功");
            return R.success("保存新地址成功");
        }
        log.error("保存新地址失败了");
        return R.error("保存新地址失败");
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping()
    R<String> update_address(@RequestBody AddressBook addressBook)
    {
        log.info("修改地址，新地址为{}",addressBook.toString());
        boolean b = addressBookService.updateById(addressBook);
        if (b)
        {
            log.info("修改地址已成功");
            return R.success("修改地址成功");
        }
        return R.error("修改地址失败");
    }

    /**
     * 根据id回查地址详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    R<AddressBook> getOneById(@PathVariable("id") Long id)
    {
        log.info("回查地址详情，拿到id为{}",id);
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getIsDeleted,0);
        queryWrapper.eq(AddressBook::getId,id);
        AddressBook one = addressBookService.getOne(queryWrapper);
        if (one==null)
        {
            return R.error("未知错误");
        }
        return R.success(one);
    }

    /**
     * 删除指定地址
     * @param ids
     * @return
     */
    @DeleteMapping()
    R<String> delete_address(@RequestParam Long ids) //能拿到
    {
        log.info("删除地址，拿到id为{}",ids);
        AddressBook addressBook = new AddressBook();
        addressBook.setId(ids);
        addressBook.setIsDeleted(1);
        addressBookService.updateById(addressBook);
        return R.success("删除成功");
    }

    /**
     * 设置默认地址接口
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    R<String> setDefaultAddress(@RequestBody AddressBook addressBook)
    {
        log.info("设置默认地址，拿到id为{}",addressBook.getId());
        log.info("设置默认地址，拿到对象{}",addressBook.toString());
        //封装service方法，把当前的地址设置为默认，该用户的其他地址设置为非默认
        boolean b = addressBookService.updateDefaultWithOtherChange(addressBook);
        if (b)
        {
            log.info("设置默认地址已成功");
            return R.success("设置成功");
        }
        log.error("设置默认地址已失败");
        return R.error("设置失败");
    }

    /**
     * 获取默认地址详情 !!!
     * @return
     */
    @GetMapping("/default")
    R<AddressBook> getDefaultAddress()
    {
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getIsDeleted,0);
        queryWrapper.eq(AddressBook::getIsDefault,1);
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        //根据条件查询默认地址
        AddressBook one = addressBookService.getOne(queryWrapper);
        if (one==null)
            return R.error("没有找到默认地址");
        log.info("找到默认地址了");
        return R.success(one);
    }

}
