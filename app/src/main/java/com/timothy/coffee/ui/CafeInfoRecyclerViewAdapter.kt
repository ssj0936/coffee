package com.timothy.coffee.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.timothy.coffee.R
import com.timothy.coffee.data.model.Cafenomad

class CafeInfoRecyclerViewAdapter:RecyclerView.Adapter<CafeInfoRecyclerViewAdapter.CafeInfoViewHolder>() {
    private lateinit var cafe:Cafenomad
    private var mList = mutableListOf<RateInfo>()

    class CafeInfoViewHolder(v:View) : RecyclerView.ViewHolder(v) {
        var title: AppCompatTextView = v.findViewById(R.id.title)
        var rate: AppCompatTextView = v.findViewById(R.id.rate)
    }

    data class RateInfo(
        var mInfoTitle:String,
        var mRate:Double
    )

    fun setCafe(cafe:Cafenomad, context: Context){
        this.cafe = cafe
        mList.clear()

        if (cafe.seatLevel > 0)
            this.mList.add(RateInfo(context.getString(R.string.label_seat_availability),cafe.seatLevel))
        if (cafe.wifiStabilityLevel > 0)
            this.mList.add(RateInfo(context.getString(R.string.label_wifi_stability),cafe.wifiStabilityLevel))
        if (cafe.tastyLevel > 0)
            this.mList.add(RateInfo(context.getString(R.string.label_tasty_level),cafe.tastyLevel))
        if (cafe.quietLevel > 0)
            this.mList.add(RateInfo(context.getString(R.string.label_quiet_level),cafe.quietLevel))
        if (cafe.priceLevel > 0)
            this.mList.add(RateInfo(context.getString(R.string.label_good_music_level),cafe.priceLevel))
        if (cafe.goodMusicLevel > 0)
            this.mList.add(RateInfo(context.getString(R.string.label_seat_availability),cafe.goodMusicLevel))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CafeInfoViewHolder {
        val root = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.cafe_info_recyclerview_item_layout,parent,false)
        return CafeInfoViewHolder(root)
    }

    override fun getItemCount(): Int = mList.size

    override fun onBindViewHolder(holder: CafeInfoViewHolder, position: Int) {
        val item = mList[position]

        holder.title.text = item.mInfoTitle
        holder.rate.text = item.mRate.toString()
        holder.rate.setTextColor(
            when {
                item.mRate>4 -> ContextCompat.getColor(holder.rate.context,R.color.rate_blue)
                item.mRate>3 -> ContextCompat.getColor(holder.rate.context,R.color.rate_green)
                item.mRate>2 -> ContextCompat.getColor(holder.rate.context,R.color.rate_yellow)
                item.mRate>1 -> ContextCompat.getColor(holder.rate.context,R.color.rate_orange)
                else -> ContextCompat.getColor(holder.rate.context,R.color.rate_red)
            }
        )
    }
}