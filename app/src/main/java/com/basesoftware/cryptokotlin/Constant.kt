package com.basesoftware.cryptokotlin

object Constant {

    const val DB_DATA_AVAILABLE: String = "Veritabanı verileri getiriliyor"
    const val DB_DATA_NOTEXIST: String = "Veritabanında veri yok, API bağlantısı deneniyor"

    const val USE_API_DATA: String = "Şuan API verileri kullanılmaktadır"
    const val API_DATA_NOTEXIST: String = "API verisi bulunamadı, refresh deneyin"

    const val CREATE_DB_DATA_SUCCESS: String = "API verileri veritabanına yazıldı"
    const val CREATE_DB_DATA_FAIL: String = "API verileri veritabanına yazılamadı"

    const val CREATE_DB_DATA_QUESTION: String = "API verisi alındı, veritabanına yazılsın mı ?"

}