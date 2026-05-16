package com.jd.wms.web.config;

import org.apache.shiro.authc.AuthenticationToken;

public class JwtToken implements AuthenticationToken {

    private static final long serialVersionUID = 1L;

    private String token;
    private String username;
    private Long userId;
    private String roleCode;

    public JwtToken(String token) {
        this.token = token;
    }

    public JwtToken(String token, String username, Long userId, String roleCode) {
        this.token = token;
        this.username = username;
        this.userId = userId;
        this.roleCode = roleCode;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

}