package com.basesoftware.cryptokotlin.architecture.nonArchitecture

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.basesoftware.cryptokotlin.Constant
import com.basesoftware.cryptokotlin.Extensions.recyclerSettings
import com.basesoftware.cryptokotlin.adapter.CryptoRecyclerAdapter
import com.basesoftware.cryptokotlin.databinding.ActivityNonArchitectureBinding
import com.basesoftware.cryptokotlin.model.CryptoModel
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel
import com.basesoftware.cryptokotlin.roomdb.CryptoDao
import com.basesoftware.cryptokotlin.roomdb.CryptoDatabase
import com.basesoftware.cryptokotlin.service.CryptoAPI
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.Delegates

class NonArchitectureActivity : AppCompatActivity() {

    private var _binding : ActivityNonArchitectureBinding? = null
    private val binding get() = _binding!!

    private lateinit var compositeDisposable: CompositeDisposable

    private lateinit var cryptoRecyclerAdapter : CryptoRecyclerAdapter // Recyclerview'a verilen adaptör

    private lateinit var arrayCryptoRecycler : ArrayList<CryptoRecyclerModel> // Recyclerview içerisine verilen liste

    private lateinit var arrayCrypto: List<CryptoModel> // Veritabanından & API'den dönen liste

    private lateinit var cryptoAPI: CryptoAPI // Retrofit ile veri çekmek için arayüz

    private lateinit var cryptoDao: CryptoDao // Veritabanından veri çekmek için DAO

    private var isFromApi by Delegates.notNull<Boolean>() // Veri çekme işleminin nereden yapıldığı belirten değişken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNonArchitectureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialize(); // Değişkenler initialize ediliyor

        viewSettings(); // Görünüm ayarları yapılıyor

        listener(); // Swipe dinleyici açılıyor

