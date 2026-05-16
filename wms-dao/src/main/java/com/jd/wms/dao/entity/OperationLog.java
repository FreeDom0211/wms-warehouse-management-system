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
@TableName("operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String module;

    private String operation;

    private String methodName;

    private String requestUrl;

    private String requestMethod;

    private String requestParams;

    private String responseResult;

    private Long operatorId;

    private String operatorName;

    private String operatorIp;

    private Long executionTime;

    private String status;

    private String errorMessage;

    private Date createTime;

}