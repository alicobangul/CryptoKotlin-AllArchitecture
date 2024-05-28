package com.basesoftware.cryptokotlin.architecture.mvp.view

import com.basesoftware.cryptokotlin.model.CryptoModel
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel


interface IMVPView {

    fun updateRecyclerData(arrayCryptoRecycler : ArrayList<CryptoRecyclerModel>)

    fun checkDataFromDbInfo(cryptoList: List<CryptoModel>, message: String)

    fun informationForData(message: String)

    fun questionSaveDbData(cryptoList: List<CryptoModel>, question: String)

    fun swipeEnabled(isEnabled: Boolean)

}