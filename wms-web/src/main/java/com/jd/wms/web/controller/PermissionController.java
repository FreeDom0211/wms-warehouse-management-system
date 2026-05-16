package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.Permission;
import com.jd.wms.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/permission")public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping
    public Result<IPage<Permission>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String permCode,
            @RequestParam(required = false) String permName) {
        QueryWrapper<Permission> wrapper = new QueryWrapper<>();
        if (permCode != null && !permCode.isEmpty()) {
            wrapper.like("perm_code", permCode);
        }
        if (permName != null && !permName.isEmpty()) {
            wrapper.like("perm_name", permName);
        }
        wrapper.orderByAsc("parent_id").orderByAsc("sort_order");
        IPage<Permission> page = permissionService.page(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Permission> getById(@PathVariable Long id) {
        Permission permission = permissionService.getById(id);
        return Result.success(permission);
    }

    @PostMapping
    public Result<Permission> add(@RequestBody Permission permission) {
        permissionService.addPermission(permission);
        return Result.success("添加成功", permission);    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Permission permission) {
        permission.setId(id);
        permissionService.updatePermission(permission);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success("删除成功");
    }

    @GetMapping("/all")
    public Result<List<Permission>> getAll() {
        List<Permission> permissions = permissionService.list();
        return Result.success(permissions);
    }

    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getTree() {
        List<Permission> permissions = permissionService.list();
        List<Map<String, Object>> tree = buildTree(permissions, 0L);
        return Result.success(tree);
    }

    private List<Map<String, Object>> buildTree(List<Permission> permissions, Long parentId) {
        return permissions.stream()
                .filter(p -> p.getParentId().equals(parentId))
                .map(p -> {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", p.getId());
                    node.put("label", p.getPermName());
                    node.put("permCode", p.getPermCode());
                    node.put("menuUrl", p.getMenuUrl());
                    List<Map<String, Object>> children = buildTree(permissions, p.getId());
                    if (!children.isEmpty()) {
                        node.put("children", children);
                    }
                    return node;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/options")
    public Result<List<Map<String, Object>>> getOptions() {
        List<Permission> permissions = permissionService.list();
        List<Map<String, Object>> options = permissions.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("value", p.getId());
            map.put("label", p.getPermName());
            map.put("parentId", p.getParentId());
            return map;
        }).collect(Collectors.toList());
        return Result.success(options);
    }

}