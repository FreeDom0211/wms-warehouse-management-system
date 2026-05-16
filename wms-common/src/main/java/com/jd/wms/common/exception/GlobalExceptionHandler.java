package com.jd.wms.common.exception;

import com.jd.wms.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WmsException.class)
    public ResponseEntity<Result<Void>> handleWmsException(WmsException e, HttpServletRequest request) {
        log.error("业务异常[{}]: {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(e.getCode() != null ? e.getCode() : 500, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验失败[{}]: {}", request.getRequestURI(), errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.error(400, errorMessage));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleUnauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        log.error("无权限访问[{}]: {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.forbidden("无权限访问"));
    }

    @ExceptionHandler(org.apache.shiro.authc.AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuthenticationException(org.apache.shiro.authc.AuthenticationException e, HttpServletRequest request) {
        log.error("认证失败[{}]: {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.unauthorized("认证失败: " + e.getMessage()));
    }

    @ExceptionHandler(org.apache.shiro.authz.HostUnauthorizedException.class)
    public ResponseEntity<Result<Void>> handleHostUnauthorizedException(org.apache.shiro.authz.HostUnauthorizedException e, HttpServletRequest request) {
        log.error("主机认证失败[{}]: {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.unauthorized("未授权的主机访问"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常[{}]: ", request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("系统内部错误: " + e.getMessage()));
    }

}