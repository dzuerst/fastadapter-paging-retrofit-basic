package com.neonusa.fastadapterwithapi

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.neonusa.fastadapterwithapi.model.CharacterData

class CharacterItem(val character: CharacterData?): AbstractItem<CharacterItem.ViewHolder>()  {

    class ViewHolder(itemView: View) : FastAdapter.ViewHolder<CharacterItem>(itemView){
        var tvName: TextView = itemView.findViewById(R.id.tvName)
        var tvSpecies: TextView = itemView.findViewById(R.id.tv_species)
        var imgChar: ImageView = itemView.findViewById(R.id.imageView)

        override fun bindView(item: CharacterItem, payloads: List<Any>) {
            tvName.text = item.character?.name
            tvSpecies.text = item.character?.species

            Glide.with(imgChar)
                .load(item.character?.image)
                .circleCrop()
                .into(imgChar)
        }

        override fun unbindView(item: CharacterItem) {
            tvName.text = null
            tvSpecies.text = null

            imgChar.setImageDrawable(null)
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