package com.example.user.a20170118_bluetooth30;

/**
 * Created by USER on 2017/1/18.
 */

public class CCommand {
    public static double Rssi_n=2.037219;
    public static double Rssi_A=65.53125;
    public static double Rssi_D1=0;
    public static double Rssi_D2=0;
    public static double Rssi_D3=0;
    public static double Rssi_D4=0;
    public static double Rssi_D5=0;

    public final char Conf_ACK_head = 0xAA;
    public final char Conf_ACK_Addr = 0x00;
    public final char Conf_cmd = 0x03;
    public final char ACK_cmd = 0xFE;
    public final char Conf_ACK_length = 0x06;

    // Data Format: 閬�葫皞怨����誘�撘�
    public final char Temperature_head = 0x50;
    public final char Temperature_Addr = 0xD9;
    public final char Temperature_cmd = 0x55;
    public final char Temperature_length = 0x06;

    public final char Result_head = 0x50;
    public final char Result_Cnt_cmd = 0xBB;
    public final char Result_RTc_cmd = 0x99;
    public final char Result_length = 0x06;

    // Data Format: 閬�葫 BT idle ���誘�撘�
    public final char BT_idle_cmd_head = 0x50;
    public final char BT_idle_cmd_Addr = 0xD9;
    public final char Result_BT_ACK_Addr = 0xD5;
    public final char BT_idle_cmd_cmd = 0xF9;
    public final char BT_idle_ACK_cmd = 0x6D;
    public final char BT_idle_cmd_length = 0x06;

    public final char BT_idle_cmd_data0 = 0xCD;
    public final char BT_idle_cmd_data1 = 0x25;
    public final char BT_idle_cmd_data2 = 0xCA;
    public final char BT_idle_cmd_data3 = 0x0D;

    //Data Format: 閬����� BT RSSI ���誘�撘�
    public final char BT_RSSI_cmd_head = 0x50;
    public final char BT_RSSI_cmd_Addr = 0xD9;
    //public static final char BT_RSSI_ACK_Addr = 0xD5;	//��esult_BT_ACK_Addr = 0xD5;
    public final char BT_RSSI_cmd_cmd = 0x81;
    public final char BT_RSSI_ACK_cmd = 0xB7;
    public final char BT_RSSI_cmd_length = 0x06;

    public final char BT_RSSI_cmd_data0 = 0xCD;
    public final char BT_RSSI_cmd_data1 = 0x67;
    public final char BT_RSSI_cmd_data2 = 0xCD;
    public final char BT_RSSI_cmd_data3 = 0x51;
    public final char BT_RSSI_cmd_data4 = 0xBD;
    public final char BT_RSSI_cmd_data5 = 0x3D;

    public int[] XOR_en_decryption = { 0x661DF934, 0xAEFD0000 };

    public byte[] RSSI_data_format = {
            (byte) 0xAA, (byte) 0x00, (byte) 0x02, (byte) 0x70
    };
}
