package com.jd.wms.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.Goods;
import com.jd.wms.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goods")public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping
    public Result<IPage<Goods>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<Goods> page = goodsService.page(new Page<>(pageNum, pageSize));
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Goods> getById(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        return Result.success(goods);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Goods goods) {
        goodsService.addGoods(goods);
        return Result.success("添加成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Goods goods) {
        goods.setId(id);
        goodsService.updateGoods(goods);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        goodsService.deleteGoods(id);
        return Result.success("删除成功");
    }

}