package com.jd.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.wms.common.exception.WmsException;
import com.jd.wms.dao.entity.Product;
import com.jd.wms.dao.mapper.ProductMapper;
import com.jd.wms.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public Product getByProductCode(String productCode) {
        return baseMapper.selectByProductCode(productCode);
    }

    @Override
    @Transactional
    public boolean addProduct(Product product) {
        Product existing = getByProductCode(product.getProductCode());
        if (existing != null) {
            throw new WmsException("商品编号已存在");
        }
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        return save(product);
    }

    @Override
    @Transactional
    public boolean updateProduct(Product product) {
        product.setUpdateTime(new Date());
        return updateById(product);
    }

    @Override
    @Transactional
    public boolean deleteProduct(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new WmsException("商品不存在");
        }
        return removeById(id);
    }

}