function initPage(pageId) {
    switch(pageId) {
        case 'stockIn':
            initStockInPage && initStockInPage();
            break;
        case 'stockOut':
            initStockOutPage && initStockOutPage();
            break;
        case 'taskBoard':
            initTaskBoard && initTaskBoard();
            break;
        case 'inventoryCheck':
            initInventoryCheck && initInventoryCheck();
            break;
        case 'location':
            initLocationManage && initLocationManage();
            break;
        case 'exception':
            initAbnormalPage && initAbnormalPage();
            break;
        case 'inventoryMonitor':
            initInventoryMonitor && initInventoryMonitor();
            break;
        case 'taskDispatch':
            initTaskSchedule && initTaskSchedule();
            break;
        case 'performance':
            initPerformance && initPerformance();
            break;
        case 'qualityCheck':
            initQualityCheck && initQualityCheck();
            break;
        case 'alertManagement':
            initWarningManage && initWarningManage();
            break;
        case 'userManage':
            initUserManage && initUserManage();
            break;
        case 'roleManage':
            initRoleManage && initRoleManage();
            break;
        case 'warehouseManage':
            initWarehouseManage && initWarehouseManage();
            break;
        case 'backup':
            initBackupPage && initBackupPage();
            break;
        case 'operationLog':
            initLogPage && initLogPage();
            break;
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const pageId = document.currentScript?.dataset?.pageId;
    if (pageId) {
        initPage(pageId);
    }
});