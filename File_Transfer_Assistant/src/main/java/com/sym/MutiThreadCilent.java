package com.sym;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多线程聊天室客户端
 */

class ReadFromIn{
    public static  String readFromSystemIn(int num){
        synchronized (System.in) {
            switch (num) {
                case 1:
                    System.out.println("您收到一条文件信息是否接收：Y：接收 N：丢弃");
                    break;
                case 2:
                    System.out.println("请指定文件存储路径");
                    break;
                case 3:
                    break;
                default:
                    break;
            }
            Scanner in = new Scanner(System.in);
            if (in.hasNext()) {
                return in.nextLine();
            }
        }
        return "";
    }
}
class ReadFromServer implements Runnable {
    private Socket cilent;

    ReadFromServer(Socket cilent) {
        this.cilent = cilent;
    }

    @Override
    public void run() {
        Scanner readFromServer = null;
        try {
            readFromServer = new Scanner(cilent.getInputStream(),"UTF-8");
            readFromServer.useDelimiter("\n");
            while (true) {
                if (readFromServer.hasNext()) {
                    String str = readFromServer.nextLine();
                    if (str.contains("=") && str.split(":")[1].startsWith("F=")) {
                        String words = str.split("=")[1];
                            String change = ReadFromIn.readFromSystemIn(1);
                            if (change.equals("Y")) {
                                //文件写入
                                String path = "";
                                path = ReadFromIn.readFromSystemIn(2);
                                write(words, path);
                                System.out.println("文件写入成功");
                                } else {
                                    continue;
                                }
                    } else{
                        System.out.println(str);
                    }
                }
                if (cilent.isClosed()) {
                    System.out.println("客户端已经关闭");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readFromServer.close();
        }
    }
    private void write(String words, String path) {
        String[] paths = path.split("\\\\");
        path = "";
        for (int i = 0; i < paths.length - 1; i++) {
            path += paths[i] + File.separator;
        }
        path += paths[paths.length - 1];
        byte[] bytes = new byte[0];
        try {
            bytes = words.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(path+"："+words);
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            if (file.createNewFile()) {
                    OutputStream writeToStream = new FileOutputStream(file);
                    writeToStream.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class SendToServer implements Runnable {
        private Socket cilent;
        SendToServer(Socket cilent) {
            this.cilent = cilent;
        }

        @Override
        public void run() {
            Scanner in = new Scanner(System.in);
            in.useDelimiter("\n");
            PrintStream sendToServer = null;
            try {
                sendToServer = new PrintStream(cilent.getOutputStream(), true, "UTF-8");
                while (true) {
                    System.out.println("在完成注册后请输入要发送的内容或文件路径...");
                    String str = "";
                    if(in.hasNext()){
                        str = in.nextLine();
                    }
                    if (str.equals("C") == true) {
                        break;
                    }
                    if(!str.startsWith("G")&&!str.startsWith("P")&&!str.startsWith("userName")){
                        continue;
                    }
                    if ((str.startsWith("G:") && str.split(":")[1].startsWith("F=")) || (str.startsWith("P:") && str.split("-")[1].startsWith("F="))) {
                        String path = str.substring(str.split("=").length + 2);
                        String[] paths = path.split("\\\\");
                        path = "";
                        for (int i = 0; i < paths.length - 1; i++) {
                            path += paths[i] + File.separator;
                        }
                        path += paths[paths.length - 1];
                        File file = new File(path);
                        if (file.exists()) {
                            InputStream readFromFile = new FileInputStream(file);
                            byte[] bytes = new byte[1024];
                            int ret = 1;
                            String words = "";
                            while (ret > 0) {
                                ret = readFromFile.read(bytes);
                                if (ret > 1) {
                                    words += new String(bytes, 0, ret,"UTF-8");
                                }
                            }
                            str = str.substring(0, str.split("=").length + 2) + words;
                        } else {
                            System.out.println("该文件不存在");
                            continue;
                        }
                    }else {
                        System.out.println(str);
                    }
                    sendToServer.println(str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                sendToServer.close();
                try {
                    cilent.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
}

public class MutiThreadCilent {
    public static void main(String[] args) {
            Socket client = null;
            try {
                client = new Socket("127.0.0.1", 6666);
                Thread readFromServer = new Thread(new ReadFromServer(client));
                Thread sendToServer = new Thread(new SendToServer(client));
                readFromServer.start();
                sendToServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}


