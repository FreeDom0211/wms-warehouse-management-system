package com.jd.wms.web.controller;

import com.jd.wms.common.util.JwtUtil;
import com.jd.wms.dao.entity.*;
import com.jd.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class WmsSystemTest {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeAll
    public static void setUpBeforeClass() {
        log.info("========== 仓储管理系统测试初始化 ==========");
    }

    @Test
    @DisplayName("1. 用户登录测试 - 管理员登录")
    public void testManagerLogin() {
        log.info("\n---------- 测试: 管理员登录 ----------");
        User user = userService.login("admin001", "123456");
        assertNotNull(user);
        assertEquals("MANAGER", user.getRoleCode());
        assertNotNull(user.getName());
        log.info("管理员登录成功: {}", user.getName());
        
        String token = jwtUtil.generateToken(user.getId(), user.getWorkNo(), user.getRoleCode());
        assertNotNull(token);
        log.info("生成Token: {}", token.substring(0, 20) + "...");
    }

    @Test
    @DisplayName("2. 用户登录测试 - 仓储主管登录")
    public void testAdministratorLogin() {
        log.info("\n---------- 测试: 仓储主管登录 ----------");
        User user = userService.login("admin002", "123456");
        assertNotNull(user);
        assertEquals("ADMINISTRATOR", user.getRoleCode());
        assertNotNull(user.getName());
        log.info("仓储主管登录成功: {}", user.getName());
        
        String token = jwtUtil.generateToken(user.getId(), user.getWorkNo(), user.getRoleCode());
        assertNotNull(token);
        log.info("生成Token: {}", token.substring(0, 20) + "...");
    }

    @Test
    @DisplayName("3. 用户登录测试 - 操作员登录")
    public void testOperatorLogin() {
        log.info("\n---------- 测试: 操作员登录 ----------");
        User user = userService.login("op001", "123456");
        assertNotNull(user);
        assertEquals("OPERATOR", user.getRoleCode());
        assertNotNull(user.getName());
        log.info("操作员登录成功: {}", user.getName());
        
        String token = jwtUtil.generateToken(user.getId(), user.getWorkNo(), user.getRoleCode());
        assertNotNull(token);
        log.info("生成Token: {}", token.substring(0, 20) + "...");
    }

    @Test
    @DisplayName("4. 用户管理模块测试 - 管理员")
    public void testManagerUserManagement() {
        log.info("\n---------- 测试: 管理员 - 用户管理 ----------");
        
        User newUser = new User();
        newUser.setWorkNo("test_user_" + System.currentTimeMillis());
        newUser.setPhone("13900139999");
        newUser.setName("测试用户");
        newUser.setPassword("123456");
        newUser.setRoleCode("OPERATOR");
        
        userService.register(newUser);
        assertNotNull(newUser.getId());
        log.info("创建用户成功: {}", newUser.getName());
        
        User foundUser = userService.getById(newUser.getId());
        assertNotNull(foundUser);
        assertEquals("测试用户", foundUser.getName());
        log.info("查询用户成功: {}", foundUser.getName());
        
        foundUser.setName("测试用户修改");
        userService.updateById(foundUser);
        User updatedUser = userService.getById(newUser.getId());
        assertEquals("测试用户修改", updatedUser.getName());
        log.info("更新用户成功: {}", updatedUser.getName());
        
        userService.removeById(newUser.getId());
        User deletedUser = userService.getById(newUser.getId());
        assertNull(deletedUser);
        log.info("删除用户成功");
    }

    @Test
    @DisplayName("5. 用户管理模块测试 - 仓储主管")
    public void testAdministratorUserManagement() {
        log.info("\n---------- 测试: 仓储主管 - 用户管理 ----------");
        
        User user = userService.login("admin002", "123456");
        assertNotNull(user);
        
        List<User> userList = userService.list();
        assertTrue(userList.size() >= 3);
        log.info("查询用户列表成功, 共 {} 条记录", userList.size());
    }

    @Test
    @DisplayName("6. 角色权限管理测试 - 管理员")
    public void testManagerRolePermissionManagement() {
        log.info("\n---------- 测试: 管理员 - 角色权限管理 ----------");
        
        List<Role> roles = roleService.list();
        assertTrue(roles.size() >= 3);
        log.info("查询角色列表成功, 共 {} 个角色", roles.size());
        
        List<Permission> permissions = permissionService.list();
        assertTrue(permissions.size() > 0);
        log.info("查询权限列表成功, 共 {} 个权限", permissions.size());
        
        Role role = roleService.getByRoleCode("OPERATOR");
        assertNotNull(role);
        assertEquals("仓储操作员", role.getRoleName());
        log.info("查询角色成功: {}", role.getRoleName());
        
        List<Permission> rolePermissions = permissionService.getMenuPermissions(1L, "OPERATOR");
        assertTrue(rolePermissions.size() > 0);
        log.info("查询角色权限成功, 共 {} 个权限", rolePermissions.size());
    }

    @Test
    @DisplayName("7. 仓库管理模块测试")
    public void testWarehouseManagement() {
        log.info("\n---------- 测试: 仓库管理 ----------");
        
        List<Warehouse> warehouses = warehouseService.list();
        assertTrue(warehouses.size() >= 3);
        log.info("查询仓库列表成功, 共 {} 个仓库", warehouses.size());
        
        Warehouse warehouse = warehouseService.getById(1L);
        assertNotNull(warehouse);
        assertNotNull(warehouse.getName());
        log.info("查询仓库成功: {}", warehouse.getName());
        
        Warehouse newWarehouse = new Warehouse();
        newWarehouse.setWarehouseCode("TEST_" + System.currentTimeMillis());
        newWarehouse.setName("测试仓库");
        newWarehouse.setAddress("测试地址");
        warehouseService.addWarehouse(newWarehouse);
        assertNotNull(newWarehouse.getId());
        log.info("创建仓库成功: {}", newWarehouse.getName());
        
        warehouseService.removeById(newWarehouse.getId());
        Warehouse deleted = warehouseService.getById(newWarehouse.getId());
        assertNull(deleted);
        log.info("删除仓库成功");
    }

    @Test
    @DisplayName("8. 货位管理模块测试")
    public void testLocationManagement() {
        log.info("\n---------- 测试: 货位管理 ----------");
        
        List<Location> locations = locationService.list();
        assertTrue(locations.size() > 0);
        log.info("查询货位列表成功, 共 {} 个货位", locations.size());
        
        Location location = locationService.getById(1L);
        assertNotNull(location);
        assertEquals("A001", location.getLocationCode());
        log.info("查询货位成功: {}", location.getLocationCode());
        
        Location newLocation = new Location();
        newLocation.setLocationCode("LOC_" + System.currentTimeMillis());
        newLocation.setWarehouseId(1L);
        newLocation.setZone("A区");
        locationService.addLocation(newLocation);
        assertNotNull(newLocation.getId());
        log.info("创建货位成功: {}", newLocation.getLocationCode());
    }

    @Test
    @DisplayName("9. 权限验证测试 - 操作员无用户管理权限")
    public void testOperatorPermissionRestriction() {
        log.info("\n---------- 测试: 权限验证 - 操作员权限限制 ----------");
        
        User operator = userService.login("op001", "123456");
        assertEquals("OPERATOR", operator.getRoleCode());
        
        List<Permission> permissions = permissionService.getMenuPermissions(operator.getId(), operator.getRoleCode());
        boolean hasUserPermission = permissions.stream()
            .anyMatch(p -> "user".equals(p.getPermCode()));
        assertFalse(hasUserPermission);
        log.info("操作员无用户管理权限, 验证通过");
    }

    @Test
    @DisplayName("10. 权限验证测试 - 仓储主管权限")
    public void testAdministratorPermission() {
        log.info("\n---------- 测试: 权限验证 - 仓储主管权限 ----------");
        
        User admin = userService.login("admin002", "123456");
        assertEquals("ADMINISTRATOR", admin.getRoleCode());
        
        List<Permission> permissions = permissionService.getMenuPermissions(admin.getId(), admin.getRoleCode());
        assertTrue(permissions.size() > 0);
        log.info("仓储主管权限列表: {} 个权限", permissions.size());
        
        boolean hasTaskPermission = permissions.stream()
            .anyMatch(p -> "task".equals(p.getPermCode()));
        assertTrue(hasTaskPermission);
        log.info("仓储主管有任务管理权限, 验证通过");
    }

    @Test
    @DisplayName("11. 权限验证测试 - 管理员权限")
    public void testManagerPermission() {
        log.info("\n---------- 测试: 权限验证 - 管理员权限 ----------");
        
        User manager = userService.login("admin001", "123456");
        assertEquals("MANAGER", manager.getRoleCode());
        
        List<Permission> permissions = permissionService.getMenuPermissions(manager.getId(), manager.getRoleCode());
        assertTrue(permissions.size() > 0);
        log.info("管理员权限列表: {} 个权限", permissions.size());
        
        boolean hasUserPermission = permissions.stream()
            .anyMatch(p -> "user".equals(p.getPermCode()));
        assertTrue(hasUserPermission);
        log.info("管理员有用户管理权限, 验证通过");
        
        boolean hasRolePermission = permissions.stream()
            .anyMatch(p -> "role".equals(p.getPermCode()));
        assertTrue(hasRolePermission);
        log.info("管理员有角色管理权限, 验证通过");
    }

}
