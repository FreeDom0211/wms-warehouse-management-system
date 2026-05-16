package com.jd.wms.web.config;

import com.jd.wms.common.util.JwtUtil;
import com.jd.wms.dao.entity.Permission;
import com.jd.wms.dao.entity.Role;
import com.jd.wms.dao.entity.User;
import com.jd.wms.service.PermissionService;
import com.jd.wms.service.RoleService;
import com.jd.wms.service.UserRoleService;
import com.jd.wms.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        JwtToken jwtToken = (JwtToken) principals.getPrimaryPrincipal();
        String username = jwtToken.getUsername();
        
        if (username == null) {
            return null;
        }

        User user = userService.getByWorkNo(username);
        if (user == null) {
            user = userService.getByPhone(username);
        }
        if (user == null) {
            return null;
        }

        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        List<Role> userRoles = roleService.getRolesByUserId(user.getId());
        for (Role role : userRoles) {
            roles.add(role.getRoleCode());
            List<Permission> rolePermissions = permissionService.getPermissionsByRoleId(role.getId());
            for (Permission perm : rolePermissions) {
                permissions.add(perm.getPermCode());
            }
        }

        String roleCode = user.getRoleCode();
        if (roleCode != null) {
            roles.add(roleCode);
            Role role = roleService.getByRoleCode(roleCode);
            if (role != null) {
                List<Permission> rolePermissions = permissionService.getPermissionsByRoleId(role.getId());
                for (Permission perm : rolePermissions) {
                    permissions.add(perm.getPermCode());
                }
            }
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setRoles(roles);
        info.setStringPermissions(permissions);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) token;
        String jwt = jwtToken.getToken();

        if (jwt == null || jwt.isEmpty()) {
            throw new AuthenticationException("Token不能为空");
        }

        try {
            if (jwtUtil.isTokenExpired(jwt)) {
                throw new ExpiredCredentialsException("Token已过期");
            }

            Long userId = jwtUtil.getUserIdFromToken(jwt);
            User user = userService.getById(userId);

            if (user == null) {
                throw new UnknownAccountException("用户不存在");
            }

            if (user.getStatus() != 1) {
                throw new DisabledAccountException("用户已禁用");
            }

            String username = jwtUtil.getUsernameFromToken(jwt);
            String roleCode = jwtUtil.getRoleCodeFromToken(jwt);

            JwtToken authenticatedToken = new JwtToken(jwt, username, userId, roleCode != null ? roleCode : user.getRoleCode());

            return new SimpleAuthenticationInfo(authenticatedToken, jwt, getName());

        } catch (ExpiredCredentialsException e) {
            throw e;
        } catch (UnknownAccountException e) {
            throw e;
        } catch (DisabledAccountException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Token验证失败: " + e.getMessage());
        }
    }

}