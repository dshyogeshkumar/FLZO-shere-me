package com.example

import android.app.Application
import androidx.room.Room
import com.example.db.AppDatabase
import com.example.discovery.DeviceDiscoveryManager
import com.example.discovery.WifiDirectManager
import com.example.file.LocalFileManager
import com.example.transfer.TransferEngine

class FlzoShareApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var fileManager: LocalFileManager
        private set

    lateinit var wifiDirectManager: WifiDirectManager
        private set

    lateinit var discoveryManager: DeviceDiscoveryManager
        private set

    lateinit var transferEngine: TransferEngine
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "flzo_share.db"
        ).fallbackToDestructiveMigration().build()

        fileManager = LocalFileManager(this)
        wifiDirectManager = WifiDirectManager(this)
        discoveryManager = DeviceDiscoveryManager(this, wifiDirectManager)
        transferEngine = TransferEngine(this, database, fileManager)
    }

    companion object {
        lateinit var instance: FlzoShareApp
            private set
    }
}
