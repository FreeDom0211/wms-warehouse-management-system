package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.Permission;
import com.jd.wms.dao.entity.Role;
import com.jd.wms.dao.entity.RolePermission;
import com.jd.wms.service.PermissionService;
import com.jd.wms.service.RolePermissionService;
import com.jd.wms.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/role")public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String roleName) {
        QueryWrapper<Role> wrapper = new QueryWrapper<>();
        if (roleCode != null && !roleCode.isEmpty()) {
            wrapper.like("role_code", roleCode);
        }
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like("role_name", roleName);
        }
        IPage<Role> page = roleService.page(new Page<>(pageNum, pageSize), wrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "");
        result.put("count", page.getTotal());
        result.put("data", page.getRecords());
        return result;
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("role", role);
        List<Permission> permissions = permissionService.getPermissionsByRoleId(id);
        result.put("permissions", permissions);
        return Result.success(result);
    }

    @PostMapping
    public Result<Role> add(@RequestBody Role role) {
        roleService.addRole(role);
        return Result.success("添加成功", role);    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateRole(role);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success("删除成功");
    }

    @GetMapping("/all")
    public Result<List<Role>> getAll() {
        List<Role> roles = roleService.getAllActiveRoles();
        return Result.success(roles);
    }

    @GetMapping("/{id}/permissions")    public Result<List<Permission>> getRolePermissions(@PathVariable Long id) {
        List<Permission> permissions = permissionService.getPermissionsByRoleId(id);
        return Result.success(permissions);
    }

    @PostMapping("/{id}/assign-permissions")    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> request) {
        List<Long> permissionIds = request.get("permissionIds");
        rolePermissionService.assignPermissions(id, permissionIds);
        return Result.success("权限分配成功");
    }

    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getOptions() {
        List<Role> roles = roleService.getAllActiveRoles();
        List<Map<String, Object>> options = roles.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", r.getId());
            map.put("label", r.getRoleName());
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(options);
    }

}