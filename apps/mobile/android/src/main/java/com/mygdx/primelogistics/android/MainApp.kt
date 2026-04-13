package com.mygdx.primelogistics.android

import android.app.Application
import com.mygdx.primelogistics.android.api.RetrofitClient
import com.mygdx.primelogistics.android.utils.SessionManager

class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val sessionManager = SessionManager(this)

        RetrofitClient.init {
            sessionManager.fetchAuthToken()
        }
    }
}
