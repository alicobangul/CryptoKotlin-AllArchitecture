package com.basesoftware.cryptokotlin.architecture.mvvm.model

import com.basesoftware.cryptokotlin.Constant
import com.basesoftware.cryptokotlin.model.CryptoModel
import com.basesoftware.cryptokotlin.roomdb.CryptoDao
import com.basesoftware.cryptokotlin.service.CryptoAPI
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

@ActivityRetainedScoped
class MVVMRepository @Inject constructor(
    private var cryptoAPI : CryptoAPI,
    private var cryptoDao : CryptoDao,
    private var compositeDisposable : CompositeDisposable
) {

    val behaviorDataFromDb = BehaviorSubject.create<List<CryptoModel>>()
    val behaviorDataFromApi = BehaviorSubject.create<List<CryptoModel>>()
    val behaviorInformationForData = BehaviorSubject.create<String>()

    fun checkDataFromDb() {

        compositeDisposable.add(cryptoDao.getAllData() // Veritabanından List<CryptoModel> döndürmesi gereken observable fonksiyon
            .subscribeOn(Schedulers.io()) // I/O thread kullanıldı
            .observeOn(AndroidSchedulers.mainThread()) // UI gösterim
            .subscribe { t: List<CryptoModel> -> behaviorDataFromDb.onNext(t) }) // Dönen veriyi kontrol etmek için referans verilmiş method

    }

    fun checkDataFromApi() {

        compositeDisposable.clear() // Disposable temizlendi

        compositeDisposable.add(
            cryptoAPI.getCryptoData() // Api tarafından List<CryptoModel> döndürmesi gereken observable fonksiyon
                .subscribeOn(Schedulers.io()) // I/O thread kullanıldı
                .observeOn(AndroidSchedulers.mainThread()) // UI gösterim
                .subscribe(
                    { t: List<CryptoModel> -> behaviorDataFromApi.onNext(t) },
                    { behaviorDataFromApi.onNext(arrayListOf()) }
                ) // Dönen veriyi kontrol etmek için referans verilmiş method
        )

    }

    fun saveDbDataFromApi(arrayCrypto: List<CryptoModel>) {

        compositeDisposable.add(
            cryptoDao.insert(arrayCrypto) // Room ile list şeklinde veri kaydediliyor
                .subscribeOn(Schedulers.io()) // I/O thread kullanıldı
                .observeOn(AndroidSchedulers.mainThread()) // UI gösterim
                .subscribe(
                    { behaviorInformationForData.onNext(Constant.CREATE_DB_DATA_SUCCESS) },  // Başarılı olur ise
                    { behaviorInformationForData.onNext(Constant.CREATE_DB_DATA_FAIL) }) // Başarısız olur ise
        )

    }

}