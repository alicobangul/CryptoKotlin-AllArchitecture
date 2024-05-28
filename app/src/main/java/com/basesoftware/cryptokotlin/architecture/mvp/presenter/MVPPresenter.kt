package com.basesoftware.cryptokotlin.architecture.mvp.presenter

import com.basesoftware.cryptokotlin.Constant
import com.basesoftware.cryptokotlin.architecture.mvp.model.MVPRepository
import com.basesoftware.cryptokotlin.architecture.mvp.view.IMVPView
import com.basesoftware.cryptokotlin.model.CryptoModel
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@ActivityScoped
class MVPPresenter @Inject constructor() {

    @Inject lateinit var repository : MVPRepository

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    var view : IMVPView? = null

    fun detach() {

        compositeDisposable.clear() // Gözlemciler kaldırıldı

        view = null // View kaldırıldı

    }

    private fun getDataFromDb(cryptoList: List<CryptoModel>) {

        // Veritabanı kontrol sonucu, view tarafında snackbar ile gösteriliyor

        view?.checkDataFromDbInfo(
            cryptoList,
            if ((cryptoList.isNotEmpty())) Constant.DB_DATA_AVAILABLE else Constant.DB_DATA_NOTEXIST
        )

    }

    private fun getDataFromApi(cryptoList: List<CryptoModel>) {

        if (cryptoList.isNotEmpty()) convertModel(cryptoList, true) // API verisi Recyclerview adaptöründe gösterilecek şekilde modelleniyor

        else {

            view?.swipeEnabled(true) // Refresh izni verildi

            view?.informationForData(Constant.API_DATA_NOTEXIST) // API verisi olmadığına dair bilgilendirildi

        }

    }

    fun initBinds(view: IMVPView) {

        this.view = view // View tarafından gerekli arayüz alındı

        compositeDisposable.addAll(
            repository.behaviorDataFromDb.subscribe(this::getDataFromDb),
            repository.behaviorDataFromApi.subscribe(this::getDataFromApi),
            repository.behaviorInformationForData.subscribe { message: String -> this.view?.informationForData(message) }
        ) // Gözlemciler eklendi

    }

    fun checkDataFromDb() { repository.checkDataFromDb() } // Model, veritabanındaki veriyi kontrol ediliyor

    fun checkDataFromDbProcess(cryptoList: List<CryptoModel>) {

        // Veritabanı verisi var, veri Recyclerview adaptöründe gösterilecek şekilde modelleniyor

        if (cryptoList.isNotEmpty()) convertModel(cryptoList, false) else repository.checkDataFromApi()

    }

    private fun convertModel(cryptoList: List<CryptoModel>, isFromApi: Boolean) {

        /**
         * arrayCryptoRecycler = arrayListOf yapılmasının nedeni: RecyclerView adaptöründe AsyncListDiffer kullanılması.
         * arrayCryptoRecycler = arrayListOf yapılmadan Recyclerview adaptöründeki updateData methodu içerisinde
         * mDiffer.submitList(arrayNewCrypto); [arrayNewCrypto parametreden gelen liste] yapılırsa
         * iki liste aynı olduğu için ItemCallback tetiklenmeyecektir. Bu nedenle arrayCryptoRecycler.clear(); yerine new ArrayList<>() yapılmalıdır.
         * Aksi halde; ekranda veritabanından gelen veriler var iken refresh yapılırsa ve api verilerinin olduğu liste submitList ile verilirse
         * Ekrandaki OFFLINE (DB DATA) yazısı değişmeyecek, item güncellenmeyecektir.
         * Çünkü AsyncListDiffer sistemi eski ve yeni liste eşleştirmesi üzerine kuruludur.
         *
         * Seçenek a-) convertModel içerisinde arrayCryptoRecycler = arrayListOf yapılması
         * Seçenek b-) Adaptör içerisinde mDiffer.submitList(arrayListOf(arrayNewCrypto)); yapılması [arrayNewCrypto parametreden gelen liste]
         */

        // Yeni listeyi oluştur

        val arrayCryptoRecycler = arrayListOf<CryptoRecyclerModel>()

        // Yeni modeller ile liste hazırlanıyor
        arrayCryptoRecycler.addAll(
            cryptoList.map { cryptoModel -> CryptoRecyclerModel(cryptoModel.currency, cryptoModel.price, isFromApi) }
        )

        // Model çevirme işlemi tamamlandı
        convertModelComplete(cryptoList, arrayCryptoRecycler, isFromApi)

    }

    private fun convertModelComplete(arrayDbList: List<CryptoModel>, recyclerList: ArrayList<CryptoRecyclerModel>, isFromApi: Boolean) {

        // Adaptördeki verileri güncelle
        view?.updateRecyclerData(recyclerList)

        // Kullanıcıya veritabanına kayıt isteyip istemediği soruluyor
        if (isFromApi) view?.questionSaveDbData(arrayDbList, Constant.CREATE_DB_DATA_QUESTION)

        // Refresh izni verildi
        view?.swipeEnabled(true)

    }

    fun saveDbDataFromApi(cryptoList: List<CryptoModel>) { repository.saveDbDataFromApi(cryptoList) }

    fun swipeAction(arrayCryptoRecycler: List<CryptoRecyclerModel>) {

        // Eğer hiç veri yok ise API denemesi yap
        if (arrayCryptoRecycler.isEmpty()) repository.checkDataFromApi()

        else {

            // Eğer veri var ise & veritabanı verisi ise, API denemesi yap
            if (!arrayCryptoRecycler[0].isApiData) repository.checkDataFromApi()

            else view?.informationForData(Constant.USE_API_DATA)

        }

    }

}