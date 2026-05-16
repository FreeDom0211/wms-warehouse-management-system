package com.jd.wms.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TaskWebSocketHandler extends TextWebSocketHandler {

    private final Map<Long, Set<WebSocketSession>> operatorSessions = new ConcurrentHashMap<>();
    private final Map<Long, Set<WebSocketSession>> managerSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            String userIdStr = query.split("userId=")[1].split("&")[0];
            Long userId = Long.parseLong(userIdStr);

            if (query.contains("role=OPERATOR")) {
                operatorSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
                log.info("操作员[{}] WebSocket连接已建立", userId);
            } else if (query.contains("role=MANAGER")) {
                managerSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
                log.info("主管[{}] WebSocket连接已建立", userId);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("收到WebSocket消息: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        operatorSessions.values().forEach(sessions -> sessions.remove(session));
        managerSessions.values().forEach(sessions -> sessions.remove(session));
        log.info("WebSocket连接已关闭");
    }

    public void sendTaskUpdateToOperator(Long operatorId, Object taskUpdate) {
        Set<WebSocketSession> sessions = operatorSessions.get(operatorId);
        if (sessions != null && !sessions.isEmpty()) {
            sendMessage(sessions, taskUpdate, "TASK_UPDATE");
            log.info("向操作员[{}]推送任务更新", operatorId);
        }
    }

    public void sendTaskUpdateToManager(Long managerId, Object taskUpdate) {
        Set<WebSocketSession> sessions = managerSessions.get(managerId);
        if (sessions != null && !sessions.isEmpty()) {
            sendMessage(sessions, taskUpdate, "TASK_UPDATE");
            log.info("向主管[{}]推送任务更新", managerId);
        }
    }

    public void sendToAllManagers(Object message) {
        managerSessions.values().forEach(sessions -> sendMessage(sessions, message, "NOTIFICATION"));
    }

    public void sendToAllOperators(Object message) {
        operatorSessions.values().forEach(sessions -> sendMessage(sessions, message, "NOTIFICATION"));
    }

    public void sendTaskAssignedNotification(Long operatorId, Object taskInfo) {
        Set<WebSocketSession> sessions = operatorSessions.get(operatorId);
        if (sessions != null && !sessions.isEmpty()) {
            sendMessage(sessions, taskInfo, "TASK_ASSIGNED");
            log.info("向操作员[{}]推送任务分配通知", operatorId);
        }
    }

    public void sendAlertNotification(Long managerId, Object alertInfo) {
        Set<WebSocketSession> sessions = managerSessions.get(managerId);
        if (sessions != null && !sessions.isEmpty()) {
            sendMessage(sessions, alertInfo, "ALERT");
            log.info("向主管[{}]推送预警通知", managerId);
        }
    }

    private void sendMessage(Set<WebSocketSession> sessions, Object data, String type) {
        try {
            Map<String, Object> message = new ConcurrentHashMap<>();
            message.put("type", type);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(message);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        log.error("发送WebSocket消息失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("构建WebSocket消息失败: {}", e.getMessage());
        }
    }

    public int getOnlineOperatorCount() {
        return operatorSessions.values().stream().mapToInt(Set::size).sum();
    }

    public int getOnlineManagerCount() {
        return managerSessions.values().stream().mapToInt(Set::size).sum();
    }

}