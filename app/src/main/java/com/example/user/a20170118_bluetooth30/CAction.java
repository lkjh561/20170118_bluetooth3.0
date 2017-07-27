package com.example.user.a20170118_bluetooth30;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;


public class CAction {
    public static int isSend=0;// 0 不發信       1 錯誤當下發信      2 關於頁面回報發信
    int extraCount=0;
    CCommand command;
    Activity activity;
    static int lowPowerEnd=0;
    static byte[] SetCommand = new byte[4];
    static byte[] ReadData = new byte[4];
    static byte[] Data_tbl = new byte[12];
    static byte[] Data_tbl_tmp2 = new byte[12];
    static byte[] CompareData = new byte[12];
    static byte[] ExtraTmp = new byte[20];
    static String[] File_array = new String[8125];
    static String[] File_array_tmp2 = new String[8125];

    private int User_ID_number;

    // 計算 CRC-16 時使用
    public int[] Source_Data = new int[3];

    String[] BP_array = new String[3];
    short setting_loop = 0;
    short memory_loop=0;
    short isSet=-1;
    static short isTmp=-1;
    boolean tmpFirst=true;
    static short isSleep=-1;
    boolean configurate_flag;
    int linkTimes=0;
    static short loopTimes=0;
    String head, cmd;
    /*final */static short loopLimit=3;

    static boolean goSleep=false;
    Thread tSleep, tTmp;
    static boolean tmp1IsRight=true;
    static boolean isGoSet=false;
    static boolean isGoTmp=false;
    static boolean isGoSleep=false;
    static boolean isGoRssi=false;
    int alreadyGetTmp=0;//0 未要溫   1有要溫  2要溫且收溫成功


    DataOutputStream outStream = null;
    DataInputStream inStream = null;
    Handler h,h2,hReceiver;
    Runnable r1, r2, rReceiver;
    int countdown=0;
    float extraTmp=0;

    int extraTimes=0;
    int tryTimes=0;
    int commonTimes=0;

    static String errorMsg="";

    private final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805f9b34fb");

    public CAction(DataOutputStream dos, DataInputStream dis, Activity activity) {

        command = new CCommand();
        outStream = dos;
        inStream = dis;
        this.activity = activity;

    }
    public void doConfirm_set() {
        String BP_array[] = { "", "", "", "", "" };
        String string_temp;
        int aaa[] = { 0, 0, 0, 0, 0 };
        int CRC_result, loop_cnt;
        char bit_cnt;
        memory_loop=0;
        setting_loop=0;

        try {
            Log.e("IN","in");
            // 計算測溫之時間間隔
            MainActivity.time_hr_min_sec = (((MainActivity.time_hour * 60) + MainActivity.time_minute) * 60 + MainActivity.time_second);
            // 儲存警示溫度之設定值
            int temp2_sound=MainActivity.Alarm2_temperature;
//			MainActivity.Alarm2_temperature = (MainActivity.Alarm2_temperature & 0x7FFF);
           /* if(MainActivity.audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0){ //非靜音
                temp2_sound = (temp2_sound & 0x7FFF);
            }else{
                temp2_sound = (temp2_sound | 0x8000);

            }*/
            Log.e("IN2","in");
            int time_hr_min_secTmp=MainActivity.time_hr_min_sec;
            Log.i("search:"+MainActivity.time_hr_min_sec, "2222");
            if(time_hr_min_secTmp>=60){
                time_hr_min_secTmp=MainActivity.time_hr_min_sec-18;
            }else{
//				time_hr_min_secTmp=MainActivity.time_hr_min_sec-1;
                time_hr_min_secTmp=MainActivity.time_hr_min_sec;
            }


            // 組合計算 CRC-16 所需的資料
            Source_Data[0] = (command.Conf_ACK_head << 24) + (command.Conf_ACK_Addr << 16)
                    + (command.Conf_cmd << 8) + (command.Conf_ACK_length << 0);
            Source_Data[1] = (time_hr_min_secTmp << 16) + MainActivity.Alarm1_temperature;
            Source_Data[2] = (temp2_sound << 16);
            Log.e("IN3","in");
            String setData123="";
            for(int i=0;i<3;i++){
                setData123+=Integer.toHexString(Source_Data[i])+", ";
            }
            Log.e("setData123", setData123);
            // }
            if(MainActivity.IsShowLogCat)
                Log.e("totalTime", time_hr_min_secTmp+"");
            // Step2: 計算 CRC-16
            bit_cnt = 80;
            CRC_result = CRC16_Check(Source_Data, bit_cnt);
            Source_Data[2] += CRC_result;
            User_ID_number = CRC_result;

            // 顯示輸入之各項設定值
            aaa[0] = ((Source_Data[1] >> 16) & 0xFFFF); // Encode-1
            aaa[1] = (Source_Data[1] & 0xFFFF); // Encode-2
            aaa[2] = ((Source_Data[2] >> 16) & 0xFFFF); // Encode-3
            aaa[3] = (User_ID_number & 0xFFFF); // CRC-16

            BP_array[0] = Integer.toString(aaa[0]) + "\t\t"
                    + Integer.toString(aaa[1]) + "\t\t"
                    + Integer.toString(aaa[2]) + "\t\t"
                    + Integer.toString(aaa[3]);

            for (loop_cnt = 0; loop_cnt < 4; loop_cnt++) {
                if (aaa[loop_cnt] < 16) {
                    string_temp = "_000";
                } else if (aaa[loop_cnt] < 256) {
                    string_temp = "_00";
                } else if (aaa[loop_cnt] < 4096) {
                    string_temp = "_0";
                } else {// aaa[loop_cnt] >= 4096
                    string_temp = "_";
                }

                BP_array[loop_cnt] = string_temp
                        + Integer.toHexString(aaa[loop_cnt]).toUpperCase();
            }
            Log.e("IN4","in");
            string_temp = BP_array[0] + BP_array[1] + BP_array[2] + BP_array[3];
            BP_array[0] = string_temp;

            string_temp = Integer.toHexString(Source_Data[0]).toUpperCase()
                    + string_temp;

            // Step3: 對指令格式的資料部分做加密
            Source_Data[1] ^= command.XOR_en_decryption[0];
            Source_Data[2] ^= command.XOR_en_decryption[1];

        } catch (Exception obj) {
            obj.printStackTrace();
        }
        Log.e("IN5","in");
    }

