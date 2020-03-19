package com.timothy.coffee.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timothy.coffee.data.model.Cafenomad
import com.timothy.coffee.databinding.CafeRecyclerviewItemLayoutBinding

class CafeAdapter(private val cafes:List<Cafenomad>): RecyclerView.Adapter<CafeAdapter.ViewHolder>(){

    //hold item view's reference
    class ViewHolder(val binding: CafeRecyclerviewItemLayoutBinding) :RecyclerView.ViewHolder(binding.root)

    //create a new view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CafeRecyclerviewItemLayoutBinding.inflate(layoutInflater,parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = cafes.size

    //replacing content of view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cafe:Cafenomad = cafes[position]

        holder.binding.cafeAddress.text = cafe.address
        holder.binding.cafeName.text = cafe.name
    }
}