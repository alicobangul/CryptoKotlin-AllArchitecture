package com.basesoftware.cryptokotlin.architecture.mvc.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.basesoftware.cryptokotlin.Extensions.recyclerSettings
import com.basesoftware.cryptokotlin.adapter.CryptoRecyclerAdapter
import com.basesoftware.cryptokotlin.architecture.mvc.controller.MVCController
import com.basesoftware.cryptokotlin.architecture.mvc.model.MVCRepository
import com.basesoftware.cryptokotlin.databinding.ActivityMvcBinding
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

@AndroidEntryPoint
class MVCActivity : AppCompatActivity() {

    private var _binding : ActivityMvcBinding? = null
    private val binding get() = _binding!!

    private var compositeDisposable = CompositeDisposable()

    @Inject lateinit var controller: MVCController

    @Inject lateinit var repository: MVCRepository

    @Inject lateinit var cryptoRecyclerAdapter: CryptoRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMvcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        openObserver() // Observer işlemleri

        viewSettings() // Görünüm ayarları yapılıyor

        listenerAndObserver() // Dinleyiciler açılıyor

        checkDataFromDb() // // Veritabanı kontrol ediliyor

    }

    private fun openObserver() {

        compositeDisposable.addAll(
            repository.behaviorCheckDataFromDbInfo.subscribe(this::checkDataFromDbInfo), // Gelen mesaj UI'da gösterildi
            repository.behaviorInformationForData.subscribe(this::informationForData), // Gelen mesaj UI'da gösterildi
            repository.behaviorQuestionSaveDbData.subscribe(this::questionSaveDbData), // Gelen mesaj UI'da gösterildi
            repository.behaviorUpdateRecyclerData.subscribe(this::updateRecyclerData) // Adaptör güncellendi
        )

    }

    private fun viewSettings() {  binding.recyclerCrypto.recyclerSettings(cryptoRecyclerAdapter) }

    private fun listenerAndObserver() {

        binding.swipeLayout.setOnRefreshListener {

            binding.swipeLayout.isRefreshing = false // Refresh animasyonu kapatıldı

            controller.swipeAction() // Controller'a view tarafında swipe yapıldığı bildirildi

        }

    }

    private fun updateRecyclerData(outputList : ArrayList<CryptoRecyclerModel>) { cryptoRecyclerAdapter.updateData(outputList) } // Adaptör güncellendi

    private fun checkDataFromDb() { controller.checkDataFromDb() } // Veritabanı kontrol ediliyor

    private fun checkDataFromDbInfo(message : String) {

        /**
         * Snackbar sonrası controller tarafında gerekli aksiyon başlatıldı
         * Snackbar dismiss beklenmesinin nedeni veritabanında veri bulunamadığı zaman api verisi alınıyor.
         * Aynı anda iki snackbar açıldığında görünüm karmaşası yaratıyor, bu yüzden dismiss bekleniyor.
         */

        Snackbar
            .make(binding.root, message, Snackbar.LENGTH_SHORT)
            .addCallback(object : BaseCallback<Snackbar?>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)

                    controller.checkDataFromDbProcess() // UI bilgilendirme sonrası veritabanı veri sonucuna göre işlemler yapılıyor
                }
            }).show()

    }

    private fun informationForData(message: String) { Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show() } // UI bilgilendirme

    private fun questionSaveDbData(question : String) {

        Snackbar
            .make(binding.root, question, Snackbar.LENGTH_LONG)
            .setAction("EVET") { controller.saveDbDataFromApi() } // API sonrası veriler veritabanına yazılıyor
            .show()

    }

}