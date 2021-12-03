package com.sample.download_manager_app.downloadmanager.utils

import android.content.Context
import com.sample.download_manager_app.downloadmanager.database.DownloadManagerDatabase
import com.sample.download_manager_app.downloadmanager.repo.DownloadManagerRepository
import com.sample.download_manager_app.downloadmanager.utils.networkutil.ConnectivityInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object InjectorUtils {

    fun getDownloadManagerRepository(context: Context): DownloadManagerRepository {
        return DownloadManagerRepository.getInstance(
            DownloadManagerDatabase.getInstance(context).taskDao()
        )
    }

    fun getRetrofitService(): Retrofit {

        return Retrofit.Builder()
            .baseUrl("https://file-examples.com/")
            .client(OkHttpClient().apply {

                 this.newBuilder().addInterceptor(ConnectivityInterceptor())
                     .build()
            })
            .build()
    }
}