package com.wenqh;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;

public class Client
{
    private String remoteHost = "localhost";
    private int remotePort = 8000;
    private DatagramSocket socket;
    private int lastSuccessOrder = -1;
    private boolean existFailed = false;
    private boolean transComplete = false;
    private int cwnd = 0;
    private int status1;

    public Client() throws IOException
    {
        this.socket = new DatagramSocket(); //与本地的任意一个UDP端口绑定
    }

    public static void main(String args[]) throws IOException
    {
        new Client().send();
    }

    public void send() throws IOException
    {
        int order = 0;
        int i = 0;
        try
        {
            InetAddress remoteIP = InetAddress.getByName(remoteHost);
            InputStream localReader = new FileInputStream("D:\\source.txt");
            byte[] outputData = new byte[512];
            ArrayList<byte[]> content = new ArrayList<>();
            int count;
            while ((count = localReader.read(outputData)) != -1)
            {
                if (count != outputData.length) outputData = cutByteArray(outputData, 0, count);
                content.add(outputData);
                outputData = new byte[512];
            }
            rcvThread recieve=new rcvThread();
            recieve.start();
            lableB:
            while (true)
            {
                for (; order < content.size(); order++)
                {
                    if (existFailed)
                    {
                        sleep(20);
                        order = lastSuccessOrder+1;
                    }
                    while (true)
                    {
                        if (cwnd > 4){
                            sleep(200);
                            cwnd--;
                            break;
                        }
                        else break;
                    }
                    byte[] packet = content.get(order);
                    DatagramPacket outputPacket = new DatagramPacket(addBytes(intToByteArray(order), packet), packet.length + 4, remoteIP, remotePort);
                    if(Math.random()<0.5) socket.send(outputPacket);  //给EchoServer发送数据报
                    cwnd++;
                }
            /*告诉server发完数据了，结束标志为 （order=-1,数据部分=最后一个数据包的order）；
                数据部分=最后一个数据包的order 的原因是为了防止这个“告知结束”的数据包在其他数据包之前到达
             */
                DatagramPacket outputPacket = new DatagramPacket(addBytes(intToByteArray(-200), intToByteArray(order - 1)), 8, remoteIP, remotePort);
                socket.send(outputPacket);
                while (true)
                {
                    i++;
                    if (i == 100) break lableB;
                    if (!transComplete)
                    {
                        sleep(200);
                        order = lastSuccessOrder;
                        continue lableB;
                    }
                    break lableB;
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } finally
        {
            socket.close();
        }
    }

    class rcvThread extends Thread
    {
        DatagramPacket packet = new DatagramPacket(new byte[512], 512);

        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    socket.receive(packet);
                    int status = byteArrayToInt(packet.getData());
                    status1=status;
                    if (status >= 0)
                    {
                        lastSuccessOrder = status;
                        existFailed = false;
                        cwnd--;
                    }
                    else if (status == -2)
                    {
                        existFailed = true;
                        cwnd--;
                    }
                    else if (status == -3)
                    {
                        existFailed = false;
                        transComplete = true;
                        cwnd--;
                    }
                }
            } catch (SocketException e){}
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
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

    public static byte[] cutByteArray(byte[] data, int start, int length)
    {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) result[i] = data[start + i];
        return result;
    }

    public static byte[] addBytes(byte[] data1, byte[] data2)
    {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }
}




