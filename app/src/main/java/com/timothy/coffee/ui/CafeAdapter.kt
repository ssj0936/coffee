package com.timothy.coffee.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.data.model.CafenomadDisplay
import com.timothy.coffee.databinding.CafeRecyclerviewItemLayoutBinding
import timber.log.Timber

class CafeAdapter(
    private val cafes:List<CafenomadDisplay>,
    private val listener:OnCafeAdapterClickListener
): RecyclerView.Adapter<CafeAdapter.ViewHolder>(){

    //hold item view's reference
    class ViewHolder(val binding: CafeRecyclerviewItemLayoutBinding) :RecyclerView.ViewHolder(binding.root){
        fun bind(cafe:CafenomadDisplay){
            binding.cafeinfo = cafe
            binding.executePendingBindings()
        }
    }

    //create a new view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CafeRecyclerviewItemLayoutBinding.inflate(layoutInflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return cafes.size
    }

    //replacing content of view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cafeDisplay = cafes[position]
        holder.bind(cafeDisplay)

        holder.binding.root.setOnClickListener{
            listener.onItemClick(cafeDisplay)
        }
    }

    interface OnCafeAdapterClickListener{
        fun onItemClick(cafe:CafenomadDisplay)
    }
}