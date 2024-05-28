package com.basesoftware.cryptokotlin.architecture.mvvm.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.basesoftware.cryptokotlin.Constant
import com.basesoftware.cryptokotlin.architecture.mvvm.model.DataResult
import com.basesoftware.cryptokotlin.architecture.mvvm.model.MVVMRepository
import com.basesoftware.cryptokotlin.model.CryptoModel
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@HiltViewModel
class MVVMViewModel @Inject constructor() : ViewModel() {

    @Inject lateinit var repository : MVVMRepository

    val recyclerData = MutableLiveData<ArrayList<CryptoRecyclerModel>>()

    val dataResult = MutableLiveData<DataResult>()

    val swipeStatus = MutableLiveData<Boolean>()

    val informationForData = MutableLiveData<String>()

    val questionSaveDbData = MutableLiveData<DataResult>()

    private val compositeDisposable = CompositeDisposable()

    private fun getDataFromDb(cryptoList: List<CryptoModel>) {

        // Veritabanı kontrol sonucu, view tarafında snackbar ile gösteriliyor

        dataResult.value = DataResult(
            cryptoList,
            if ((cryptoList.isNotEmpty())) Constant.DB_DATA_AVAILABLE else Constant.DB_DATA_NOTEXIST
        )

    }

    private fun getDataFromApi(cryptoList: List<CryptoModel>) {

        if (cryptoList.isNotEmpty()) convertModel(cryptoList, true) // API verisi Recyclerview adaptöründe gösterilecek şekilde modelleniyor

        else {

            swipeStatus.value = true // Refresh izni verildi

            informationForData.setValue(Constant.API_DATA_NOTEXIST) // API verisi olmadığına dair bilgilendirildi

        }

    }

    fun openObservers() {

        compositeDisposable.addAll(
            repository.behaviorDataFromDb.subscribe(this::getDataFromDb),
            repository.behaviorDataFromApi.subscribe(this::getDataFromApi),
            repository.behaviorInformationForData.subscribe(informationForData::setValue)
        ) // Gözlemciler eklendi

    }

    fun checkDataFromDb() { repository.checkDataFromDb() } // Model, veritabanındaki veriyi kontrol ediliyor

    fun checkDataFromDbProcess(cryptoList: List<CryptoModel>) {

        // Veritabanı verisi var, veri Recyclerview adaptöründe gösterilecek şekilde modelleniyor

        if (cryptoList.isNotEmpty()) convertModel(cryptoList, false) else repository.checkDataFromApi()

    }

    private fun convertModel(cryptoList: List<CryptoModel>, isFromApi: Boolean) {

        /**
         * arrayCryptoRecycler = new ArrayList<>() yapılmasının nedeni: RecyclerView adaptöründe AsyncListDiffer kullanılması.
         * arrayCryptoRecycler = new ArrayList<>() yapılmadan Recyclerview adaptöründeki updateData methodu içerisinde
         * mDiffer.submitList(arrayNewCrypto); [arrayNewCrypto parametreden gelen liste] yapılırsa
         * iki liste aynı olduğu için ItemCallback tetiklenmeyecektir. Bu nedenle arrayCryptoRecycler.clear(); yerine new ArrayList<>() yapılmalıdır.
         * Aksi halde; ekranda veritabanından gelen veriler var iken refresh yapılırsa ve api verilerinin olduğu liste submitList ile verilirse
         * Ekrandaki OFFLINE (DB DATA) yazısı değişmeyecek, item güncellenmeyecektir.
         * Çünkü AsyncListDiffer sistemi eski ve yeni liste eşleştirmesi üzerine kuruludur.
         *
         * Seçenek a-) convertModel içerisinde arrayCryptoRecycler = new ArrayList<>(); yapılması
         * Seçenek b-) Adaptör içerisinde mDiffer.submitList(new ArrayList<>(arrayNewCrypto)); yapılması [arrayNewCrypto parametreden gelen liste]
         */

        // Yeni listeyi oluştur

        val arrayCryptoRecycler = ArrayList<CryptoRecyclerModel>()

        // Yeni modeller ile liste hazırlanıyor
        arrayCryptoRecycler.addAll(
            cryptoList.map { cryptoModel -> CryptoRecyclerModel(cryptoModel.currency, cryptoModel.price, isFromApi) }
        )

        // Model çevirme işlemi tamamlandı
        convertModelComplete(cryptoList, arrayCryptoRecycler, isFromApi)

    }

    private fun convertModelComplete(arrayDbList: List<CryptoModel>, recyclerList: ArrayList<CryptoRecyclerModel>, isFromApi: Boolean) {

        // Adaptördeki verileri güncelle
        recyclerData.value = recyclerList

        // Kullanıcıya veritabanına kayıt isteyip istemediği soruluyor
        if (isFromApi) questionSaveDbData.value = DataResult(arrayDbList, Constant.CREATE_DB_DATA_QUESTION)

        // Refresh izni verildi
        swipeStatus.value = true

    }

    fun saveDbDataFromApi(cryptoList: List<CryptoModel>) { repository.saveDbDataFromApi(cryptoList) }

    fun swipeAction(arrayCryptoRecycler: List<CryptoRecyclerModel>) {

        // Eğer hiç veri yok ise API denemesi yap
        if (arrayCryptoRecycler.isEmpty()) repository.checkDataFromApi()

        else {

            // Eğer veri var ise & veritabanı verisi ise, API denemesi yap
            if (!arrayCryptoRecycler[0].isApiData) repository.checkDataFromApi() else informationForData.setValue(Constant.USE_API_DATA)

        }

    }

}