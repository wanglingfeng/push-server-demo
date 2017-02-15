package com.push.server;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lfwang on 2017/2/8.
 */
@ServerEndpoint("/webSocket/{name}")
@Component
public class MyWebSocket {

    private static String name;

    // 静态变量，用来记录当前在线连接数，应设计为线程安全的字段
    private static int onlineCount = 0;

    // concurrent包的线程安全set，用来存放每个客户端对应的MyWebSocket对象
    // private static CopyOnWriteArraySet<MyWebSocket> webSocketSet = new CopyOnWriteArraySet<>();
    private static ConcurrentHashMap<String, MyWebSocket> webSocketMap = new ConcurrentHashMap<>();

    // 与某个客户端的连接会话，需要通过它来客户端发送数据
    private Session session;

    /**
     * 建立连接成功
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("name") String name, Session session) {
        MyWebSocket.name = name;
        this.session = session;
        // webSocketSet.add(this); // 加入set中
        webSocketMap.put(session.getId(), this);

        addOnlineCount(); // 在线人数加一
        System.out.println("有新连接加入。当前在线人数为：" + getOnlineCount());

        try {
            sendMessage("连接成功，可以发送消息");
        } catch (IOException e) {
            System.out.println("IO异常");
        }
    }

    /**
     * 收到客户端消息
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("来自客户端的消息：" + message);

        // 群发消息
        /*for (MyWebSocket item : webSocketSet) {
            try {
                item.sendMessage("回复消息：" + message + " +10086");
            } catch (IOException ignored) {
            }
        }*/
        try {
            // this.sendMessage("回复消息：" + message + " +10086");
            MyWebSocket socket = webSocketMap.get(session.getId());
            socket.sendMessage("回复消息：" + name + " --> " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发生错误时
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose(Session session) {
        // webSocketSet.remove(this); // 从set中移除
        webSocketMap.remove(session.getId());
        subOnlineCount(); // 在线人数减一
        System.out.println("有一连接关闭。当前在线人数为：" + getOnlineCount());
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        MyWebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebSocket.onlineCount--;
    }
}
