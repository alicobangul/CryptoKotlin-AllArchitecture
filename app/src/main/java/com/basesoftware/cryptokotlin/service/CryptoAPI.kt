package com.basesoftware.cryptokotlin.service

import com.basesoftware.cryptokotlin.model.CryptoModel
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface CryptoAPI {

    @GET("atilsamancioglu/K21-JSONDataSet/master/crypto.json")
    fun getCryptoData() : Single<List<CryptoModel>>

}