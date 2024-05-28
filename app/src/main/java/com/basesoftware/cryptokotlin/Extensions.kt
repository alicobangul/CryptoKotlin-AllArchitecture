package com.basesoftware.cryptokotlin

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.basesoftware.cryptokotlin.adapter.CryptoRecyclerAdapter

object Extensions {

    fun RecyclerView.recyclerSettings(cryptoAdapter : CryptoRecyclerAdapter) {

        setHasFixedSize(true) // Recyclerview boyutunun değişmeyeceği bildirildi [performans artışı]

        layoutManager = LinearLayoutManager(this.context) // Row için layout seçildi

        adapter = cryptoAdapter // Adaptör bağlandı

    }

}