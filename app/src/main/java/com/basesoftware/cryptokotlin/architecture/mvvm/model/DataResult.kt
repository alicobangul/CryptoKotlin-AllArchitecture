package com.basesoftware.cryptokotlin.architecture.mvvm.model

import com.basesoftware.cryptokotlin.model.CryptoModel

data class DataResult(val cryptoList : List<CryptoModel>, val message : String)
