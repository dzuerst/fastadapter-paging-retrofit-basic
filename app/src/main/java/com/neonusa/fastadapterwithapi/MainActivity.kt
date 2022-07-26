package com.neonusa.fastadapterwithapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericFastAdapter
import com.mikepenz.fastadapter.adapters.GenericItemAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.paged.PagedModelAdapter
import com.neonusa.fastadapterwithapi.databinding.ActivityMainBinding
import com.neonusa.fastadapterwithapi.model.CharacterData
import com.neonusa.fastadapterwithapi.paging.Status

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    private lateinit var viewModel: MainViewModel

    private var characterAdapter: GenericFastAdapter? = null
    private lateinit var characterPagedModelAdapter: PagedModelAdapter<CharacterData, CharacterItem>

    private lateinit var footerAdapter: GenericItemAdapter
    private var isFirstPageLoading = true
    private var retrySnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = MainViewModel()

        viewModel.getCharacters()

        setupCharactersObservables()
        setupRecyclerView(savedInstanceState)
//        onRetryButtonClick()


        // test get data from api (successfully)
//        viewModel.characterList.observe(this){
//            Log.i("MainActivity", "onCreate: $it")
//        }

    }

    private fun setupCharactersObservables() {
        // Observe network live data
        viewModel.networkStateLiveData?.observe(this, Observer { networkState ->
            when (networkState?.status) {
                Status.FAILED -> {
                    footerAdapter.clear()
//                    pbHome.makeGone()
//                    createRetrySnackbar()
                    retrySnackbar?.show()
                }
                Status.SUCCESS -> {
                    footerAdapter.clear()
//                    pbHome.makeGone()
                }
                Status.LOADING -> {
                    if (!isFirstPageLoading) {
//                        showRecyclerViewProgressIndicator()
                    } else {
                        isFirstPageLoading = false
                    }
                }
            }
        })

        // Observe latest video live data
        viewModel.characterLiveData?.observe(
            this,
            Observer<PagedList<CharacterData>> { characterList ->
                characterPagedModelAdapter.submitList(characterList)
            })
    }

    private fun setupRecyclerView(savedInstanceState: Bundle?) {
        val asyncDifferConfig = AsyncDifferConfig.Builder<CharacterData>(object :
            DiffUtil.ItemCallback<CharacterData>() {
            override fun areItemsTheSame(
                oldItem: CharacterData,
                newItem: CharacterData
            ): Boolean {
                //todo : change name to id maybe
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(
                oldItem: CharacterData,
                newItem: CharacterData
            ): Boolean {
                return oldItem == newItem
            }
        }).build()

        characterPagedModelAdapter =
            PagedModelAdapter<CharacterData, CharacterItem>(asyncDifferConfig) {
                CharacterItem(it)
            }

        footerAdapter = ItemAdapter.items()

        characterAdapter = FastAdapter.with(listOf(characterPagedModelAdapter, footerAdapter))
        characterAdapter?.registerTypeInstance(CharacterItem(null))
        characterAdapter?.withSavedInstanceState(savedInstanceState)

        binding.rvMain.layoutManager = LinearLayoutManager(this)
        binding.rvMain.adapter = characterAdapter
//        binding.rvMain.addItemDecoration(
//            DividerItemDecorator(
//                ContextCompat.getDrawable(
//                    requireContext(),
//                    R.drawable.view_divider_item_decorator
//                )!!
//            )
//        )
//        onItemClick()
    }
}