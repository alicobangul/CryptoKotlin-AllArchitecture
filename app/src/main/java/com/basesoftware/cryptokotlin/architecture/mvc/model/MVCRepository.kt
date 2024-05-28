package com.basesoftware.cryptokotlin.architecture.mvc.model

import com.basesoftware.cryptokotlin.Constant
import com.basesoftware.cryptokotlin.model.CryptoModel
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel
import com.basesoftware.cryptokotlin.roomdb.CryptoDao
import com.basesoftware.cryptokotlin.service.CryptoAPI
import dagger.hilt.android.scopes.ActivityScoped
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject


@ActivityScoped
class MVCRepository @Inject constructor(private var cryptoAPI : CryptoAPI, private var cryptoDao : CryptoDao, private var compositeDisposable : CompositeDisposable) {

    private var arrayCryptoRecycler: ArrayList<CryptoRecyclerModel> = arrayListOf()

    private var arrayCrypto: List<CryptoModel> = arrayListOf()

    private var isFromApi = false

    private var isSwipeEnabled = false

    val behaviorCheckDataFromDbInfo = BehaviorSubject.create<String>()
    val behaviorInformationForData = BehaviorSubject.create<String>()
    val behaviorQuestionSaveDbData = BehaviorSubject.create<String>()
    val behaviorUpdateRecyclerData = BehaviorSubject.create<ArrayList<CryptoRecyclerModel>>()

    fun swipeAction() {

        if (isSwipeEnabled) {

            // Şuanki data durumlarına göre swipe aksiyonu yapılıyor

            if (arrayCryptoRecycler.isEmpty()) checkDataFromApi() // Eğer hiç veri yok ise API denemesi yap

            else {

                if (!arrayCryptoRecycler[0].isApiData) checkDataFromApi() // Eğer veri var ise & veritabanı verisi ise, API denemesi yap

                else behaviorInformationForData.onNext(Constant.USE_API_DATA)

            }

        }

    }

    fun checkDataFromDb() {

        compositeDisposable.add(
            cryptoDao.getAllData() // Veritabanından List<CryptoModel> döndürmesi gereken observable fonksiyon
                .subscribeOn(Schedulers.io()) // I/O thread kullanıldı
                .observeOn(AndroidSchedulers.mainThread()) // UI gösterim
                .subscribe(this::getDataFromDb)
        ) // Dönen veriyi kontrol etmek için referans verilmiş method

    }

    private fun getDataFromDb(cryptoList: List<CryptoModel>) {

        compositeDisposable.clear() // Disposable temizlendi

        arrayCrypto = cryptoList // Dönen veri diğer fonksiyonların işlem yapabilmesi için değişkene atıldı

        isFromApi = false // API verisi olmadığı belirtildi

        // Gözlemciler database kontrolü sonrası bilgilendiriliyor
        behaviorCheckDataFromDbInfo.onNext(if (arrayCrypto.isNotEmpty()) Constant.DB_DATA_AVAILABLE else Constant.DB_DATA_NOTEXIST)

    }

    fun checkDataFromDbProcess() {

        if (arrayCrypto.isNotEmpty()) convertModel() // Veritabanı verisi var, veri Recyclerview adaptöründe gösterilecek şekilde modelleniyor

        else checkDataFromApi() // Veritabanında veri yok [liste boş], API verisi kontrol ediliyor

    }

    private fun checkDataFromApi() {

        compositeDisposable.add(cryptoAPI.getCryptoData() // Api tarafından List<CryptoModel> döndürmesi gereken observable fonksiyon
            .subscribeOn(Schedulers.io()) // I/O thread kullanıldı
            .observeOn(AndroidSchedulers.mainThread()) // UI gösterim
            .subscribe(this::getDataFromApi) { getDataFromApi(arrayListOf()) } // Dönen veriyi kontrol etmek için referans verilmiş method
        )

    }

    private fun getDataFromApi(cryptoList: List<CryptoModel>) {

        compositeDisposable.clear() // Disposable temizlendi

        arrayCrypto = cryptoList // Dönen veri diğer fonksiyonların işlem yapabilmesi için değişkene atıldı

        isFromApi = true // API verisi olduğu belirtildi

        if (arrayCrypto.isNotEmpty()) convertModel() // API verisi Recyclerview adaptöründe gösterilecek şekilde modelleniyor

        else {

            isSwipeEnabled = true // Refresh izni verildi

            behaviorInformationForData.onNext(Constant.API_DATA_NOTEXIST) // API verisi olmadığına dair gözlemciler bilgilendirildi
        }

    }

    fun saveDbDataFromApi() {

        compositeDisposable.add(
            cryptoDao.insert(arrayCrypto) // Room ile list şeklinde veri kaydediliyor
                .subscribeOn(Schedulers.io()) // I/O thread kullanıldı
                .observeOn(AndroidSchedulers.mainThread()) // UI gösterim
                .subscribe(
                    { behaviorInformationForData.onNext(Constant.CREATE_DB_DATA_SUCCESS) },  // Başarılı olur ise
                    { behaviorInformationForData.onNext(Constant.CREATE_DB_DATA_FAIL) } // Başarısız olur ise
                )
        )

    }

    private fun convertModel() {

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

        arrayCryptoRecycler = arrayListOf() // Yeni listeyi oluştur

        // Yeni modeller ile liste hazırlanıyor
        arrayCryptoRecycler.addAll(
            arrayCrypto.map { cryptoModel -> CryptoRecyclerModel(cryptoModel.currency, cryptoModel.price, isFromApi) }
        )

        behaviorUpdateRecyclerData.onNext(arrayCryptoRecycler) // Son item'a gelince gözlemcileri bilgilendir

        // Kullanıcıya veritabanına kayıt isteyip istemediği soruluyor
        if (isFromApi) behaviorQuestionSaveDbData.onNext(Constant.CREATE_DB_DATA_QUESTION)

        isSwipeEnabled = true // Refresh izni verildi

    }

}