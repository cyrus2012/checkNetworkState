package com.example.checknetworkstate

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log



class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }


    override fun onStart() {
        super.onStart()
        Log.d(TAG,"MainActivity onStart")

    }


    override fun onResume() {
        super.onResume()
        Log.d(TAG,"onResume()")
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(networkCallback)


        //updateConnectedFlags()


        println("MainActivity onResume end")
    }


    override fun onPause() {
        super.onPause()
        Log.d(TAG,"onPause()")
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }


    override fun onStop() {
        super.onStop()
        Log.d(TAG,"onStop()")
    }





    val networkCallback = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        object : ConnectivityManager.NetworkCallback(ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO) {
            // network is available for use
            override fun onAvailable(network: Network) {
                //super.onAvailable(network)
                Log.d(TAG, "The default network is now: " + network)
            }


            // Network capabilities have changed for the network
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                //super.onCapabilitiesChanged(network, networkCapabilities)
                //val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                Log.e(TAG, "The default network changed capabilities: " + networkCapabilities)

                //require permission android.permission.ACCESS_FINE_LOCATION and android.permission.ACCESS_WIFI_STATE
                val wifiInfo: WifiInfo? = networkCapabilities.transportInfo as WifiInfo?
                if(wifiInfo != null) {
                    Log.d(TAG, wifiInfo.toString())
                    Log.d(TAG, "onCapabilitiesChanged() Connected Wifi SSID: " + wifiInfo.ssid)
                }

                updateActiveNetworkFlags()

            }


            // lost network connection
            override fun onLost(network: Network) {
                //super.onLost(network)
                Log.e(TAG, "The application no longer has a default network. The last default network was " + network)
                updateActiveNetworkFlags()
            }


            override fun onLinkPropertiesChanged(network : Network, linkProperties : LinkProperties) {
                Log.e(TAG, "The default network changed link properties: " + linkProperties)
            }
        }
    }else{
        object : ConnectivityManager.NetworkCallback() {
            // network is available for use
            override fun onAvailable(network: Network) {
                //super.onAvailable(network)
                Log.d(TAG, "The default network is now: " + network)
            }


            // Network capabilities have changed for the network
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                //super.onCapabilitiesChanged(network, networkCapabilities)
                //val unmetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                Log.e(TAG, "The default network changed capabilities: " + networkCapabilities)

                //require permission android.permission.ACCESS_FINE_LOCATION and android.permission.ACCESS_WIFI_STATE
                val wiFiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo : WifiInfo? = wiFiManager.connectionInfo as WifiInfo?
                if(wifiInfo != null)
                    Log.d(TAG, "onCapabilitiesChanged() Connected Wifi SSID: " + wifiInfo.ssid)

                updateActiveNetworkFlags()

            }


            // lost network connection
            override fun onLost(network: Network) {
                //super.onLost(network)
                Log.e(TAG,
                    "The application no longer has a default network. The last default network was $network")
                updateActiveNetworkFlags()
            }


            override fun onLinkPropertiesChanged(network : Network, linkProperties : LinkProperties) {
                Log.e(TAG, "The default network changed link properties: $linkProperties")
            }
        }
    }



    fun updateActiveNetworkFlags(){
        val connectivityManager = getSystemService((Context.CONNECTIVITY_SERVICE)) as ConnectivityManager
        val currentNetwork: Network? = connectivityManager.activeNetwork
        val networkCapability: NetworkCapabilities? = connectivityManager.getNetworkCapabilities(currentNetwork)
        wifiConnected = networkCapability?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?:false
        mobileConnected = networkCapability?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?:false


        Log.d(TAG,"Wifi network is active: $wifiConnected")
        Log.d(TAG, "Mobile network is active: $mobileConnected")


        //to display the SSID name if WIFI is active
        if(wifiConnected){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val wifiInfo: WifiInfo? = networkCapability?.transportInfo as WifiInfo
                if(wifiInfo != null)
                    Log.d(TAG, "Connected Wifi SSID: " + wifiInfo.ssid)
            }


        }


    }








    companion object {

        // Whether there is a Wi-Fi connection.
        private var wifiConnected = false
        // Whether there is a mobile connection.
        private var mobileConnected = false

    }




}
