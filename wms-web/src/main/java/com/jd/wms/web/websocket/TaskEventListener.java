package com.jd.wms.web.websocket;

import com.jd.wms.common.event.TaskEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskEventListener {

    private final TaskWebSocketHandler webSocketHandler;

    public TaskEventListener(TaskWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @EventListener
    public void handleTaskEvent(TaskEvent event) {
        log.debug("收到任务事件: {}", event.getEventType());

        switch (event.getEventType()) {
            case "TASK_UPDATE":
                if (event.getOperatorId() != null) {
                    webSocketHandler.sendTaskUpdateToOperator(event.getOperatorId(), event.getData());
                }
                break;

            case "TASK_UPDATE_ALL_MANAGERS":
                webSocketHandler.sendToAllManagers(event.getData());
                break;

            case "TASK_ASSIGNED":
                if (event.getOperatorId() != null) {
                    webSocketHandler.sendTaskAssignedNotification(event.getOperatorId(), event.getData());
                }
                break;

            case "ALERT":
                if (event.getManagerId() != null) {
                    webSocketHandler.sendAlertNotification(event.getManagerId(), event.getData());
                }
                break;

            case "PERFORMANCE_UPDATE":
                if (event.getManagerId() != null) {
                    webSocketHandler.sendTaskUpdateToManager(event.getManagerId(), event.getData());
                }
                break;

            case "NOTIFY_ALL_MANAGERS":
                webSocketHandler.sendToAllManagers(event.getData());
                break;

            default:
                log.warn("未知事件类型: {}", event.getEventType());
        }
    }
}