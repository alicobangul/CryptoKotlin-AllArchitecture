package com.basesoftware.cryptokotlin.architecture.mvp.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.basesoftware.cryptokotlin.Extensions.recyclerSettings
import com.basesoftware.cryptokotlin.adapter.CryptoRecyclerAdapter
import com.basesoftware.cryptokotlin.architecture.mvp.presenter.MVPPresenter
import com.basesoftware.cryptokotlin.databinding.ActivityMvpBinding
import com.basesoftware.cryptokotlin.model.CryptoModel
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MVPActivity : AppCompatActivity(), IMVPView {

    private var _binding : ActivityMvpBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var cryptoRecyclerAdapter : CryptoRecyclerAdapter

    @Inject lateinit var presenter : MVPPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMvpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewSettings() // Görünüm ayarları yapılıyor

        listener() // Swipe dinleyici açılıyor

        checkDataFromDb() // Veritabanı kontrol ediliyor

    }

    override fun onStart() {
        super.onStart()
        presenter.initBinds(this) // Presenter içerisine arayüz verildi
    }

    override fun onStop() {
        super.onStop()
        presenter.detach() // Presenter gözlemcisi ve view kaldırıldı
    }

    private fun viewSettings() {

        binding.recyclerCrypto.recyclerSettings(cryptoRecyclerAdapter)

        swipeEnabled(false) // Refresh kapalı, ilk önce veri kontrolleri yapılacak

    }

    private fun listener() {

        binding.swipeLayout.setOnRefreshListener {

            binding.swipeLayout.isRefreshing = false // Refresh animasyonu kapatıldı

            presenter.swipeAction(cryptoRecyclerAdapter.getCurrentList()) // Presenter'a view tarafında swipe yapıldığı bildirildi

        }

    }

    override fun updateRecyclerData(arrayCryptoRecycler : ArrayList<CryptoRecyclerModel>) {

        /**
         * Adaptör güncellendi
         * MVC ile arasındaki en belirgin fark veri model'den değil, presenter'dan alınmaktadır
         */

        cryptoRecyclerAdapter.updateData(arrayCryptoRecycler)

    }

    private fun checkDataFromDb() { presenter.checkDataFromDb() } // Veritabanı kontrol ediliyor

    override fun checkDataFromDbInfo(cryptoList: List<CryptoModel>, message: String) {
        /**
         * Snackbar sonrası presenter tarafında gerekli aksiyon başlatıldı
         * Snackbar dismiss beklenmesinin nedeni veritabanında veri bulunamadığı zaman api verisi alınıyor.
         * Aynı anda iki snackbar açıldığında görünüm karmaşası yaratıyor, bu yüzden dismiss bekleniyor.
         */

        Snackbar
            .make(binding.root, message, Snackbar.LENGTH_SHORT)
            .addCallback(object : BaseCallback<Snackbar?>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)

                    presenter.checkDataFromDbProcess(cryptoList)

                }
            }).show()

    }

    override fun informationForData(message: String) { Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show() } // UI bilgilendirme

    override fun questionSaveDbData(cryptoList: List<CryptoModel>, question: String) {

        Snackbar
            .make(binding.root, question, Snackbar.LENGTH_LONG)
            .setAction("EVET") { presenter.saveDbDataFromApi(cryptoList) } // API sonrası veriler veritabanına yazılıyor
            .show()

    }

    override fun swipeEnabled(isEnabled: Boolean) { binding.swipeLayout.isEnabled = isEnabled }

}