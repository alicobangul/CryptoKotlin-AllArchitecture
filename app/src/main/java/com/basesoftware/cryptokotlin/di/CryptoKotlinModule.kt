package com.basesoftware.cryptokotlin.di

import android.content.Context
import androidx.room.Room
import com.basesoftware.cryptokotlin.adapter.CryptoRecyclerAdapter
import com.basesoftware.cryptokotlin.roomdb.CryptoDatabase
import com.basesoftware.cryptokotlin.service.CryptoAPI
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.rxjava3.disposables.CompositeDisposable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(ActivityRetainedComponent::class)
object CryptoKotlinModule {

    @ActivityRetainedScoped
    @Provides
    fun compositeDisposableProvider() = CompositeDisposable()

    @ActivityRetainedScoped
    @Provides
    fun cryptoRecyclerAdapterProvider() = CryptoRecyclerAdapter()

    @ActivityRetainedScoped
    @Provides
    fun cryptoDatabaseProvider(@ApplicationContext context: Context) : CryptoDatabase {
        return Room.databaseBuilder(context, CryptoDatabase::class.java, "CryptoDB").allowMainThreadQueries().build()
    }

    @ActivityRetainedScoped
    @Provides
    fun cryptoDaoProvider(cryptodatabase: CryptoDatabase) = cryptodatabase.cryptoDao()

    @ActivityRetainedScoped
    @Provides
    fun gsonProvider() : Gson = GsonBuilder().setLenient().create()

    @ActivityRetainedScoped
    @Provides
    fun cryptoAPIProvider(gson : Gson) : CryptoAPI {

        return Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CryptoAPI::class.java)

    }

}