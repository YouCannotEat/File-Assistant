package com.sym;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多线程聊天室服务器端
 */
public class MutiThreadServer {
    private static Map<String,Socket> clientMap = new ConcurrentHashMap<>();
    private static class ExecuteClient implements Runnable {
        private Socket client;
        private String userName;
        public ExecuteClient(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            Scanner readFromClient = null;
            PrintStream sendToClient = null;
            try {
                readFromClient = new Scanner(client.getInputStream());
                String strFromClient;
                sendToClient = new PrintStream(client.getOutputStream(), true, "UTF-8");
                sendToClient.println("请首先完成登录注册，格式为 userName：用户名");
                boolean flag = false;
                while (true) {
                    if (readFromClient.hasNext()) {
                        strFromClient = readFromClient.nextLine();

                        Pattern pattern = Pattern.compile("\r");
                        Matcher matcher = pattern.matcher(strFromClient);
                        strFromClient = matcher.replaceAll("");

                        if (flag == false && strFromClient.startsWith("userName:")) {
                            //注册进入聊天室
                            userName = strFromClient.substring(
                                    strFromClient.split(":")[0].length() + 1);
                            if (registerUser() == true) {
                                flag = true;
                                sendToClient.println("用户注册登录成功！\n" +
                                        "群发消息格式：G:消息内容/F=文件\n" +
                                        "私法消息格式为：P:用户名-消息内容/F=文件");
                                groupChat("服务器", "用户" + userName + "上线了!");
                            } else {
                                flag = false;
                                sendToClient.println("异地重复登录警告！请重新输入");
                            }
                        }else if (flag == true && strFromClient.startsWith("G:")) {
                            //群聊
                            String msg = strFromClient.substring(
                                    strFromClient.split(":")[0].length()+1);
                            groupChat(userName,msg);

                        } else if (flag == true && strFromClient.startsWith("P:")) {
                            //私聊
                            String temp = strFromClient.substring(
                                    strFromClient.split(":")[0].length()+1);
                            String msg = temp.substring(temp.split("-")[0].length()+1);
                            if(privateChat(temp.split("-")[0],msg)==false){
                                sendToClient.println("该用户未在线");
                            }else{
                                sendToClient.println("发出消息"+msg);
                            }
                        } else if (flag == true && strFromClient.equals("C")) {
                            String userName = null;
                            for (String key : clientMap.keySet()) {
                                if (clientMap.get(key).equals(client) == true) {
                                    userName = key;
                                    break;
                                }
                            }
                            clientMap.remove(userName);
                            System.out.println("用户" + userName + "退出下线");
                            groupChat("服务器","用户" + userName + "退出下线！");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                sendToClient.close();
                readFromClient.close();
            }
        }

        private boolean registerUser() {
            if (clientMap.get(userName) != null) {
                if (clientMap.get(userName).getPort() != client.getPort()) {
                    return false;
                } else return true;
            }
            System.out.println("用户姓名为" + userName + ",端口号为" + client.getPort());
            clientMap.put(userName, client);
            return true;
        }
        private boolean groupChat(String name,String msg) {
            String sendmsg = name + "发来群消息:" + msg;
            for(String user:clientMap.keySet()){
                Socket socket = clientMap.get(user);
                PrintStream sendToClient = null;
                try {
                    sendToClient = new PrintStream(socket.getOutputStream(),true,"UTF-8");
                    sendToClient.println(sendmsg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        private boolean privateChat(String name,String msg){
            Socket socket = clientMap.get(name);
            if(socket == null) return false;
            PrintStream printStream = null;
            try {
                printStream = new PrintStream(socket.getOutputStream(),true,"UTF-8");
                printStream.println("用户"+userName+"发来消息:"+msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        ExecutorService executorService = null;
        try {
            serverSocket = new ServerSocket(6666);
            Socket client = null;
            executorService = Executors.newFixedThreadPool(5);
            while(true){
                System.out.println("等待客户端连接...");
                client = serverSocket.accept();
                System.out.println("有新的客户端连接，端口号为："+client.getPort());
                executorService.submit(new ExecuteClient(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            executorService.shutdown();
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
