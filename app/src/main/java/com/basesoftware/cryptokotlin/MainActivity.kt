package com.basesoftware.cryptokotlin

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.basesoftware.cryptokotlin.architecture.mvc.view.MVCActivity
import com.basesoftware.cryptokotlin.architecture.mvp.view.MVPActivity
import com.basesoftware.cryptokotlin.architecture.mvvm.view.MVVMActivity
import com.basesoftware.cryptokotlin.architecture.nonArchitecture.NonArchitectureActivity
import com.basesoftware.cryptokotlin.databinding.ActivityMainBinding
import com.basesoftware.cryptokotlin.databinding.SelectArchitectureBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var _binding : ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectArchitecture(); // Hangi mimariyi seçeceği kullanıcıya soruluyor

    }

    private fun selectArchitecture() {

        val architectureDialog = AlertDialog.Builder(this)

        val architectureBinding: SelectArchitectureBinding = SelectArchitectureBinding.inflate(layoutInflater)

        architectureDialog.setCancelable(false)

        architectureDialog.setView(architectureBinding.getRoot())

        architectureDialog.setPositiveButton("TAMAM") { _: DialogInterface?, _: Int ->

            val selectedArchitectureView: RadioButton = architectureBinding.root
                .findViewById(architectureBinding.rdGroupArchitecture.checkedRadioButtonId)

            val selectedArchitecture = selectedArchitectureView.text.toString()

            val target: Class<*> = when (selectedArchitecture) {
                "MVC" -> MVCActivity::class.java
                "MVP" -> MVPActivity::class.java
                "MVVM" -> MVVMActivity::class.java
                else -> NonArchitectureActivity::class.java
            }

            val changeActivity = Intent(this, target)
            startActivity(changeActivity)
            finish()
        }

        architectureDialog.show();

    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}