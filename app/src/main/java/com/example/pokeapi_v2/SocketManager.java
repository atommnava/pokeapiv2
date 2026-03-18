package com.example.pokeapi_v2;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

public class SocketManager {

    private static Socket socket;

    public static Socket getSocket() {
        if (socket == null) {
            try {
                socket = IO.socket("http://10.0.2.2:3000");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return socket;
    }
}