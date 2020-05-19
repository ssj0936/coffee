package com.timothy.coffee.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.databinding.CafeRecyclerviewItemLayoutBinding
import com.timothy.coffee.util.LonAndLat
import com.timothy.coffee.util.Util
import timber.log.Timber

class CafeAdapter(
    private val cafes:List<Cafenomad>,
    private val currentLocation:LonAndLat?,
    private val listener:OnCafeAdapterClickListener
): RecyclerView.Adapter<CafeAdapter.ViewHolder>(){

    //hold item view's reference
    class ViewHolder(val binding: CafeRecyclerviewItemLayoutBinding) :RecyclerView.ViewHolder(binding.root)

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
        val cafe:Cafenomad = cafes[position]

        holder.binding.cafeName.text = cafe.name
        holder.binding.cafeAddress.text = if(cafe.distance==null) "" else "${cafe.distance} M"
        holder.binding.tastyRating.rating = cafe.tastyLevel.toFloat()
        holder.binding.root.setOnClickListener{
            listener.onItemClick(cafe)
        }
    }

    interface OnCafeAdapterClickListener{
        fun onItemClick(cafe:Cafenomad)
    }
}