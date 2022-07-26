package com.neonusa.fastadapterwithapi.paging

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.neonusa.fastadapterwithapi.api.RetroInstance
import com.neonusa.fastadapterwithapi.api.RetroService
import com.neonusa.fastadapterwithapi.model.CharacterData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CharacterDataSource(
    private val coroutineScope: CoroutineScope
): PageKeyedDataSource<String,CharacterData>() {

    private var supervisorJob = SupervisorJob()
    private val networkState = MutableLiveData<NetworkState>()
    private var retryQuery: (() -> Any)? = null
    private var nextPageToken: String? = null

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, CharacterData>
    ) {
        retryQuery = { loadInitial(params, callback) }
        executeQuery {
            callback.onResult(it, null, nextPageToken)
        }
    }

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, CharacterData>
    ) {

    }

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, CharacterData>
    ) {
        retryQuery = { loadAfter(params, callback) }
        executeQuery {
            callback.onResult(it, nextPageToken)
        }
    }

    private fun executeQuery(callback: (List<CharacterData>) -> Unit){
        coroutineScope.launch(getJobErrorHandler() + supervisorJob) {
            val response = RetroInstance.getRetroInstance().create(
            RetroService::class.java).getDataFromAPI(1).body()

            nextPageToken = response?.info?.next

            val characterList = response?.results
            Log.i("CharDataSource", "executeQuery: $characterList")

            retryQuery = null
            networkState.postValue(NetworkState.LOADED)

            callback(characterList ?: emptyList())
        }
    }

    private fun getJobErrorHandler() = CoroutineExceptionHandler { _, e ->
        Log.e("CharacterDataSource", "getJobErrorHandler: $e", )
        networkState.postValue(
            NetworkState.error(
                e.localizedMessage
            )
        )
    }

    fun getNetworkState(): LiveData<NetworkState> = networkState

    fun retryFailedQuery() {
        val prevQuery = retryQuery
        retryQuery = null
        prevQuery?.invoke()
    }

}