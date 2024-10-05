package com.github.gentoopng.tducenvmirror.WebSocket;
import com.github.gentoopng.tducenvmirror.TDUCEnvMirror;
import com.github.gentoopng.tducenvmirror.WebSocket.WebSocketClient;

import java.net.http.HttpClient;
import java.net.http.WebSocket;

public class WebSocketClientForEnv extends WebSocketClient{
    public WebSocketClientForEnv(TDUCEnvMirror mainClass){
        super(mainClass);
    }
}
