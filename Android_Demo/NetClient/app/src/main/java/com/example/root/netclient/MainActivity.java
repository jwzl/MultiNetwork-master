package com.example.root.netclient;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public static final int SHOW_RESPONSE=0;
    private Button mEthernetBtn;
    private Button mWifiBtn;
    private Handler mhandler;
    private TextView mText;
    public   NetClientUtil  mNetUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initial the member
        mEthernetBtn = (Button)findViewById(R.id.Eth_btn);
        mWifiBtn = (Button)findViewById(R.id.Wifi_Btn);
        mText = (TextView)findViewById(R.id.textView);

        mText.setBackgroundColor(android.graphics.Color.rgb(224,255,255));

        mEthernetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Ethernet
                startActivity(new Intent(MainActivity.this, EthernetCommActivity.class));
                mEthernetBtn.setClickable(false);
            }
        });

        mhandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);

                switch(msg.what){
                    case SHOW_RESPONSE:
                        String response=(String) msg.obj;
                        //在这里进行UI操作，将结果显示到界面上
                        mText.setText(mNetUtil.getCurrentnetworkName()+"\r\nHttp request Sucessful\n"+response);
                        break;
                    default:
                        mText.setText(mNetUtil.getCurrentnetworkName()+"\nHttp request failed!");
                        break;
                }

            }
        };

        mWifiBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // Enable the wifi
                mNetUtil = new NetClientUtil();
                if(mNetUtil.InitNetwork(MainActivity.this,  "MOBILE")) {
                    // Start to request the http.
                    sendRequestWithHttpURLConnection();
                    // mWifiBtn.setClickable(false);
                }else{
                    mText.setText("No available network!");
                }
            }
        });


    }

    // Send the http request!
    private void sendRequestWithHttpURLConnection(){
        mText.setText("Http Request:");
        //send the http request in thread!
        new Thread(new Runnable(){

            @Override
            public void run() {
                // TODO Auto-generated method stub
                HttpURLConnection connection=null;

                try {
                    URL url=new URL("https://www.baidu.com");
                    Message message=new Message();
                    connection =(HttpURLConnection) url.openConnection();

                    if( connection.getResponseCode() ==HttpURLConnection.HTTP_OK){
                        Log.e("####", "Http Okay");
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);
                        InputStream in=connection.getInputStream();
                        //Create the input stream
                        BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                        StringBuilder response=new StringBuilder();
                        String line;
                        while((line=reader.readLine())!=null){
                            response.append(line);
                        }
                        //push the result into message
                        message.what=SHOW_RESPONSE;
                        message.obj=response.toString();
                    }else {
                        Log.e("####",String.valueOf(connection.getResponseCode()));
                        message.what=1;
                        message.obj="";
                    }

                    mhandler.sendMessage(message);

                } catch (java.net.MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    if(connection!=null){
                        connection.disconnect();
                    }
                }
            }

        }).start();
    }
}
