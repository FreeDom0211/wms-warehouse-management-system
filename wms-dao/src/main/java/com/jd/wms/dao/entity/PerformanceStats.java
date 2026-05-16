package com.jd.wms.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("performance_stats")
public class PerformanceStats implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long operatorId;

    private Date statDate;

    private Integer stockInCount;

    private Integer stockOutCount;

    private Integer checkCount;

    private Integer totalHandleQuantity;

    private Integer errorCount;

    private BigDecimal accuracyRate;

    private BigDecimal avgHandleTime;

    private Date createTime;

    private Date updateTime;

}