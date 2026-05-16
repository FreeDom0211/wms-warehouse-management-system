package com.jd.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.wms.dao.entity.User;

public interface UserService extends IService<User> {

    User login(String workNo, String password);

    User loginByPhone(String phone, String password);

    User getByWorkNo(String workNo);

    User getByPhone(String phone);

    boolean register(User user);

    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    boolean resetPassword(Long userId, String newPassword);

}