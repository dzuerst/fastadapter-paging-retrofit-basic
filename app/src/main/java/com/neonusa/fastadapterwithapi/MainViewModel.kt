package com.neonusa.fastadapterwithapi

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.neonusa.fastadapterwithapi.helper.ResultWrapper
import com.neonusa.fastadapterwithapi.model.CharacterData
import com.neonusa.fastadapterwithapi.paging.CharacterDataSourceFactory
import com.neonusa.fastadapterwithapi.paging.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    lateinit var characterDsFactory: CharacterDataSourceFactory
    var characterLiveData: LiveData<PagedList<CharacterData>>? = null
    var networkStateLiveData: LiveData<NetworkState>? = null

    fun getCharacters() {
        if (characterLiveData == null) {
            viewModelScope.launch {
                characterDsFactory = CharacterDataSourceFactory(viewModelScope)
//                characterLiveData.value = ResultWrapper.Loading
//
//                val uploadsPlaylistIdRequest = async(Dispatchers.IO) { homeRepository.getUploadsPlaylistId(channelId) }
//                val response = uploadsPlaylistIdRequest.await()
                    characterLiveData = LivePagedListBuilder(characterDsFactory, pagedListConfig()).build()
                    networkStateLiveData = Transformations.switchMap(characterDsFactory.characterDataSourceLiveData) {
                        it.getNetworkState()
                    }
            }

        }

    }

    /**
     * Retry possible last paged request (ie: network issue)
     */
    fun refreshFailedRequest() =
        characterDsFactory.getSource()?.retryFailedQuery()

    private fun pagedListConfig() = PagedList.Config.Builder()
        .setInitialLoadSizeHint(10)
        .setEnablePlaceholders(false)
        .setPageSize(10)
        .build()














//    init {
//        getCharacters()
//    }
//    var characterList: MutableLiveData<List<CharacterData>> = MutableLiveData<List<CharacterData>>()


    // from this method data is loaded successfully via log
//    fun getCharacters() = CoroutineScope(Dispatchers.Main).launch {
//        val response = RetroInstance.getRetroInstance().create(RetroService::class.java).getDataFromAPI(1)
//        Log.i("HomeViewModel","LOADING...")
//        if(response.isSuccessful){
//            Log.i("HomeViewModel","getCharacters : ${response.body()?.results}")
//            characterList.postValue(response.body()?.results)
//        }else{
//            Log.e("error","${response.code().toString()} ${response.message()}")
//        }
//    }

}