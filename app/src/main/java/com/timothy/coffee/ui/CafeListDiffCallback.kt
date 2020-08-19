package com.timothy.coffee.ui

import androidx.recyclerview.widget.DiffUtil
import com.timothy.coffee.data.model.CafenomadDisplay

class CafeListDiffCallback(
    private val newData:List<CafenomadDisplay>,
    private val oldData:List<CafenomadDisplay>
): DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldData[oldItemPosition].cafenomad.id == newData[newItemPosition].cafenomad.id

    override fun getOldListSize(): Int = oldData.size

    override fun getNewListSize(): Int = newData.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
            = oldData[oldItemPosition].isFavorite == newData[newItemPosition].isFavorite
}