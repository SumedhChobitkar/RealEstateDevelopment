//package com.RealEstateDevelopment.Handler;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.util.concurrent.CopyOnWriteArraySet;
//
//public class LocationWebSocketHandler extends TextWebSocketHandler {
//
//    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//
//        sessions.add(session);
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//
//        sessions.remove(session);
//    }
//
//    public void broadcastMessage(Object message) throws Exception {
//        String jsonMessage = objectMapper.writeValueAsString(message);
//        for (WebSocketSession session : sessions) {
//            if (session.isOpen()) {
//                session.sendMessage(new TextMessage(jsonMessage));
//            }
//        }
//    }
//}
package com.RealEstateDevelopment.Handler;

import com.RealEstateDevelopment.Entity.Location;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

public class LocationWebSocketHandler extends TextWebSocketHandler {
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void sendLocationUpdate(double latitude, double longitude) throws Exception {
        String googleMapsLink = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        locationData.put("mapLink", googleMapsLink);

        String jsonMessage = objectMapper.writeValueAsString(locationData);

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }

    public void broadcastMessage(Object message) throws Exception {
        String jsonMessage = objectMapper.writeValueAsString(message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }

//    private static class LocationMessage {
//        public double latitude;
//        public double longitude;
//
//        public LocationMessage(double latitude, double longitude) {
//            this.latitude = latitude;
//            this.longitude = longitude;
//        }
//    }
}