        checkDataFromDb(); // Veritabanı kontrol ediliyor

    }

    private fun initialize() {

        val cryptoDatabase = Room.databaseBuilder(applicationContext, CryptoDatabase::class.java, "CryptoDB")
            .allowMainThreadQueries()
            .build() // Veritabanı bağlantısı

        cryptoDao = cryptoDatabase.cryptoDao() // Dao tanımlandı

        compositeDisposable = CompositeDisposable() // CompositeDisposable oluşturuldu

        arrayCryptoRecycler = arrayListOf() // Adaptöre verilecek liste oluşturuldu

        arrayCrypto = arrayListOf() // Api & Database'den dönen verilerde kullanılan liste

        isFromApi = false // Şuanki veri alma modu database [DEFAULT]

        cryptoRecyclerAdapter = CryptoRecyclerAdapter() // Adaptör oluşturuldu

        cryptoAPI = Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
            .create(CryptoAPI::class.java) // Api sınıfı oluşturuldu

    }

    private fun viewSettings() {

        binding.recyclerCrypto.recyclerSettings(cryptoRecyclerAdapter)

        binding.swipeLayout.setEnabled(false) // Swipe kapalı [DEFAULT]
    }

    private fun listener() {

        binding.swipeLayout.setOnRefreshListener {

            binding.swipeLayout.apply {

                isEnabled = false // Swipe kapatıldı

                isRefreshing = false // Swipe animasyonu kapatıldı

            }

            // Şuanki data durumlarına göre işlem yapılıyor
            if (arrayCryptoRecycler.isEmpty()) checkDataFromApi() // Eğer hiç veri yok ise API'den veri almayı dene

            else {

                if (!arrayCryptoRecycler[0].isApiData) checkDataFromApi() // Eğer veri var ise & veritabanı verisi ise API'den veri almayı dene

                else {

                    informationSnackbar(Constant.USE_API_DATA) // API UI bilgilendirme

                    binding.swipeLayout.isEnabled = true // Swipe açıldı

                }

            }
        }

    }

    private fun checkDataFromDb() {

        isFromApi = false // Veritabanından veri almaya çalışıldığı bildirildi

        compositeDisposable.add(
            cryptoDao.getAllData() // Veritabanından List<CryptoModel> döndürmesi gereken observable fonksiyon
                .subscribeOn(Schedulers.io()) // I/O thread kullanıldı
                .observeOn(AndroidSchedulers.mainThread()) // UI gösterim
                .subscribe(this::getDataFromDb)
        ) // Dönen veriyi kontrol etmek için referans verilmiş method

    }

    private fun getDataFromDb(cryptoList : List<CryptoModel>) {

        arrayCrypto = cryptoList // Veritabanından dönen liste diğer methodlara parametre verilmemesi için değişkene atıldı

        val message = if (arrayCrypto.isNotEmpty()) Constant.DB_DATA_AVAILABLE else Constant.DB_DATA_NOTEXIST

        Snackbar
            .make(binding.root, message, Snackbar.LENGTH_SHORT)
            .addCallback(object : BaseCallback<Snackbar?>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)

                    /**
                     * Veritabanında veri var [liste boş değil], veriyi recyclerview için modelle
                     * Veritabanında veri yok [liste boş], API'den veri almayı dene
                     */
                    if (arrayCrypto.isNotEmpty()) convertModel() else checkDataFromApi()

                }
            }).show()
    }

    private fun checkDataFromApi() {

        isFromApi = true // API'dan veri almaya çalışıldığı bildirildi

        compositeDisposable.add(cryptoAPI.getCryptoData() // Api tarafından List<CryptoModel> döndürmesi gereken observable fonksiyon
            .subscribeOn(Schedulers.io()) // I/O thread kullanıldı
            .observeOn(AndroidSchedulers.mainThread()) // UI gösterim
            .subscribe(this::getDataFromApi) { getDataFromApi(arrayListOf()) } // Dönen veriyi kontrol etmek için referans verilmiş method
        )

    }

    private fun getDataFromApi(cryptoList: List<CryptoModel>) {

        arrayCrypto = cryptoList // API'den dönen liste diğer methodlara parametre verilmemesi için değişkene atıldı

        if (cryptoList.isNotEmpty()) {

            Snackbar
                .make(binding.root, Constant.CREATE_DB_DATA_QUESTION, Snackbar.LENGTH_SHORT)
                .setAction("EVET") { saveDbDataFromApi() } // API sonrası veriler veritabanına yazılıyor
                .show()

            convertModel() // Veriyi recylerview için modelle

        }
        else {

            informationSnackbar(Constant.API_DATA_NOTEXIST) // UI bilgilendirmesi

            binding.swipeLayout.isEnabled = true // Swipe açıldı

        }
    }

    private fun convertModel() {

        // Veriler RecyclerView için modelleniyor

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

        arrayCryptoRecycler = arrayListOf()

        arrayCryptoRecycler.addAll(
            arrayCrypto.map { cryptoModel -> CryptoRecyclerModel(cryptoModel.currency, cryptoModel.price, isFromApi) }
        )

        showData(); // Veri modelleme bitti veriyi recyclerview'da göster

        binding.swipeLayout.isEnabled = true // Swipe yapma izni verildi

    }

    private fun showData() { cryptoRecyclerAdapter.updateData(arrayCryptoRecycler) } // RecyclerView'a data gönder AsyncListDiffer kullanarak

    private fun saveDbDataFromApi() {

        compositeDisposable.add(
            cryptoDao.insert(arrayCrypto) // Room ile list şeklinde veri kaydediliyor
                .subscribeOn(Schedulers.io()) // I/O thread kullanıldı
                .observeOn(AndroidSchedulers.mainThread()) // UI gösterim
                .subscribe(
                    { informationSnackbar(Constant.CREATE_DB_DATA_SUCCESS) },  // Başarılı olur ise
                    { informationSnackbar(Constant.CREATE_DB_DATA_FAIL)} // Başarısız olur ise
                )
        )

    }

    private fun informationSnackbar(message: String) { Snackbar.make(binding.root, message, Toast.LENGTH_SHORT).show() }

}