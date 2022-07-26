package com.neonusa.fastadapterwithapi

import android.view.View
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.neonusa.fastadapterwithapi.model.CharacterData

class CharacterItem(val character: CharacterData?): AbstractItem<CharacterItem.ViewHolder>()  {

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<CharacterItem>(itemView){
        var tvName: TextView = itemView.findViewById(R.id.tvName)
        var tvSpecies: TextView = itemView.findViewById(R.id.tv_species)

        override fun bindView(item: CharacterItem, payloads: List<Any>) {
            tvName.text = item.character?.name
            tvSpecies.text = item.character?.species
        }

        override fun unbindView(item: CharacterItem) {
            tvName.text = null
            tvSpecies.text = null
        }

    }

    override val type: Int
        get() = R.id.fast_adapter_character_item
    override val layoutRes: Int
        get() = R.layout.recycler_row

    override fun getViewHolder(v: View): CharacterItem.ViewHolder {
        return ViewHolder(v)
    }
}