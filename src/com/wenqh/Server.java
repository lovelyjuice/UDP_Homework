package com.wenqh;

import java.io.*;
import java.net.*;

public class Server
{
    private int port = 8000;
    private DatagramSocket socket;

    public Server() throws IOException
    {
        socket = new DatagramSocket(port); //与本地的一个固定端口绑定
        System.out.println("服务器启动");
    }

    private void service() throws IOException
    {
        OutputStream dstFile = new FileOutputStream("D:\\destination.txt");
        int currentOrder;
        int preOrder=-1;
        while (true)
        {
            try
            {
                DatagramPacket packet = new DatagramPacket(new byte[516], 516);
                socket.receive(packet);  //接收来自任意一个EchoClient的数据报
                currentOrder = byteArrayToInt(packet.getData());
                System.out.println(currentOrder);
                DatagramPacket response;
                if(currentOrder==-200&&byteArrayToInt(cutByteArray(packet.getData(),4,4))==preOrder)
                {
                    response = new DatagramPacket(intToByteArray(-3), 4,packet.getSocketAddress());
                    socket.send(response);
                    break;
                }
                if(currentOrder!=preOrder+1)
                {
                    response=new DatagramPacket(intToByteArray(-2),4,packet.getSocketAddress());
                    socket.send(response);
                }
                else
                {
                    preOrder=currentOrder;
                    response=new DatagramPacket(intToByteArray(currentOrder),4,packet.getSocketAddress());
                    socket.send(response);
                    dstFile.write(packet.getData(),4,packet.getLength()-4);
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        dstFile.close();
    }

    public static void main(String args[]) throws IOException
    {
        new Server().service();
    }

    private static int byteArrayToInt(byte[] b)
    {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    private static byte[] intToByteArray(int a)
    {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    private static byte[] cutByteArray(byte[] data, int start, int length)
    {
        byte[] result = new byte[length];
        System.arraycopy(data, start, result, 0, length);
        return result;
    }
}

