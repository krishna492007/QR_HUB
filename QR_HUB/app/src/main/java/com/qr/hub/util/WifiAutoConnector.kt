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
     * Triggers official system connection prompt without manual password entry
     */
    fun connectToWifi(
        context: Context,
        ssid: String,
        password: String = "",
        encryption: String = "WPA"
    ) {
        val cleanSsid = ssid.trim().removeSurrounding("\"")
        val cleanPass = password.trim().removeSurrounding("\"")

        // Always copy password to clipboard as backup
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("WiFi Password", cleanPass.ifEmpty { "No password" }))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectViaSystemPrompt(context, cleanSsid, cleanPass, encryption)
        } else {
            connectLegacy(context, cleanSsid, cleanPass, encryption)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun connectViaSystemPrompt(
        context: Context,
        ssid: String,
        pass: String,
        encryption: String
    ) {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // Method 1: Network Suggestion (Persists credentials in system WiFi settings)
            val suggestionBuilder = WifiNetworkSuggestion.Builder()
                .setSsid(ssid)

            val upperEnc = encryption.uppercase()
            when {
                upperEnc.contains("WPA3") || upperEnc.contains("SAE") -> {
                    if (pass.isNotEmpty()) suggestionBuilder.setWpa3Passphrase(pass)
                }
                upperEnc.contains("WPA") || upperEnc.contains("WPA2") || upperEnc.contains("WEP") -> {
                    if (pass.isNotEmpty()) suggestionBuilder.setWpa2Passphrase(pass)
                }
                else -> {
                    // Open / No Password
                    suggestionBuilder.setIsEnhancedOpen(false)
                }
            }

            suggestionBuilder.setIsAppInteractionRequired(true)
            val suggestion = suggestionBuilder.build()
            val status = wifiManager.addNetworkSuggestions(listOf(suggestion))

            // Method 2: System Specifier Prompt (Shows native Android 10-16 "Connect to Wi-Fi network?" dialog)
            val specifierBuilder = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)

            when {
                upperEnc.contains("WPA3") || upperEnc.contains("SAE") -> {
                    if (pass.isNotEmpty()) specifierBuilder.setWpa3Passphrase(pass)
                }
                upperEnc.contains("WPA") || upperEnc.contains("WPA2") -> {
                    if (pass.isNotEmpty()) specifierBuilder.setWpa2Passphrase(pass)
                }
            }

            val specifier = specifierBuilder.build()
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build()

            connectivityManager.requestNetwork(networkRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager.bindProcessToNetwork(network)
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                }
            })

            Toast.makeText(context, "Connecting to '$ssid'... (Password saved)", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: Open WiFi Settings
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            Toast.makeText(context, "Password copied — select '$ssid' to join!", Toast.LENGTH_LONG).show()
        }
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
                Toast.makeText(context, "Connected to $ssid!", Toast.LENGTH_SHORT).show()
            } else {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                Toast.makeText(context, "Password copied — tap '$ssid' to join!", Toast.LENGTH_LONG).show()
            }
        } catch (_: Exception) {
            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
    }
}
