package com.jd.wms.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("inventory_alert")
public class InventoryAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String alertType;

    private Long productId;

    private Long warehouseId;

    private Long locationId;

    private Integer currentQuantity;

    private Integer thresholdValue;

    private String alertLevel;

    private String status;

    private String description;

    private Long handlerId;

    private String handleResult;

    private Date createTime;

    private Date updateTime;

}