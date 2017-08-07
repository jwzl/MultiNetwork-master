package com.example.root.netclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;

/**
 * Created by Qing on 17-6-19.
 */

public class NetClientUtil {
    /* TCP Part*/
    private static Socket mSocket;
    private Thread mClientThread;
    private static Network mnetwork;
    private static int mnetId;
    private int IsConnected;
    private String mNetworkTypeName;
    public final static String TAG ="NetClientUtil";
    private Context mcontext;

    private  boolean bindToNetwork(Context context, int NetType){
        final ConnectivityManager  CMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder;
        int  NetCap = 0;
        mcontext = context;
        builder = new  NetworkRequest.Builder();
        // Clear all capability.
        builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN);
        builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
        builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED);
        if(NetType == ConnectivityManager.TYPE_WIFI)
            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        if(NetType == ConnectivityManager.TYPE_ETHERNET)
            builder.addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET);
        if(NetType == ConnectivityManager.TYPE_MOBILE) {
            builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
            builder.removeCapability(NetworkCapabilities.TRANSPORT_ETHERNET);
        }

        //builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        mNetworkTypeName = CMgr.getActiveNetworkInfo().getTypeName();
        if ( CMgr.getActiveNetworkInfo().getState() != NetworkInfo.State.CONNECTED){
            return false;
        }

        CMgr.registerNetworkCallback(builder.build(), new ConnectivityManager.NetworkCallback(){
            @Override
            public void onAvailable(Network network) {
                CMgr.bindProcessToNetwork(network);
                mNetworkTypeName = CMgr.getNetworkInfo(network).getTypeName();
                Log.e(TAG,"########"+mNetworkTypeName+getMobileNetworkType(CMgr.getNetworkInfo(network))+"onAvailable");
                IsConnected= 1;
            }

            @Override
            public void  onLost(Network network) {
                CMgr.bindProcessToNetwork(null);
                mNetworkTypeName =  CMgr.getNetworkInfo(network).getTypeName();
                Log.e(TAG,"########"+mNetworkTypeName+getMobileNetworkType(CMgr.getNetworkInfo(network))+"onLost");
                CMgr.unregisterNetworkCallback(this);
                IsConnected = 0;
            }
        });

        return true;
    }

    private  boolean forcebindToNetwork(Context context, int NetType){
        final ConnectivityManager  CMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = CMgr.getAllNetworks();

        for (int i = 0; i < networks.length; i++) {
            NetworkInfo netInfo = CMgr.getNetworkInfo(networks[i]);
            if (netInfo.getType() == NetType) {
                CMgr.bindProcessToNetwork(networks[i]);
                mNetworkTypeName = netInfo.getTypeName();
                Log.e(TAG,"########"+mNetworkTypeName+"bounded!");
            }
        }
        if (CMgr.getActiveNetworkInfo().getState() != NetworkInfo.State.CONNECTED){
            return false;
        }

        return true;
    }

    public boolean InitNetwork(Context context, String Type){
        int NetType = ConnectivityManager.TYPE_ETHERNET;

        if(Type.equals("WIFI"))
            NetType = ConnectivityManager.TYPE_WIFI;
        if(Type.equals("Ethernet"))
            NetType = ConnectivityManager.TYPE_ETHERNET;
        if(Type.equals("MOBILE"))
            NetType = ConnectivityManager.TYPE_MOBILE;

        return bindToNetwork(context, NetType);
        //forcebindToNetwork(context, NetType);
    }

    public int getConnectStatus(){
        return IsConnected;
    }

    public static void CreateSocket(String addr, int port ){
        SocketAddress  SAddr;

        SAddr = new InetSocketAddress(addr, port);
        Log.e(TAG, Integer.toString(port));

        try {
            //1. Create the socket
            mSocket = new Socket();
            //2. Connect to the target.
            mSocket.connect(SAddr, 20000);

        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage()+"###");
        }
    }

    public  boolean IsConnect2server(){
        return mSocket.isConnected();
    }
   public String NetClientRecv(){
       try {
           InputStream in = mSocket.getInputStream();
           InputStreamReader isr = new InputStreamReader(in);
           BufferedReader BufRDer = new BufferedReader(isr);

           return BufRDer.readLine();
       }catch (Exception e){
           e.printStackTrace();
           return null;
       }
   }

    public int NetClientSend(String message){
        DataOutputStream out;

        try {
            out = new DataOutputStream(mSocket.getOutputStream());
            out.write((message).getBytes("utf-8"));
            out.flush();
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "###"+e.getMessage());
            return -1;
        }

        return 0;
    }

    public void DeleteNet(){
        try {
            mSocket.getOutputStream().close();
            mSocket.getInputStream().close();
            mSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getCurrentnetworkName(){
        return mNetworkTypeName;
    }

    public String getMobileNetworkType(NetworkInfo  networkInfo) {
        /*
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        */
        int networkType = networkInfo.getSubtype();

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }
}
