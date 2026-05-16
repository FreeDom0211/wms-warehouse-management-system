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
@TableName("quality_check")
public class QualityCheck implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String checkNo;

    private String checkType;

    private Long productId;

    private String batchNo;

    private Long locationId;

    private Integer sampleQuantity;

    private Integer qualifiedQuantity;

    private Integer unqualifiedQuantity;

    private String checkResult;

    private String qualityIssue;

    private String status;

    private Long auditorId;

    private String auditorResult;

    private String auditorRemark;

    private Date createTime;

    private Date updateTime;

}