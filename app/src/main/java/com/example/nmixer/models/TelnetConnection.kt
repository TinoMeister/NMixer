package com.example.nmixer.models

import android.os.StrictMode
import java.io.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class TelnetConnection {
    var aux = "G4F0!"
    private var ip : String = "192.168.4.1"
    private var port : Int = 2807
    private var socket = DatagramSocket()
    var result : String = ""

    fun connectUdp(){
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            //Open a port to send the package
            socket = DatagramSocket()
            socket.broadcast = true

            val sendData = checkSumCalc(aux).toByteArray()

            val sendPacket = DatagramPacket(sendData, sendData.size, InetAddress.getByName(ip), port)
            socket.send(sendPacket)

            receiveUDP()
        } catch (e: IOException) {
            //            Log.e(FragmentActivity.TAG, "IOException: " + e.message)
        }
    }

    fun sendUDP(data : String){
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        try {
            //Open a port to send the package
            socket = DatagramSocket()
            socket.broadcast = true

            aux = "G4F$data!"

            val sendData = checkSumCalc(aux).toByteArray()

            val sendPacket = DatagramPacket(sendData, sendData.size, InetAddress.getByName(ip), port)
            socket.send(sendPacket)

        } catch (e: IOException) {
            //Log.e(FragmentActivity.TAG, "IOException: " + e.message)
        }
    }

    private fun receiveUDP(){
        val buffer = ByteArray(5)

        try {
            val packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)
            result = String(packet.data, 0, packet.length)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            socket.close()
        }
    }

    private fun checkSumCalc(data : String) : String{
        var checksum = 0

        for (i in data.indices){
            checksum += data[i].toByte()
        }

        return data + checksum.toString()
    }
}