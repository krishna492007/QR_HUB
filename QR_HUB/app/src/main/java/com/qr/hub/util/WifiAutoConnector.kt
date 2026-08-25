package com.qr.hub.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi

object WifiAutoConnector {

    /**
     * 1-Tap WiFi Connection Engine for Android 10 to Android 16
     * Copies password, suggests network credentials to OS, and opens the native Wi-Fi connection panel
     */
    fun connectToWifi(
        context: Context,
        ssid: String,
        password: String = "",
        encryption: String = "WPA"
    ) {
        val cleanSsid = ssid.trim().removeSurrounding("\"")
        val cleanPass = password.trim().removeSurrounding("\"")

        // 1. Copy password to clipboard as immediate guarantee
        if (cleanPass.isNotEmpty()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("WiFi Password", cleanPass))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectModern(context, cleanSsid, cleanPass, encryption)
        } else {
            connectLegacy(context, cleanSsid, cleanPass, encryption)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectModern(
        context: Context,
        ssid: String,
        pass: String,
        encryption: String
    ) {
        val upperEnc = encryption.uppercase()

        // 2. Add Network Suggestion to OS
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifiManager != null) {
                val suggestionBuilder = WifiNetworkSuggestion.Builder().setSsid(ssid)
                when {
                    upperEnc.contains("WPA3") || upperEnc.contains("SAE") -> {
                        if (pass.isNotEmpty()) suggestionBuilder.setWpa3Passphrase(pass)
                    }
                    upperEnc.contains("WPA") || upperEnc.contains("WPA2") || upperEnc.contains("WEP") -> {
                        if (pass.isNotEmpty()) suggestionBuilder.setWpa2Passphrase(pass)
                    }
                    else -> {
                        suggestionBuilder.setIsEnhancedOpen(false)
                    }
                }
                val suggestion = suggestionBuilder.build()
                wifiManager.addNetworkSuggestions(listOf(suggestion))
            }
        } catch (_: Exception) { }

        // 3. Try WifiNetworkSpecifier with ConnectivityManager (in case device is in range)
        try {
            val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager != null) {
                val specifierBuilder = WifiNetworkSpecifier.Builder().setSsid(ssid)
                if (pass.isNotEmpty()) {
                    if (upperEnc.contains("WPA3") || upperEnc.contains("SAE")) {
                        specifierBuilder.setWpa3Passphrase(pass)
                    } else {
                        specifierBuilder.setWpa2Passphrase(pass)
                    }
                }
                val specifier = specifierBuilder.build()
                val request = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .setNetworkSpecifier(specifier)
                    .build()

                connectivityManager.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        try {
                            connectivityManager.bindProcessToNetwork(network)
                        } catch (_: Exception) { }
                    }
                })
            }
        } catch (_: Exception) { }

        // 4. Open Floating Wi-Fi Panel or Wi-Fi Settings
        var openedPanel = false
        try {
            val panelIntent = Intent(Settings.Panel.ACTION_WIFI).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(panelIntent)
            openedPanel = true
        } catch (_: Exception) { }

        if (!openedPanel) {
            try {
                val settingsIntent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
            } catch (_: Exception) { }
        }

        val toastMsg = if (pass.isNotEmpty()) {
            "Password '$pass' copied! Select '$ssid' to connect."
        } else {
            "Select '$ssid' to connect."
        }
        Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
    }

    @Suppress("DEPRECATION")
    private fun connectLegacy(
        context: Context,
        ssid: String,
        pass: String,
        encryption: String
    ) {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiConfig = android.net.wifi.WifiConfiguration().apply {
                SSID = "\"$ssid\""
                if (pass.isNotEmpty()) {
                    preSharedKey = "\"$pass\""
                } else {
                    allowedKeyManagement.set(android.net.wifi.WifiConfiguration.KeyMgmt.NONE)
                }
            }

            val netId = wifiManager.addNetwork(wifiConfig)
            if (netId != -1) {
                wifiManager.disconnect()
                wifiManager.enableNetwork(netId, true)
                wifiManager.reconnect()
                Toast.makeText(context, "Connecting to $ssid...", Toast.LENGTH_SHORT).show()
            } else {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                Toast.makeText(context, "Password copied — tap '$ssid' to join!", Toast.LENGTH_LONG).show()
            }
        } catch (_: Exception) {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
