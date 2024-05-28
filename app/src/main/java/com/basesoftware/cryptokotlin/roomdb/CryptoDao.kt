package com.basesoftware.cryptokotlin.roomdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.basesoftware.cryptokotlin.model.CryptoModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface CryptoDao {

    @Query("SELECT * FROM Crypto")
    fun getAllData() : Single<List<CryptoModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cryptoList : List<CryptoModel>) : Completable

}