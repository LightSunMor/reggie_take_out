package com.practice.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.practice.reggie.model.AddressBook;
import com.practice.reggie.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface addressBookService extends IService<AddressBook> {
    List<AddressBook> selectAllAddressByUser(User user);

    boolean updateDefaultWithOtherChange(AddressBook addressBook);
}