    public void transmit_set() {
        if(MainActivity.IsShowLogCat)
            Log.e("SET", "要求設定"+isSet);
        loopTimes++;
        for(int i=0;i<12;i++){
            File_array[i]="";
            File_array_tmp2[i]="";
        }
        setting_loop++;
        isSet=1;
        // 清除旗標
        configurate_flag = true;

        for (int i = 0; i < 3; i++) {
            BP_array[i] = "";
        }

        for (int i = 0; i < 12; i++) {
            Data_tbl[i] = 0;
        }

        // do{ //Step4: 重新排列指令格式 - 準備要傳送到 uC 的設定資料
        // 設定參數的指令: AA00_CRC_Time_Alarm(Hi)_Alarm(Lo)_0306
        // 要求測溫數據的指令: 50D9_CRC_50D9_5550_D955_5506
        // 要求 BT idle 的指令: 50D9_CRC_Time_CD25_CA0D_F906
        // head
        CompareData[0] = (byte) ((Source_Data[0] & 0xFF000000) >> 24);
        // addr.
        CompareData[1] = (byte) ((Source_Data[0] & 0x00FF0000) >> 16);
        // CRC-16
        CompareData[10] = (byte) ((Source_Data[0] & 0x0000FF00) >> 8);
        CompareData[11] = (byte) ((Source_Data[0] & 0x000000FF) >> 0);
        // Encode1
        CompareData[4] = (byte) ((Source_Data[1] & 0xFF000000) >> 24);
        CompareData[5] = (byte) ((Source_Data[1] & 0x00FF0000) >> 16);
        // Encode2
        CompareData[6] = (byte) ((Source_Data[1] & 0x0000FF00) >> 8);
        CompareData[7] = (byte) ((Source_Data[1] & 0x000000FF) >> 0);
        // Encode3
        CompareData[8] = (byte) ((Source_Data[2] & 0xFF000000) >> 24);
        CompareData[9] = (byte) ((Source_Data[2] & 0x00FF0000) >> 16);
        // cmd
        CompareData[2] = (byte) ((Source_Data[2] & 0x0000FF00) >> 8);
        // length
        CompareData[3] = (byte) ((Source_Data[2] & 0x000000FF) >> 0);

        // 開始傳送資料:
        // 1. 傳送喚醒 uC 的 0
        SetCommand[0] = 0;
        SetCommand[1] = 0;
        SetCommand[2] = 0;
        SetCommand[3] = 0;
        byte[] zero = new byte[] { 1 };
        try {
            MainActivity.outStream.write(zero, 0, 1);
            if(MainActivity.outStream2!=null){
                MainActivity.outStream2.write(zero, 0, 1);
            }

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }
        try {
            MainActivity.outStream.write(zero, 0, 1);
            if(MainActivity.outStream2!=null){
                MainActivity.outStream2.write(zero, 0, 1);
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Log.e("step", "寫完0");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isSet=0;
        try {
            MainActivity.outStream.write(CompareData, 0, 12);
            String setData="";
            for(int i=0;i<12;i++){
                setData+=Integer.toHexString(CompareData[i])+", ";
            }
            Log.e("setData", setData);
            if(MainActivity.outStream2!=null){
                MainActivity.outStream2.write(CompareData, 0, 12);
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e("step", "傳送命令封包");
        head = Integer.toHexString(CompareData[0]);
        cmd = Integer.toHexString(CompareData[10]);
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
        memory_loop=setting_loop;
        if(isGoSet){	//如果已經在設定的狀態下


        }else{

            isGoSet=true;

            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    do{
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if(loopTimes>loopLimit){
                            if(MainActivity.IsShowLogCat)
                                Log.e("LoopTimes", "設定失敗超過"+loopLimit+"次");
                            return;
                        }
                        if(isSet==0){
                            if(MainActivity.IsShowLogCat)
                                Log.e("isSet", isSet+"");
                            MainActivity.hWorker.post(new Runnable() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    transmit_set();
                                }
                            });
                        }

                    }while(isSet!=-1 && MainActivity.isExist);
                }
            }).start();
//			});
        }
    }
    public void doConfirm_tmp() {
        String BP_array[] = { "", "", "", "", "" };
        String string_temp;
        int aaa[] = { 0, 0, 0, 0, 0 };
        int CRC_result, loop_cnt;
        char bit_cnt;

//		msg1 = "";
        memory_loop=0;
        setting_loop=0;

        try {

            // 組合計算 CRC-16 所需的資料
            Source_Data[0] = (command.Temperature_head << 24)
                    + (command.Temperature_Addr << 16) + (command.Temperature_cmd << 8)
                    + (command.Temperature_length << 0);

            Source_Data[1] = (command.Temperature_head << 24)
                    + (command.Temperature_Addr << 16) + (command.Temperature_cmd << 8)
                    + (command.Temperature_head << 0);

            Source_Data[2] = (command.Temperature_Addr << 24) + (command.Temperature_cmd << 16);
            String setData456="";
            for(int i=0;i<3;i++){
                setData456+=Integer.toHexString(Source_Data[i])+", ";
            }
            Log.e("setData456", setData456);
            // Step2: 計算 CRC-16
            bit_cnt = 80;
            CRC_result = CRC16_Check(Source_Data, bit_cnt);
            Source_Data[2] += CRC_result;

            // 顯示 CRC-16
            // tv1.append("\nCRC_result:"+ CRC_result);
//			msg1 += "CRC_result: " + CRC_result;
            User_ID_number = CRC_result;
            // field_hr_min_sec.setText(Integer.toString(MainActivity.time_hr_min_sec));

            // 顯示輸入之各項設定值
            aaa[0] = ((Source_Data[1] >> 16) & 0xFFFF); // Encode-1
            aaa[1] = (Source_Data[1] & 0xFFFF); // Encode-2
            aaa[2] = ((Source_Data[2] >> 16) & 0xFFFF); // Encode-3
            aaa[3] = (User_ID_number & 0xFFFF); // CRC-16

            BP_array[0] = Integer.toString(aaa[0]) + "\t\t"
                    + Integer.toString(aaa[1]) + "\t\t"
                    + Integer.toString(aaa[2]) + "\t\t"
                    + Integer.toString(aaa[3]);

            for (loop_cnt = 0; loop_cnt < 4; loop_cnt++) {
                if (aaa[loop_cnt] < 16) {
                    string_temp = "_000";
                } else if (aaa[loop_cnt] < 256) {
                    string_temp = "_00";
                } else if (aaa[loop_cnt] < 4096) {
                    string_temp = "_0";
                } else {// aaa[loop_cnt] >= 4096
                    string_temp = "_";
                }

                BP_array[loop_cnt] = string_temp
                        + Integer.toHexString(aaa[loop_cnt]).toUpperCase();
            }

            string_temp = BP_array[0] + BP_array[1] + BP_array[2] + BP_array[3];
            BP_array[0] = string_temp;

            string_temp = Integer.toHexString(Source_Data[0]).toUpperCase()
                    + string_temp;

            // Step3: 對指令格式的資料部分做加密
            Source_Data[1] ^= command.XOR_en_decryption[0];
            Source_Data[2] ^= command.XOR_en_decryption[1];

        } catch (Exception obj) {
            obj.printStackTrace();
        }

    }
    public void transmit_tmp() {
        tryTimes++;
        if(MainActivity.IsShowLogCat)
            Log.e("TMP", "要求溫度"+", "+tryTimes+", "+commonTimes+", "+extraTimes);
        setting_loop++;
        loopTimes=0;
        loopTimes++;
        for(int i=0;i<12;i++){
            File_array[i]="";
            File_array_tmp2[i]="";
        }

        // 清除旗標
        configurate_flag = true;

        for (int i = 0; i < 3; i++) {
            BP_array[i] = "";
        }

        for (int i = 0; i < 12; i++) {
            Data_tbl[i] = 0;
        }

        // Step4: 重新排列指令格式 - 準備要傳送到 uC 的設定資料
        // 設定參數的指令: AA00_CRC_Time_Alarm(Hi)_Alarm(Lo)_0306
        // 要求測溫數據的指令: 50D9_CRC_50D9_5550_D955_5506
        // 要求 BT idle 的指令: 50D9_CRC_Time_CD25_CA0D_F906
        // head
        CompareData[0] = (byte) ((Source_Data[0] & 0xFF000000) >> 24);
        // addr.
        CompareData[1] = (byte) ((Source_Data[0] & 0x00FF0000) >> 16);
        // CRC-16
        CompareData[10] = (byte) ((Source_Data[0] & 0x0000FF00) >> 8);
        CompareData[11] = (byte) ((Source_Data[0] & 0x000000FF) >> 0);
        // Encode1
        CompareData[4] = (byte) ((Source_Data[1] & 0xFF000000) >> 24);
        CompareData[5] = (byte) ((Source_Data[1] & 0x00FF0000) >> 16);
        // Encode2
        CompareData[6] = (byte) ((Source_Data[1] & 0x0000FF00) >> 8);
        CompareData[7] = (byte) ((Source_Data[1] & 0x000000FF) >> 0);
        // Encode3
        CompareData[8] = (byte) ((Source_Data[2] & 0xFF000000) >> 24);
        CompareData[9] = (byte) ((Source_Data[2] & 0x00FF0000) >> 16);
        // cmd
        CompareData[2] = (byte) ((Source_Data[2] & 0x0000FF00) >> 8);
        // length
        CompareData[3] = (byte) ((Source_Data[2] & 0x000000FF) >> 0);

//		Log.e("Source_Data ", Source_Data[0] + "");
        // 開始傳送資料:
        // 1. 傳送喚醒 uC 的 0
        SetCommand[0] = 0;
        SetCommand[1] = 0;
        SetCommand[2] = 0;
        SetCommand[3] = 0;
        if(MainActivity2.linkLose!=-1){return;}								   //如果斷線, 跳出
        byte[] zero = new byte[] { 1 };
//		byte[] zero = new byte[] { 0 };

        try {
            MainActivity.outStream.write(zero, 0, 1);
            if(MainActivity.outStream2!=null){
                MainActivity.outStream2.write(zero, 0, 1);
            }
            if(MainActivity.IsShowLogCat)
                Log.e("TMP", "傳要溫1");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            if(MainActivity.IsShowLogCat)
                Log.e("TMP", "傳要溫1.1");
            e1.printStackTrace();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }
        try {
            MainActivity.outStream.write(zero, 0, 1);
            if(MainActivity.outStream2!=null){
                MainActivity.outStream2.write(zero, 0, 1);
            }
            if(MainActivity.IsShowLogCat)
                Log.e("TMP", "傳要溫1");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            if(MainActivity.IsShowLogCat)
                Log.e("TMP", "傳要溫1.1");
            e1.printStackTrace();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//		}
        if(MainActivity2.linkLose!=-1){return;}								   //如果斷線, 跳出
        try {

            MainActivity.outStream.write(CompareData, 0, 12);
            if(MainActivity.outStream2!=null){
                MainActivity.outStream2.write(CompareData, 0, 12);
            }

//			MainActivity.outStream.write(AllSetCommand, 0, 13);
            if(MainActivity.IsShowLogCat)
                Log.e("TMP", "傳要溫12");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            if(MainActivity.IsShowLogCat)
                Log.e("TMP", "傳要溫12.12");
            e1.printStackTrace();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        head = Integer.toHexString(CompareData[0]);
        cmd = Integer.toHexString(CompareData[10]);
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
        memory_loop=setting_loop;
        if(MainActivity.IsShowLogCat)
            Log.e("test", "-3");
        isTmp=0;
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                if(isTmp!=-1 && MainActivity2.linkLose==-1){
                    Message message=new Message();
                    message.what=15;
                    MainActivity.hReceiver.sendMessage(message);

                }
            }
        }).start();

    }
    public int CRC16_Check(int CheckCRC[], char bits_cnt) {
        // int CRC_result;
        int CRC_Remainder;

        // u32 GF = 0x11021;
        int GF = 0x1021;
        int[] source_data = new int[4];
        char MSB_Decryption, count_loop;
        char i, j;// ,k,loop_cnt;

        i = 0;
        // loop_cnt = 0;

        GF <<= 16;
        count_loop = bits_cnt;

        source_data[0] = CheckCRC[0];
        source_data[1] = CheckCRC[1];
        source_data[2] = CheckCRC[2];
        source_data[3] = 0;

        // input: CheckCRC = Source_Data (12 bytes or 96 bits)
        for (i = count_loop; i > 0; i--) {
            // loop_cnt += 1;
            MSB_Decryption = (char) ((source_data[0] >> 31) & 0x01);

            source_data[0] <<= 1;
            if (MSB_Decryption == 1) {
                source_data[0] ^= GF;
            }

            for (j = 1; j < 4; j++) {
                if (((source_data[j] >> 31) & 0x01) == 1) {
                    source_data[j - 1] |= 1;
                }

                source_data[j] <<= 1;
            }
        }

        CRC_Remainder = ((source_data[0] >> 16) & 0x0000FFFF);

        return (CRC_Remainder);
    }
    void doALL(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if(MainActivity2.linkLose!=-1){return;}								   //如果斷線, 跳出
                doConfirm_set();														   //設置設定指令
                transmit_set();															   //傳送設定指令
                do{																		   //每0.1秒檢測是否設定成功
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }while(isSet!=-1 && MainActivity2.linkLose==-1);						   //設定中 && 連線中
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e3) {
                    // TODO Auto-generated catch block
                    e3.printStackTrace();
                }

                while(MainActivity.isExist && isSet==-1 && MainActivity2.linkLose==-1){  //程式存在 && 不是在設定
                    short tmpTimes=MainActivity2.linkTimes;								 //儲存現在連線的次數

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e3) {
                        // TODO Auto-generated catch block
                        e3.printStackTrace();
                    }
                    if(MainActivity2.linkLose!=-1){return;}								   //如果斷線, 跳出

                    doConfirm_tmp();														//設置要溫指令
                    transmit_tmp();															//傳送要溫指令
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                    }
                    try {
                        Thread.sleep(1200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public void receiveSet() {
//		msg1="";
        // Step2: 還原指令排列順序
        byte restore;

        Log.e("reIN","123456789");
        restore = Data_tbl[10];
        Data_tbl[10] = Data_tbl[2];
        Data_tbl[2] = restore;

        restore = CompareData[10];
        CompareData[10] = CompareData[2];
        CompareData[2] = restore;

        restore = Data_tbl[11];
        Data_tbl[11] = Data_tbl[3];
        Data_tbl[3] = restore;

        restore = CompareData[11];
        CompareData[11] = CompareData[3];
        CompareData[3] = restore;

        // 修正 CompareData array 的內容
        // if(set_param.isChecked()){ //指令內容為設定參數
        // 參數 ACK 指令: AA00_FE06_Time_Alarm(Hi)_Alarm(Lo)_CRC
        CompareData[2] = (byte) command.ACK_cmd;
        // }

        // Step3: 將收到的指令解密
        Data_tbl[4] ^= (byte) ((command.XOR_en_decryption[0] & 0xFF000000) >> 24);
        Data_tbl[5] ^= (byte) ((command.XOR_en_decryption[0] & 0x00FF0000) >> 16);
        Data_tbl[6] ^= (byte) ((command.XOR_en_decryption[0] & 0x0000FF00) >> 8);
        Data_tbl[7] ^= (byte) ((command.XOR_en_decryption[0] & 0x000000FF) >> 0);
        Data_tbl[8] ^= (byte) ((command.XOR_en_decryption[1] & 0xFF000000) >> 24);
        Data_tbl[9] ^= (byte) ((command.XOR_en_decryption[1] & 0x00FF0000) >> 16);

        CompareData[4] ^= (byte) ((command.XOR_en_decryption[0] & 0xFF000000) >> 24);
        CompareData[5] ^= (byte) ((command.XOR_en_decryption[0] & 0x00FF0000) >> 16);
        CompareData[6] ^= (byte) ((command.XOR_en_decryption[0] & 0x0000FF00) >> 8);
        CompareData[7] ^= (byte) ((command.XOR_en_decryption[0] & 0x000000FF) >> 0);
        CompareData[8] ^= (byte) ((command.XOR_en_decryption[1] & 0xFF000000) >> 24);
        CompareData[9] ^= (byte) ((command.XOR_en_decryption[1] & 0x00FF0000) >> 16);

        // 顯示解密後的 12 bytes 資料
        char loop_cnt;
        String receive_string = "0x";

        for (loop_cnt = 0; loop_cnt < 12; loop_cnt++) {
            if ((Data_tbl[loop_cnt] & 0xFF) < 16) {
                receive_string += "0";
            }

            receive_string += Integer.toHexString(Data_tbl[loop_cnt] & 0xFF)
                    .toUpperCase();

            if ((loop_cnt < 11) && ((loop_cnt % 4) == 3)) {
                receive_string += "_";
            }
        }

        String CRC_16_string;
        CRC_16_string = "Compare Data: 0x";
        for (int i = 0; i < 12; i++) {
            CRC_16_string += Integer.toHexString(CompareData[i] & 0xFF)
                    .toUpperCase();
        }

        // Step4: 計算 CRC-16
        int[] SourceData = new int[3];
        for (int i = 0; i < 3; i++) {
            SourceData[i] = (((int) (Data_tbl[(i * 4) + 0] & 0xFF)) << 24)
                    + (((int) (Data_tbl[(i * 4) + 1] & 0xFF)) << 16)
                    + (((int) (Data_tbl[(i * 4) + 2] & 0xFF)) << 8)
                    + (((int) (Data_tbl[(i * 4) + 3] & 0xFF)) << 0);
        }
        SourceData[2] &= 0xFFFF0000;

        char bit_cnt = 80;
        int CRC_result, compare_CRC;

        CRC_result = CRC16_Check(SourceData, bit_cnt);
        SourceData[2] += CRC_result;
        CRC_16_string = "0x"
                + Integer.toHexString(CRC_result & 0xFFFF).toUpperCase();

        compare_CRC = (((short) (Data_tbl[10] & 0xFF)) << 8)
                + (((short) (Data_tbl[11] & 0xFF)) << 0);

        configurate_flag = true;
        if (CRC_result != compare_CRC) {
            configurate_flag = false;
        }

        for (int i = 0; i < 3; i++) {
            if ((SourceData[i] & 0xFFF00000) == 0) {
                BP_array[i] += "000";
            } else if ((SourceData[i] & 0xFF000000) == 0) {
                BP_array[i] += "00";
            } else if ((SourceData[i] & 0xF0000000) == 0) {
                BP_array[i] += "0";
            }

            BP_array[i] += Integer.toHexString(SourceData[i]).toUpperCase();
        }

        // 比對指令其他欄位的資料
        for (int i = 0; i < 10; i++) {
            // if(i == 2){
            // CompareData[i] = (byte) ACK_cmd;
            // }

            if (CompareData[i] != Data_tbl[i]) {
                configurate_flag = false;

                i = 16;
            }
        }

        if (configurate_flag == true) {
            setting_loop = 0;
            memory_loop=0;
            isSet=-1;
            loopTimes=0;
            isGoSet=false;
            if(MainActivity.IsShowLogCat)
                Log.e("SET", "設定成功"+isSet);
        } else {
            String configuration_string = "錯誤";
        }

    }
    public void receiveTmp() {
//		msg1="";
        // Step2: 還原指令排列順序
        byte restore;

        restore = Data_tbl[10];
        Data_tbl[10] = Data_tbl[2];
        Data_tbl[2] = restore;

        restore = CompareData[10];
        CompareData[10] = CompareData[2];
        CompareData[2] = restore;

        restore = Data_tbl[11];
        Data_tbl[11] = Data_tbl[3];
        Data_tbl[3] = restore;

        restore = CompareData[11];
        CompareData[11] = CompareData[3];
        CompareData[3] = restore;

        // 修正 CompareData array 的內容
        CompareData[1] = (byte) command.Result_BT_ACK_Addr;

        // Step3: 將收到的指令解密
        Data_tbl[4] ^= (byte) ((command.XOR_en_decryption[0] & 0xFF000000) >> 24);
        Data_tbl[5] ^= (byte) ((command.XOR_en_decryption[0] & 0x00FF0000) >> 16);
        Data_tbl[6] ^= (byte) ((command.XOR_en_decryption[0] & 0x0000FF00) >> 8);
        Data_tbl[7] ^= (byte) ((command.XOR_en_decryption[0] & 0x000000FF) >> 0);
        Data_tbl[8] ^= (byte) ((command.XOR_en_decryption[1] & 0xFF000000) >> 24);
        Data_tbl[9] ^= (byte) ((command.XOR_en_decryption[1] & 0x00FF0000) >> 16);

        CompareData[4] ^= (byte) ((command.XOR_en_decryption[0] & 0xFF000000) >> 24);
        CompareData[5] ^= (byte) ((command.XOR_en_decryption[0] & 0x00FF0000) >> 16);
        CompareData[6] ^= (byte) ((command.XOR_en_decryption[0] & 0x0000FF00) >> 8);
        CompareData[7] ^= (byte) ((command.XOR_en_decryption[0] & 0x000000FF) >> 0);
        CompareData[8] ^= (byte) ((command.XOR_en_decryption[1] & 0xFF000000) >> 24);
        CompareData[9] ^= (byte) ((command.XOR_en_decryption[1] & 0x00FF0000) >> 16);

        // 顯示解密後的 12 bytes 資料
        char loop_cnt;
        String receive_string = "0x";

        for (loop_cnt = 0; loop_cnt < 12; loop_cnt++) {
            if ((Data_tbl[loop_cnt] & 0xFF) < 16) {
                receive_string += "0";
            }

            receive_string += Integer.toHexString(Data_tbl[loop_cnt] & 0xFF)
                    .toUpperCase();

            if ((loop_cnt < 11) && ((loop_cnt % 4) == 3)) {
                receive_string += "_";
            }
        }

        String CRC_16_string;
        CRC_16_string = "Compare Data: 0x";
        for (int i = 0; i < 12; i++) {
            CRC_16_string += Integer.toHexString(CompareData[i] & 0xFF)
                    .toUpperCase();
        }

        // Step4: 計算 CRC-16
        int[] SourceData = new int[3];
        for (int i = 0; i < 3; i++) {
            SourceData[i] = (((int) (Data_tbl[(i * 4) + 0] & 0xFF)) << 24)
                    + (((int) (Data_tbl[(i * 4) + 1] & 0xFF)) << 16)
                    + (((int) (Data_tbl[(i * 4) + 2] & 0xFF)) << 8)
                    + (((int) (Data_tbl[(i * 4) + 3] & 0xFF)) << 0);
        }
        SourceData[2] &= 0xFFFF0000;

        char bit_cnt = 80;
        int CRC_result, compare_CRC;

        CRC_result = CRC16_Check(SourceData, bit_cnt);
        SourceData[2] += CRC_result;
        CRC_16_string = "0x"
                + Integer.toHexString(CRC_result & 0xFFFF).toUpperCase();

        compare_CRC = (((short) (Data_tbl[10] & 0xFF)) << 8)
                + (((short) (Data_tbl[11] & 0xFF)) << 0);

        configurate_flag = true;
        tmp1IsRight=true;
        if (CRC_result != compare_CRC) {
            if(MainActivity.IsShowLogCat)
                Log.e("Error", "here?");
            configurate_flag = false;
        }

        for (int i = 0; i < 3; i++) {
            if ((SourceData[i] & 0xFFF00000) == 0) {
                BP_array[i] += "000";
            } else if ((SourceData[i] & 0xFF000000) == 0) {
                BP_array[i] += "00";
            } else if ((SourceData[i] & 0xF0000000) == 0) {
                BP_array[i] += "0";
            }

            BP_array[i] += Integer.toHexString(SourceData[i]).toUpperCase();
        }

        // 比對指令其他欄位的資料
        for (int i = 0; i < 1; i++) {
            // if(i == 2){
            // CompareData[i] = (byte) ACK_cmd;
            // }

            if (CompareData[i] != Data_tbl[i]) {
                configurate_flag = false;
            }
        }

        if (configurate_flag == true) {
            isTmp=1;

            if(MainActivity.IsShowLogCat)
                Log.e("TMP", "收溫1");
            receiveTmp2();
        } else {
            if(MainActivity.IsShowLogCat)
                Log.e("Error", "Tmp1Error");
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void receiveTmp2() {
        // Step2: 還原指令排列順序
        byte restore;
//		Log.e("where", "1");
        restore = Data_tbl_tmp2[10];
        Data_tbl_tmp2[10] = Data_tbl_tmp2[2];
        Data_tbl_tmp2[2] = restore;

        restore = CompareData[10];
        CompareData[10] = CompareData[2];
        CompareData[2] = restore;

        restore = Data_tbl_tmp2[11];
        Data_tbl_tmp2[11] = Data_tbl_tmp2[3];
        Data_tbl_tmp2[3] = restore;

        restore = CompareData[11];
        CompareData[11] = CompareData[3];
        CompareData[3] = restore;

        // 修正 CompareData array 的內容
        CompareData[1] = (byte) command.Result_BT_ACK_Addr;

        // Step3: 將收到的指令解密
        Data_tbl_tmp2[4] ^= (byte) ((command.XOR_en_decryption[0] & 0xFF000000) >> 24);
        Data_tbl_tmp2[5] ^= (byte) ((command.XOR_en_decryption[0] & 0x00FF0000) >> 16);
        Data_tbl_tmp2[6] ^= (byte) ((command.XOR_en_decryption[0] & 0x0000FF00) >> 8);
        Data_tbl_tmp2[7] ^= (byte) ((command.XOR_en_decryption[0] & 0x000000FF) >> 0);
        Data_tbl_tmp2[8] ^= (byte) ((command.XOR_en_decryption[1] & 0xFF000000) >> 24);
        Data_tbl_tmp2[9] ^= (byte) ((command.XOR_en_decryption[1] & 0x00FF0000) >> 16);

        CompareData[4] ^= (byte) ((command.XOR_en_decryption[0] & 0xFF000000) >> 24);
        CompareData[5] ^= (byte) ((command.XOR_en_decryption[0] & 0x00FF0000) >> 16);
        CompareData[6] ^= (byte) ((command.XOR_en_decryption[0] & 0x0000FF00) >> 8);
        CompareData[7] ^= (byte) ((command.XOR_en_decryption[0] & 0x000000FF) >> 0);
        CompareData[8] ^= (byte) ((command.XOR_en_decryption[1] & 0xFF000000) >> 24);
        CompareData[9] ^= (byte) ((command.XOR_en_decryption[1] & 0x00FF0000) >> 16);
//		Log.e("where", "2");
        // 顯示解密後的 12 bytes 資料
        char loop_cnt;
        String receive_string = "0x";

        for (loop_cnt = 0; loop_cnt < 12; loop_cnt++) {
            if ((Data_tbl_tmp2[loop_cnt] & 0xFF) < 16) {
                receive_string += "0";
            }

            receive_string += Integer.toHexString(Data_tbl_tmp2[loop_cnt] & 0xFF)
                    .toUpperCase();

            if ((loop_cnt < 11) && ((loop_cnt % 4) == 3)) {
                receive_string += "_";
            }
        }

        String CRC_16_string;
        CRC_16_string = "Compare Data: 0x";
        for (int i = 0; i < 12; i++) {
            CRC_16_string += Integer.toHexString(CompareData[i] & 0xFF)
                    .toUpperCase();
        }

        // Step4: 計算 CRC-16
        int[] SourceData = new int[3];
        for (int i = 0; i < 3; i++) {
            SourceData[i] = (((int) (Data_tbl_tmp2[(i * 4) + 0] & 0xFF)) << 24)
                    + (((int) (Data_tbl_tmp2[(i * 4) + 1] & 0xFF)) << 16)
                    + (((int) (Data_tbl_tmp2[(i * 4) + 2] & 0xFF)) << 8)
                    + (((int) (Data_tbl_tmp2[(i * 4) + 3] & 0xFF)) << 0);
        }
        SourceData[2] &= 0xFFFF0000;

        char bit_cnt = 80;
        int CRC_result, compare_CRC;

        CRC_result = CRC16_Check(SourceData, bit_cnt);
        SourceData[2] += CRC_result;
        CRC_16_string = "0x"
                + Integer.toHexString(CRC_result & 0xFFFF).toUpperCase();

        compare_CRC = (((short) (Data_tbl_tmp2[10] & 0xFF)) << 8)
                + (((short) (Data_tbl_tmp2[11] & 0xFF)) << 0);
        configurate_flag = true;
        if (CRC_result != compare_CRC) {
            if(MainActivity.IsShowLogCat)
                Log.e("error", "1");
            configurate_flag=false;
        }

        for (int i = 0; i < 3; i++) {
            if ((SourceData[i] & 0xFFF00000) == 0) {
                BP_array[i] += "000";
            } else if ((SourceData[i] & 0xFF000000) == 0) {
                BP_array[i] += "00";
            } else if ((SourceData[i] & 0xF0000000) == 0) {
                BP_array[i] += "0";
            }

            BP_array[i] += Integer.toHexString(SourceData[i]).toUpperCase();
        }

        // 比對指令其他欄位的資料
        for (int i = 0; i < 1; i++) {
            // if(i == 2){
            // CompareData[i] = (byte) ACK_cmd;
            // }

            if (CompareData[i] != Data_tbl_tmp2[i]) {
                if(MainActivity.IsShowLogCat)
                    Log.e("boolean", CompareData[i]+", "+Data_tbl_tmp2[i]+", "+configurate_flag);
                configurate_flag = false;
            }
        }

        if(MainActivity.IsShowLogCat)
            Log.e("boolean", configurate_flag+", "+tmp1IsRight);
        if (configurate_flag && tmp1IsRight /*&& isTmp==1*/) {//測試0711
            setting_loop = 0;
            memory_loop=0;
            isTmp=-1;
            isGoTmp=false;
            loopTimes=0;
            MainActivity2.isTmp1In=false;

            int a = (((int) (Data_tbl_tmp2[8] & 0xFF)) * 256)
                    + ((int) (Data_tbl_tmp2[9] & 0xFF));
            float b = ((float) a / 100);

            if(MainActivity.statusClothingMode==1){
                b+=0.25;
            }
            extraTmp=b;
            MainActivity.temperature=b;


            Message message=new Message();
            message.what=8;
            MainActivity.hReceiver.sendMessage(message);
            if(MainActivity.IsShowLogCat)
                Log.e("50d5", "50d5, "+b);
            commonTimes++;
            tmpFirst=true;
        } else {
        }
        if(MainActivity.IsShowLogCat)
            Log.e("TMP", "收溫2");

    }
}