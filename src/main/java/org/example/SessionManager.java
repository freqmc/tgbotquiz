package org.example;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private final Map<Long, UserSession> sessions = new HashMap<>();

    public UserSession getSession(long chatId) {
        return sessions.computeIfAbsent(chatId, UserSession::new);
    }

    public void removeSession(long chatId) {
        sessions.remove(chatId);
    }

    public boolean hasSession(long chatId) {
        return sessions.containsKey(chatId);
    }
}