package com.jd.wms.web.controller;

import com.jd.wms.common.annotation.RateLimiter;
import com.jd.wms.common.util.JwtUtil;
import com.jd.wms.common.vo.Result;
import com.jd.wms.dao.entity.Permission;
import com.jd.wms.dao.entity.User;
import com.jd.wms.service.PermissionService;
import com.jd.wms.service.UserService;
import com.jd.wms.service.VerifyCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VerifyCodeService verifyCodeService;

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/verifycode")
    public Result<Map<String, String>> getVerifyCode(HttpServletRequest request) {
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = request.getSession().getId();
        }
        String code = verifyCodeService.generateCode(sessionId);
        Map<String, String> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("code", code);
        log.debug("生成验证码[{}]: {}", sessionId, code);
        return Result.success(result);
    }

    @PostMapping("/manager/login")
    public Result<Map<String, Object>> managerLogin(@RequestBody Map<String, String> loginRequest) {
        String loginKey = loginRequest.get("loginKey");
        String password = loginRequest.get("password");

        if (loginKey == null || loginKey.isEmpty()) {
            return Result.badRequest("用户名不能为空");        }
        if (password == null || password.isEmpty()) {
            return Result.badRequest("密码不能为空");
        }

        User user = userService.login(loginKey, password);

        if (!"MANAGER".equals(user.getRoleCode())) {
            return Result.error(403, "非管理员用户无法使用管理员登录");        }

        String token = jwtUtil.generateToken(user.getId(), user.getWorkNo(), user.getRoleCode());

        List<Permission> permissions = permissionService.getMenuPermissions(user.getId(), user.getRoleCode());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        result.put("permissions", permissions);

        log.info("管理员[{}]登录成功", loginKey);
        return Result.success("登录成功", result);    }

    @PostMapping("/administrator/login")
    public Result<Map<String, Object>> administratorLogin(@RequestBody Map<String, String> loginRequest) {
        String workNo = loginRequest.get("loginKey");
        String password = loginRequest.get("password");

        if (workNo == null || workNo.isEmpty()) {
            return Result.badRequest("工号不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.badRequest("密码不能为空");
        }

        User user = userService.login(workNo, password);

        if (!"ADMINISTRATOR".equals(user.getRoleCode())) {
            return Result.error(403, "非仓储主管用户无法使用仓储主管登录");        }

        String token = jwtUtil.generateToken(user.getId(), user.getWorkNo(), user.getRoleCode());

        List<Permission> permissions = permissionService.getMenuPermissions(user.getId(), user.getRoleCode());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        result.put("permissions", permissions);

        log.info("仓储主管[{}]登录成功", workNo);
        return Result.success("登录成功", result);    }

    @PostMapping("/operator/login")
    public Result<Map<String, Object>> operatorLogin(@RequestBody Map<String, String> loginRequest) {
        String loginKey = loginRequest.get("loginKey");
        String password = loginRequest.get("password");

        if (loginKey == null || loginKey.isEmpty()) {
            return Result.badRequest("工号或手机号不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.badRequest("密码不能为空");
        }

        User user;
        if (loginKey.matches("^1[3-9]\\d{9}$")) {
            user = userService.loginByPhone(loginKey, password);
        } else {
            user = userService.login(loginKey, password);
        }

        if (!"OPERATOR".equals(user.getRoleCode())) {
            return Result.error(403, "非操作员用户无法使用操作员登录");        }

        String token = jwtUtil.generateToken(user.getId(), user.getWorkNo(), user.getRoleCode());

        List<Permission> permissions = permissionService.getMenuPermissions(user.getId(), user.getRoleCode());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        result.put("permissions", permissions);

        log.info("操作员[{}]登录成功", loginKey);
        return Result.success("登录成功", result);    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功");    }

    @GetMapping("/unauthorized")
    public Result<Void> unauthorized() {
        return Result.unauthorized("未登录或会话已过期");    }

    @GetMapping("/forbidden")
    public Result<Void> forbidden() {
        return Result.forbidden("无权限访问");    }

    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        return Result.success(new User());
    }

}