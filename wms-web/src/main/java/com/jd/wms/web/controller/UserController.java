package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.util.BCryptUtil;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.Role;
import com.jd.wms.dao.entity.User;
import com.jd.wms.service.RoleService;
import com.jd.wms.service.UserRoleService;
import com.jd.wms.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping({"/user", "/api/user"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String workNo,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String roleCode) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (workNo != null && !workNo.isEmpty()) {
            wrapper.like("work_no", workNo);
        }
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (roleCode != null && !roleCode.isEmpty()) {
            wrapper.eq("role_code", roleCode);
        }
        IPage<User> page = userService.page(new Page<>(pageNum, pageSize), wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "");
        result.put("count", page.getTotal());
        result.put("data", page.getRecords());
        return result;
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        List<Role> roles = roleService.getRolesByUserId(id);
        result.put("roles", roles);
        return Result.success(result);
    }

    @PostMapping
    public Result<User> add(@RequestBody User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("123456");
        }
        userService.register(user);
        return Result.success("添加成功", user);    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setPassword(null);
        user.setUpdateTime(new java.util.Date());
        userService.updateById(user);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userRoleService.removeUserRoles(id);
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        User user = userService.getById(id);
        user.setStatus(request.get("status"));
        user.setUpdateTime(new java.util.Date());
        userService.updateById(user);
        return Result.success("状态更新成功");    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            newPassword = "123456";
        }
        userService.resetPassword(id, newPassword);
        log.info("用户[{}]密码已重置", id);
        return Result.success("密码重置成功");
    }

    @PutMapping("/{id}/update-password")
    public Result<Void> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        userService.updatePassword(id, oldPassword, newPassword);
        return Result.success("密码修改成功");
    }

    @GetMapping("/roles")
    public Result<List<Role>> getAllRoles() {
        List<Role> roles = roleService.getAllActiveRoles();
        return Result.success(roles);
    }

    @PostMapping("/{id}/assign-roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> request) {
        List<Long> roleIds = request.get("roleIds");
        userRoleService.assignRoles(id, roleIds);
        return Result.success("角色分配成功");
    }

    @GetMapping("/role-options")
    public Result<List<Map<String, Object>>> getRoleOptions() {
        List<Role> roles = roleService.getAllActiveRoles();
        List<Map<String, Object>> options = roles.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", r.getRoleCode());
            map.put("label", r.getRoleName());
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(options);
    }

}