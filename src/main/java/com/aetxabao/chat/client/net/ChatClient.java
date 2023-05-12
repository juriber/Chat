package com.aetxabao.chat.client.net;

import com.aetxabao.chat.client.controllers.AController;
import com.aetxabao.chat.client.model.msg.MsgLogin;
import com.aetxabao.chat.client.model.msg.MsgPayload;
import com.aetxabao.chat.client.terminal.ChatClientHandlerTerm;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.ConnectException;
import java.util.Scanner;

import static com.aetxabao.chat.client.consts.AppConsts.*;
import static com.aetxabao.chat.client.consts.AppConsts.MSGTXT;

public class ChatClient {

    private String host;
    private int port;
    private String username;
    private String password;
    private EventLoopGroup group;
    private ChannelFuture f;
    private ChatClientHandler chatClientHandler;
    private boolean authorized = false;
    private boolean connected = false;

    private final Scanner scanner = new Scanner(System.in);

    private AController controller = null;

    public void setController(AController controller) {
        this.controller = controller;
    }

    public boolean writeInTerm() throws InterruptedException, JsonProcessingException {
        String input = scanner.nextLine();
        sendTxt(input);
        if (input.equalsIgnoreCase("exit") ||
                input.equalsIgnoreCase("quit") ||
                input.equalsIgnoreCase("bye")  ||
                input.equalsIgnoreCase("adios")  ||
                input.equalsIgnoreCase("agur") )  {
            return false;
        }
        return true;
    }

    public void connect(String host, int port) throws InterruptedException, ConnectException {
        group = new NioEventLoopGroup();
        try {
            chatClientHandler = new ChatClientHandler(this);
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());
                            p.addLast(chatClientHandler);
                        }
                    });
            f = b.connect(host, port).sync();
            connected = true;
        } catch (InterruptedException e) {
            close();
            throw e;
        } catch (Exception e) {
            close();
            throw new ConnectException();
        }
    }

    private void send(String text) throws InterruptedException {
        Channel channel = f.sync().channel();
        channel.writeAndFlush(text);
        channel.flush();
    }

    public void login(String username, String password) throws JsonProcessingException, InterruptedException {
        MsgLogin msgLogin = new MsgLogin("LOGIN", username, password);
        String json = msgLogin.toJson();
        send(json);
    }

    public void sendTxt(String txt) throws InterruptedException, JsonProcessingException {
        MsgPayload msgPayload = new MsgPayload(MSGTXT, username + " -> " + txt);
        String json = msgPayload.toJson();
        send(json);
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean value, String txt){
        authorized = value;
        System.out.println(txt);
        controller.loginResponse(value, txt);
    }

    public void receivedText(String txt){
        System.out.println(txt);
    }

    public void disconnected(){
        System.out.println("Disconnected client.\nIf necessary press ENTER to finish.");
        close();
    }

    public void close() {
        connected = false;
        try{
            f.channel().closeFuture();
        }finally {
            group.shutdownGracefully();
        }
    }

}