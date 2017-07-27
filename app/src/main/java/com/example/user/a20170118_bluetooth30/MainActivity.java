package com.example.user.a20170118_bluetooth30;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    UUID MY_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothSocket bluetoothSocket;
    static DataOutputStream outStream=null;
    static DataOutputStream outStream2=null;
    static DataInputStream inStream=null;
    Button btn,btn2;
    TextView tv,tv2,tv3;
    MainActivity2 mainActivity2;
    Handler hhhh;
    public static Handler hReceiver,hWorker;
    CAction action;
    public static boolean isExist = true;
    public int[] Source_Data = new int[3];
    static int time_hour = 0;
    static int time_minute = 0;
    static int time_second = 60;
    static int time_hr_min_sec = (((time_hour * 60) + time_minute) * 60 + time_second);
    static int Alarm1_temperature = 3200;
    static int Alarm2_temperature = 3800;
    static int statusClothingMode = 1;
    static float temperature = 0;
    public static Boolean IsShowLogCat=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent enableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent,0);
        action = new CAction(outStream, inStream, MainActivity.this);
        tv=(TextView)findViewById(R.id.textView);
        tv2=(TextView)findViewById(R.id.textView2);
        tv3=(TextView)findViewById(R.id.textView3);

        btn=(Button)findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.startDiscovery();
            }
        });
        btn2=(Button)findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            Thread.sleep(6000);

                        } catch (InterruptedException e2) {
                            // TODO Auto-generated catch block
                            e2.printStackTrace();
                        }
                        hhhh.post(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub;
                                ToClickMacList_OK();
                            }
                        });


                    }
                }).start();

            }
        });
        registerReceiver(broadcastReceiver,intentFilter);
        hhhh = new Handler();
        hReceiver = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                // String now_date="",time="";
                Log.e("收到","  "+msg.what);
                switch (msg.what) {
                    case 1:
                        Log.e("case1","case1");
                        action.receiveSet();
                        break;
                    case 2:
                        action.receiveTmp();
                        break;
                    case 8:
                        Log.e("temp~~~~~~~~~~~~",String.valueOf(temperature)+"度");
                        tv.setText("temp~~~"+String.valueOf(temperature)+"度");

                }

            }
        };


        //  mBluetoothAdapter.cancelDiscovery();

    }
    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
               BluetoothDevice bluetoothDevice=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e("bluetooth_name",bluetoothDevice.getName());
                Log.e("bluetooth_address",bluetoothDevice.getAddress());
                tv3.setText("bluetooth_name"+bluetoothDevice.getName()+"bluetooth_address"+bluetoothDevice.getAddress());
                try {
                    bluetoothSocket=bluetoothDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    outStream = new DataOutputStream(bluetoothSocket.getOutputStream());
                    inStream = new DataInputStream(bluetoothSocket.getInputStream());

                    bluetoothSocket.connect();
                   if (bluetoothSocket.isConnected()){
                        Log.e("bluetooth","success");
                       tv2.setText("bluetooth"+"success");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private void ToClickMacList_OK() {
        mBluetoothAdapter.cancelDiscovery();
        readMessage();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        action.doALL();
    }
    private void readMessage() {
        mainActivity2 = new MainActivity2(inStream);
        mainActivity2.start();
    }


    IntentFilter intentFilter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
}
