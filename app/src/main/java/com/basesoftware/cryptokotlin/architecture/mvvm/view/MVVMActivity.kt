package com.basesoftware.cryptokotlin.architecture.mvvm.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.basesoftware.cryptokotlin.Extensions.recyclerSettings
import com.basesoftware.cryptokotlin.adapter.CryptoRecyclerAdapter
import com.basesoftware.cryptokotlin.architecture.mvvm.model.DataResult
import com.basesoftware.cryptokotlin.architecture.mvvm.viewmodel.MVVMViewModel
import com.basesoftware.cryptokotlin.databinding.ActivityMvvmBinding
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MVVMActivity : AppCompatActivity() {

    private var _binding : ActivityMvvmBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var cryptoRecyclerAdapter : CryptoRecyclerAdapter

    private val viewModel : MVVMViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMvvmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init() // Initialize işlemi yapıldı

        viewSettings() // Görünüm ayarları yapılıyor

        listener() // Swipe dinleyici açılıyor

        observers() // Gözlemciler eklendi

        checkDataFromDb() // Veritabanı kontrol ediliyor

    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun init() { viewModel.openObservers() }

    private fun viewSettings() {

        binding.recyclerCrypto.recyclerSettings(cryptoRecyclerAdapter)

        swipeEnabled(false) // Refresh kapalı, ilk önce veri kontrolleri yapılacak

    }

    private fun listener() {

        binding.swipeLayout.setOnRefreshListener {

            binding.swipeLayout.isRefreshing = false // Refresh animasyonu kapatıldı

            viewModel.swipeAction(cryptoRecyclerAdapter.getCurrentList()) // viewmodel'a view tarafında swipe yapıldığı bildirildi

        }

    }

    private fun observers() {

        viewModel.recyclerData.observe(this, this::updateRecyclerData)

        viewModel.dataResult.observe(this, this::checkDataFromDbInfo)

        viewModel.swipeStatus.observe(this, this::swipeEnabled)

        viewModel.informationForData.observe(this, this::informationForData)

        viewModel.questionSaveDbData.observe(this, this::questionSaveDbData)

    }

    // Veri viewmodel'dan alındı
    private fun updateRecyclerData(arrayCryptoRecycler: ArrayList<CryptoRecyclerModel>) { cryptoRecyclerAdapter.updateData(arrayCryptoRecycler) }

    private fun checkDataFromDb() { viewModel.checkDataFromDb() } // Veritabanı kontrol ediliyor

    private fun checkDataFromDbInfo(dbDataResult: DataResult) {

        /**
         * Snackbar sonrası viewmodel tarafında gerekli aksiyon başlatıldı
         * Snackbar dismiss beklenmesinin nedeni veritabanında veri bulunamadığı zaman api verisi alınıyor.
         * Aynı anda iki snackbar açıldığında görünüm karmaşası yaratıyor, bu yüzden dismiss bekleniyor.
         */

        Snackbar
            .make(binding.root, dbDataResult.message, Snackbar.LENGTH_SHORT)
            .addCallback(object : BaseCallback<Snackbar?>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)

                    viewModel.checkDataFromDbProcess(dbDataResult.cryptoList)

                }
            }).show()

    }

    private fun informationForData(message: String?) { Snackbar.make(binding.root, message!!, Snackbar.LENGTH_SHORT).show() } // UI bilgilendirme

    private fun questionSaveDbData(dataResult: DataResult) {

        Snackbar
            .make(binding.root, dataResult.message, Snackbar.LENGTH_LONG)
            .setAction("EVET") { viewModel.saveDbDataFromApi(dataResult.cryptoList) } // API sonrası veriler veritabanına yazılıyor
            .show()

    }

    private fun swipeEnabled(isEnabled: Boolean) { binding.swipeLayout.isEnabled = isEnabled }

}