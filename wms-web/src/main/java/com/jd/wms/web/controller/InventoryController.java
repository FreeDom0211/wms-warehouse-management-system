package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.Inventory;
import com.jd.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public Result<IPage<Inventory>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<Inventory> page = inventoryService.page(new Page<>(pageNum, pageSize));
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Inventory> getById(@PathVariable Long id) {
        Inventory inventory = inventoryService.getById(id);
        return Result.success(inventory);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Inventory inventory) {
        inventoryService.addInventory(inventory);
        return Result.success("添加成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Inventory inventory) {
        inventory.setId(id);
        inventoryService.updateInventory(inventory);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return Result.success("删除成功");
    }

}