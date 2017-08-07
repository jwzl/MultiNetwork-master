package com.example.root.netclient;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by root on 17-6-19.
 */

public class EthernetCommActivity extends AppCompatActivity {
    private Button mConnectBtn;
    private Button mstopBtn;
    private Button mSendBtn;
    private EditText mAddrText;
    private EditText mPortText;
    private EditText mText;
    private TextView mNetStatusView;
    private ExecutorService  mThreadPool;
    public   NetClientUtil  mNetUtil;
    private String  mreponse;
    private android.os.Handler mMainHandler;
    public  final static  String TAG="EthernetCommActivity:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ethernet);

        mConnectBtn = (Button)findViewById(R.id.ConnectBtn);
        mstopBtn =(Button)findViewById(R.id.StopBtn);
        mSendBtn = (Button)findViewById(R.id.SendBtn);
        mAddrText = (EditText)findViewById(R.id.IP_EDIT);
        mPortText = (EditText)findViewById(R.id.PortEdit);
        mText = (EditText)findViewById(R.id.Content);
        mNetStatusView = (TextView)findViewById(R.id.NetStatus);

        mAddrText.setBackgroundColor(android.graphics.Color.rgb(202,255,112));
        mPortText.setBackgroundColor(android.graphics.Color.rgb(202,255,112));
        //Create the thread pool
        mThreadPool = Executors.newCachedThreadPool();
        //bound the process to specifed the network;
        mNetUtil = new NetClientUtil();
        mNetUtil.InitNetwork(EthernetCommActivity.this, "Ethernet");
        mText.setBackgroundColor(android.graphics.Color.rgb(224,255,255));
        mNetStatusView.setBackgroundColor(android.graphics.Color.rgb(192,255,62));
        mNetStatusView.setTextColor(Color.BLUE);
        mNetStatusView.setText("Current Network is :"+mNetUtil.getCurrentnetworkName());
        //Set the listenor!
        mConnectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Start the Ethernet
                mText.setText("");
                if(mNetUtil.getConnectStatus() != 0 ){
                    if(TextUtils.isEmpty(mAddrText.getText().toString())){
                        mText.setText("Please input the IP address");
                        mText.setTextColor(0xffff0000);
                        return ;
                    }
                    if(TextUtils.isEmpty(mPortText.getText().toString())){
                        mText.setText("Please input the correct port");
                        mText.setTextColor(0xffff0000);
                        return ;
                    }

                    // Start the Ethernet
                    mThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            int ret;
                            try {
                                ret = StartEthernet(mAddrText.getText().toString(),  mPortText.getText().toString());
                                if(ret != 0)
                                    System.out.print("Connect error!");
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(mText.getText().toString())){
                    return ;
                }
                // Create the thread to send!
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mNetUtil.NetClientSend(mText.getText().toString());
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });

                mText.setText("");
            }
        });

        mstopBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EthernetCommActivity.this, MainActivity.class));
            }
        });
        mMainHandler = new android.os.Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        mText.setText("");
                        Log.d(TAG, "Recieved"+mreponse);
                        mText.setText("Server Replied:"+mreponse);
                        break;
                }
            }
        };
        // Recv thread
        Thread TcpThreadRecv = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    while (true){
                        mreponse = mNetUtil.NetClientRecv();
                        // notify the test to Display the reply message!
                        if(mreponse != null){
                            if(mreponse.length() > 0 ) {
                                Message msg = Message.obtain();
                                msg.what = 0;
                                mMainHandler.sendMessage(msg);
                            }
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "###"+e.getMessage());
                }
            }
        });

        TcpThreadRecv.setPriority(Thread.NORM_PRIORITY);
        TcpThreadRecv.start();

    }
    /* Start Ethernet */
    private int StartEthernet(String addr, String port){

        int ret = -1;

        try {
            mNetUtil.CreateSocket(addr, Integer.parseInt(port));
            if(mNetUtil.IsConnect2server()){
               ret = 0;
            }
        }catch (Exception e){
            ret = -1;
            e.printStackTrace();
        }
        return ret;
    }

}