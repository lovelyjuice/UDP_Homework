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

    public void service() throws IOException
    {
        OutputStream dstFile = new FileOutputStream("D:\\destination.txt");
        int currentOrder;
        int preOrder=-1;
        lable1:
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
                    break lable1;
                }
                if(currentOrder!=preOrder+1)
                {
                    currentOrder--;
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

    public static int byteArrayToInt(byte[] b)
    {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a)
    {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static byte[] addBytes(byte[] data1, byte[] data2)
    {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    public static byte[] cutByteArray(byte[] data,int start, int length)
    {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) result[i] = data[start + i];
        return result;
    }
}

