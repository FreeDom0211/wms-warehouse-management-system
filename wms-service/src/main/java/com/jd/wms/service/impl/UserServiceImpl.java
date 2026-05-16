package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.common.util.BCryptUtil;
import com.jd.wms.dao.entity.User;
import com.jd.wms.dao.mapper.UserMapper;
import com.jd.wms.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(String workNo, String password) {
        User user = getByWorkNo(workNo);
        if (user == null) {
            throw new WmsException(401, "工号不存在");
        }
        if (!BCryptUtil.matches(password, user.getPassword())) {
            throw new WmsException(401, "密码错误");
        }
        if (user.getStatus() != 1) {
            throw new WmsException(403, "用户已禁用");
        }
        return user;
    }

    @Override
    public User loginByPhone(String phone, String password) {
        User user = getByPhone(phone);
        if (user == null) {
            throw new WmsException(401, "手机号不存在");
        }
        if (!BCryptUtil.matches(password, user.getPassword())) {
            throw new WmsException(401, "密码错误");
        }
        if (user.getStatus() != 1) {
            throw new WmsException(403, "用户已禁用");
        }
        return user;
    }

    @Override
    public User getByWorkNo(String workNo) {
        return baseMapper.selectByWorkNo(workNo);
    }

    @Override
    public User getByPhone(String phone) {
        return baseMapper.selectByPhone(phone);
    }

    @Override
    @Transactional
    public boolean register(User user) {
        User existingUser = getByWorkNo(user.getWorkNo());
        if (existingUser != null) {
            throw new WmsException("工号已存在");
        }
        existingUser = getByPhone(user.getPhone());
        if (existingUser != null) {
            throw new WmsException("手机号已存在");
        }
        user.setPassword(BCryptUtil.encode(user.getPassword()));
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        return save(user);
    }

    @Override
    @Transactional
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new WmsException("用户不存在");
        }
        if (!BCryptUtil.matches(oldPassword, user.getPassword())) {
            throw new WmsException("原密码错误");
        }
        user.setPassword(BCryptUtil.encode(newPassword));
        user.setUpdateTime(new Date());
        return updateById(user);
    }

    @Override
    @Transactional
    public boolean resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new WmsException("用户不存在");
        }
        user.setPassword(BCryptUtil.encode(newPassword));
        user.setUpdateTime(new Date());
        return updateById(user);
    }

}