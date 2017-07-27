package com.example.user.a20170118_bluetooth30;

import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;


public class MainActivity2 extends Thread {
    static Object lock;
    static boolean pause = false;
    static DataInputStream dataIn;
    static short linkLose = 0;    //-1連線中
    static short linkTimes = 0;
    byte[] ReadData = new byte[40];
    byte[] ReadData2 = new byte[40];
    byte[] ReadData3 = new byte[40];
    static boolean isTmp1In = false;
    boolean lastIs66 = false;
    int length = 0;

    MainActivity2(DataInputStream datainput) {
        dataIn = datainput;
        linkLose = -1;
    }

    public void run() {
        // TODO Auto-generated method stub
        super.run();
        String head = "", cmd = "";
        lock = new Object();
        while (MainActivity.isExist) {
            length = 0;
            try {
                if (MainActivity.IsShowLogCat)
                    Log.e("receive", "接收1");
                length = MainActivity.inStream.read(ReadData);

                String strTmp = "";
                for (int i = 0; i < ReadData.length; i++) {
                    strTmp += Integer.toHexString(ReadData[i]) + ", ";

                }
                CAction.errorMsg = strTmp;
                if (MainActivity.IsShowLogCat)
                    Log.e("Data", strTmp);

//				Log.e("receive", "接收1.5, "+Integer.toHexString(ReadData[0])+", "+Integer.toHexString(ReadData[1]));
                head = Integer.toHexString(ReadData[0]).toUpperCase();
                cmd = Integer.toHexString(ReadData[1]).toUpperCase();
                if (MainActivity.IsShowLogCat)
                    Log.e("head", head + ", " + cmd);
                boolean is6611 = false;
                if ((head.equals("66"))) {
                    for (int i = length; i < 2; ) {
                        length = MainActivity.inStream.read(ReadData, i, 2 - i);
                        i += length;
                        length = i;
                    }
                    if (Integer.toHexString(ReadData[1]).equals("11")) {
                        for (int i = length; i < 40; ) {
                            length = MainActivity.inStream.read(ReadData, i, ReadData.length - i);
                            i += length;
                        }
                    }
                    CAction.ExtraTmp[0] = ReadData[32];
                    CAction.ExtraTmp[1] = ReadData[33];
                    CAction.ExtraTmp[2] = ReadData[34];
                    CAction.ExtraTmp[3] = ReadData[35];
                    CAction.ExtraTmp[4] = ReadData[36];
                    CAction.ExtraTmp[5] = ReadData[37];
                    CAction.ExtraTmp[6] = ReadData[38];
                    CAction.ExtraTmp[7] = ReadData[39];

                    CAction.ExtraTmp[8] = ReadData[2];
                    CAction.ExtraTmp[9] = ReadData[3];
                    CAction.ExtraTmp[10] = ReadData[10];
                    CAction.ExtraTmp[11] = ReadData[11];


                    is6611 = true;

                } else if (head.equals("FFFFFFAA")) {
                    for (int i = length; i < 12; ) {
                        if (MainActivity.IsShowLogCat)
                            Log.e("receive", "set");
                        length = MainActivity.inStream.read(ReadData, i, 12 - i);
                        i += length;
                    }
                } else if (head.equals("50")) {
                    if (MainActivity.IsShowLogCat)
                        Log.e("receive", "50_1");
                    for (int i = length; i < 12; ) {
                        length = MainActivity.inStream.read(ReadData, i, 12 - i);
                        i += length;
                        length = i;
                    }
                    if (Integer.toHexString(ReadData[10]).toUpperCase().equals("FFFFFFBB")) {
                        if (MainActivity.IsShowLogCat)
                            Log.e("receive", "50_2");
                        for (int i = length; i < 24; ) {
                            length = MainActivity.inStream.read(ReadData, i, 24 - i);
                            i += length;
                        }
//						for(int i=0;i<12;i++){
//							CAction.Data_tbl_tmp2[i] = ReadData[i+12];
//						}
                    } else if (Integer.toHexString(ReadData[10]).toUpperCase().equals("6D")) {
                        if (MainActivity.IsShowLogCat)
                            Log.e("receive", "50_3");
                        for (int i = length; i < 12; ) {
                            length = MainActivity.inStream.read(ReadData, i, 12 - i);
                            i += length;
                        }
                    }
                }
                strTmp = "";
                for (int i = 0; i < ReadData.length; i++) {
                    strTmp += Integer.toHexString(ReadData[i]) + ", ";
                }
                if (MainActivity.IsShowLogCat)
                    Log.e("AllData", strTmp + ", " + is6611);

                if (!is6611) {
                    for (int i = 0; i < 12; i++) {
                        CAction.Data_tbl[i] = ReadData[i];
                        CAction.File_array[i] = Integer.toHexString(ReadData[i]);
                        CAction.File_array_tmp2[i] = Integer.toHexString(ReadData[i + 12]);
                        CAction.Data_tbl_tmp2[i] = ReadData[i + 12];
                        CAction.CompareData[i] = ReadData[i];

                    }
                    head = Integer.toHexString(CAction.Data_tbl[0]);
                    cmd = Integer.toHexString(CAction.Data_tbl[10]);
                } else {
                    head = Integer.toHexString(ReadData[0]);
                    cmd = Integer.toHexString(ReadData[1]);
                }


                if (head.length() > 2) {
                    head = "0x" + head.substring(head.length() - 2);
                } else {
                    head = "0x" + head;
                }
                if (cmd.length() > 2) {
                    cmd = "0x" + cmd.substring(cmd.length() - 2);
                } else {
                    cmd = "0x" + cmd;
                }
                if (MainActivity.IsShowLogCat)
                    Log.e("receive", "接收2, " + head + ", " + cmd);

                CAction.errorMsg = "";
                if (MainActivity.IsShowLogCat)
                    Log.e("0xhead", head + ", " + cmd);
                Message message = new Message();
                if (head.equals("0x66") && cmd.equals("0x11")) {
//					Log.e("extra", "0");
                    message.what = 14;
                    MainActivity.hReceiver.sendMessage(message);
                    lastIs66 = false;
                } else if (head.equals("0x66") && (cmd.equals("0x30") || cmd.equals("0x31"))) {
                    if (Integer.toHexString(ReadData[6]).equals("66") && Integer.toHexString(ReadData[7]).equals("ffffffee")) {
                        if (MainActivity.IsShowLogCat)
                            Log.e("extra", "1");
                        CAction.ExtraTmp[2] = ReadData[8];
                        CAction.ExtraTmp[3] = ReadData[9];
                        message.what = 14;
                        MainActivity.hReceiver.sendMessage(message);
                    }
                    lastIs66 = false;
                } else if (head.equals("0x66") && cmd.equals("0x11")) {
                    if ((Integer.toHexString(ReadData[10]).equals("66") && (Integer.toHexString(ReadData[11]).equals("30") || Integer.toHexString(ReadData[11]).equals("31"))) &&
                            (Integer.toHexString(ReadData[16]).equals("66") && Integer.toHexString(ReadData[17]).equals("ffffffee"))) {
                        if (MainActivity.IsShowLogCat)
                            Log.e("extra", "2");
                        CAction.ExtraTmp[2] = ReadData[18];
                        CAction.ExtraTmp[3] = ReadData[19];
                        message.what = 14;
                        MainActivity.hReceiver.sendMessage(message);
                    }
                    lastIs66 = false;
                } else if (head.equals("0xaa") || head.equals("0xAA")) {
                    isTmp1In = false;
                    message.what = 1;
                    MainActivity.hReceiver.sendMessage(message);
                    lastIs66 = false;
//					receiveSet();
                } else if ((head.equals("0x50") || head.equals("0xd0")) && (cmd.equals("0xbb") || cmd.equals("0xBB"))) {
                    if (MainActivity.IsShowLogCat)
                        Log.e("receive", "接收溫度1");
                    isTmp1In = true;
                    message.what = 2;
                    MainActivity.hReceiver.sendMessage(message);
                    lastIs66 = false;
//							for(int i=0;i<12;i++){
//								CAction.File_array[i] = Integer.toHexString(ReadData[i]);
//								CAction.File_array_tmp2[i+12] = Integer.toHexString(ReadData[i+12]);
//							}

//							receiveTmp();
                } else if (head.equals("0x50") && cmd.equals("0x99")) {
                    if (MainActivity.IsShowLogCat)
                        Log.e("receive", "接收溫度2");
                    isTmp1In = false;
                    lastIs66 = false;
//							message.what=2;
//							ConnectActivity.hReceiver.sendMessage(message);
//							try {
//								Thread.sleep(300);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							Message message2=new Message();
                    message.what = 3;
                    MainActivity.hReceiver.sendMessage(message);
//							receiveTmp2();
                }
                for (int i = 0; i < ReadData.length; i++) {
                    ReadData[i] = 0xffffffff;
                    ReadData2[i] = 0;
                }

            } catch (IOException e) {
            }

        }
    }
}
