package com.basesoftware.cryptokotlin.architecture.mvc.controller

import com.basesoftware.cryptokotlin.architecture.mvc.model.MVCRepository
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class MVCController @Inject constructor() {

    @Inject
    lateinit var repository : MVCRepository

    fun checkDataFromDb() { repository.checkDataFromDb() } // Repository veritabanını kontrol ediyor

    fun checkDataFromDbProcess() { repository.checkDataFromDbProcess() } // Veritabanında bilgilendirme sonrası işlem yapılıyor

    fun saveDbDataFromApi() { repository.saveDbDataFromApi() } // API verisi veritabanına kaydediliyor

    fun swipeAction() { repository.swipeAction() } // Swipe işlemi modele gönderildi

}