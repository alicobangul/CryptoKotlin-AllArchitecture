package com.basesoftware.cryptokotlin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.basesoftware.cryptokotlin.R
import com.basesoftware.cryptokotlin.databinding.RowCryptoBinding
import com.basesoftware.cryptokotlin.model.CryptoRecyclerModel
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject


@ActivityScoped
class CryptoRecyclerAdapter @Inject constructor() : RecyclerView.Adapter<CryptoRecyclerAdapter.RowHolder>() {

    private var mDiffer : AsyncListDiffer<CryptoRecyclerModel>

    init {

        val diffCallBack = object : DiffUtil.ItemCallback<CryptoRecyclerModel>() {
            override fun areItemsTheSame( oldItem: CryptoRecyclerModel, newItem: CryptoRecyclerModel): Boolean {
                /**
                 * Veritabanı verisi mevcutken refresh yapıldığında listede sadece isApiData alanı değiştiriliyor.
                 * Burada amaç: item'ı direkt olarak güncellemek yerine sadece içeriğini güncelleyerek iş yükünü azaltmak.
                 * Bu nedenle yeni liste verildiğinde de aynı item olduğunu belirtmek için aynı kalan alanı veriyoruz.
                 * (currency veya price olabilir)
                 */
                return oldItem.currency.matches(Regex(newItem.currency))
            }

            override fun areContentsTheSame(oldItem: CryptoRecyclerModel, newItem: CryptoRecyclerModel): Boolean {
                // Eğer verilerden herhangi birisi farklı ise güncelleme gerekiyor [false dönecek]
                return oldItem.currency.matches(Regex(newItem.currency)) &&
                        oldItem.price.matches(Regex(newItem.price)) &&
                        oldItem.isApiData.equals(newItem.isApiData)
            }

        }

        mDiffer = AsyncListDiffer(this, diffCallBack) // AsyncListDiffer'ı oluştur

    }

    class RowHolder(val binding : RowCryptoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val binding = RowCryptoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RowHolder(binding)
    }


    override fun onBindViewHolder(holder: RowHolder, position: Int) {

        holder.apply {

            binding.apply {

                crypto = mDiffer.currentList[holder.bindingAdapterPosition] // Databinding verisi XML'e verildi

                txtDataStatus.setTextColor(
                    ContextCompat.getColor(
                        itemView.context,
                        if (mDiffer.currentList[bindingAdapterPosition].isApiData) R.color.green else R.color.red
                    )
                ) // Duruma göre yeşil veya kırmızı renk

            }

        }

    }

    fun updateData(arrayNewCrypto : ArrayList<CryptoRecyclerModel>) {

        /**
         * AsyncListDiffer'a yeni liste oluşturarak veriler gönderilmeli, aksi halde ItemCallback çalışmayacaktır.
         * Eğer parametre olarak verilen ArrayList, gönderilmeden önce yeni ArrayList şeklinde yeniden oluşturulmadıysa
         * submitList içerisinde arrayListOf(arrayNewCrypto) yapılmalıdır.
         */

        mDiffer.submitList(arrayNewCrypto)

    }

    override fun getItemCount(): Int = mDiffer.currentList.size // RecyclerView item sayısı

    fun getCurrentList() : List<CryptoRecyclerModel> = mDiffer.currentList

}