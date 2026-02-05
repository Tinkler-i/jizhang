package com.billmanager.jizhang.exception;

/**
 * 家庭组权限异常
 * 当用户没有足够的权限执行某操作时抛出
 */
public class FamilyPermissionException extends RuntimeException {
    
    private Integer code;
    private String permission;
    private String module;
    private String action;
    
    public FamilyPermissionException(String message) {
        super(message);
        this.code = 403;
    }
    
    public FamilyPermissionException(String permission, String message) {
        super(message);
        this.code = 403;
        this.permission = permission;
        parsePermission(permission);
    }
    
    public FamilyPermissionException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 创建数据访问权限异常
     * @param userId 当前用户ID
     * @param resourceType 资源类型（如：income, expense）
     * @param resourceId 资源ID
     * @return 异常实例
     */
    public static FamilyPermissionException dataAccessDenied(Long userId, String resourceType, Long resourceId) {
        String message = String.format("用户 %d 无权访问 %s 资源 (ID: %d)", userId, resourceType, resourceId);
        FamilyPermissionException exception = new FamilyPermissionException(message);
        exception.module = resourceType;
        return exception;
    }
    
    /**
     * 创建操作权限异常
     * @param userId 当前用户ID
     * @param permission 权限标识
     * @return 异常实例
     */
    public static FamilyPermissionException operationDenied(Long userId, String permission) {
        String message = String.format("用户 %d 没有 [%s] 权限", userId, permission);
        return new FamilyPermissionException(permission, message);
    }
    
    /**
     * 创建非家庭组成员异常
     * @param userId 当前用户ID
     * @return 异常实例
     */
    public static FamilyPermissionException notFamilyMember(Long userId) {
        String message = String.format("用户 %d 不是任何家庭组的成员", userId);
        FamilyPermissionException exception = new FamilyPermissionException(message);
        exception.code = 403;
        return exception;
    }
    
    /**
     * 创建非管理员异常
     * @param userId 当前用户ID
     * @return 异常实例
     */
    public static FamilyPermissionException notAdmin(Long userId) {
        String message = String.format("用户 %d 不是家庭组管理员，无法执行此操作", userId);
        FamilyPermissionException exception = new FamilyPermissionException(message);
        exception.code = 403;
        return exception;
    }
    
    private void parsePermission(String permission) {
        if (permission != null && permission.contains("_")) {
            int lastUnderscore = permission.lastIndexOf("_");
            this.module = permission.substring(0, lastUnderscore);
            this.action = permission.substring(lastUnderscore + 1);
        }
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public String getModule() {
        return module;
    }
    
    public String getAction() {
        return action;
    }
    
    @Override
    public String toString() {
        return "FamilyPermissionException{" +
                "code=" + code +
                ", permission='" + permission + '\'' +
                ", module='" + module + '\'' +
                ", action='" + action + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
