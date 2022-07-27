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

    var page = 0

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
//            page += 1
            val response = RetroInstance.getRetroInstance().create(
            RetroService::class.java).getDataFromAPI(page).body()

            // this "if" keyword is to prevent paging loading next data when error happen
            // ex : internet network is not available

            // tanpa if ini jika pada awalnya internet dimatikan
            // maka saat retry dan internet sudah tersedia, yang akan diload adalah
            // page kedua (meski response tidak ada dan error page tetap ditambahkan + 1 jika
            // tanpa if)
            if(RetroInstance.getRetroInstance().create(RetroService::class.java).getDataFromAPI(page).isSuccessful){
                page += 1
            }

            nextPageToken = response?.info?.next

            val characterList = response?.results
//            Log.i("CharDataSource", "executeQuery: $characterList")
            Log.i("CharacterDataSource", "executeQuery: $page")

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