package com.neonusa.fastadapterwithapi.paging

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.neonusa.fastadapterwithapi.model.CharacterData
import kotlinx.coroutines.CoroutineScope

class CharacterDataSourceFactory(
    private val coroutineScope: CoroutineScope

    ): DataSource.Factory<String, CharacterData>() {

    private var characterDataSource: CharacterDataSource? = null
    val characterDataSourceLiveData = MutableLiveData<CharacterDataSource>()

    override fun create(): DataSource<String, CharacterData> {
        if (characterDataSource == null) {
            characterDataSource = CharacterDataSource(coroutineScope)
            characterDataSourceLiveData.postValue(characterDataSource)
        }
        return characterDataSource!!
    }

    fun getSource() = characterDataSourceLiveData.value

}