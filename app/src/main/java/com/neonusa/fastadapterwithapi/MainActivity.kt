package com.neonusa.fastadapterwithapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.neonusa.fastadapterwithapi.helper.isInternetAvailable
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

        // library from helper to check internet connectivity
        if (this.isInternetAvailable()) {
            viewModel.getCharacters()
        } else {
            showErrorState()
        }

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
        viewModel.networkStateLiveData?.observe(this) { networkState ->
            when (networkState?.status) {
                Status.FAILED -> {
                    footerAdapter.clear()
                    binding.pbHome.visibility = View.GONE
                    createRetrySnackbar()
                    retrySnackbar?.show()
                }
                Status.SUCCESS -> {
                    footerAdapter.clear()
                    binding.pbHome.visibility = View.GONE
                }
                Status.LOADING -> {
                    if (!isFirstPageLoading) {
                        showRecyclerViewProgressIndicator()
                        Log.i("MainActivity", "setupCharactersObservables: hello !isFirstPageLoading")
                    } else {
                        isFirstPageLoading = false
                        Log.i("MainActivity", "setupCharactersObservables: Hello i am loading")
                    }
                }
                else -> {}
            }
        }

        // Observe latest video live data
        viewModel.characterLiveData?.observe(this) { characterList ->
            characterPagedModelAdapter.submitList(characterList)
        }
    }

    private fun showErrorState() {
        binding.rvMain.visibility = View.GONE
        binding.pbHome.visibility = View.GONE
        binding.errorLayout.visibility = View.VISIBLE
        binding.tvErrorHome.text = "Harap periksa koneksi internet dan coba lagi!"
    }

    private fun showRecyclerViewProgressIndicator() {
        footerAdapter.clear()
        val progressIndicatorItem = ProgressIndicatorItem()
        footerAdapter.add(progressIndicatorItem)
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

    private fun createRetrySnackbar() {
        retrySnackbar =
            Snackbar.make(binding.root, "Error get character data", Snackbar.LENGTH_INDEFINITE)
//                .setAnchorView(this.findViewById(R.id.bottomNavView) as BottomNavigationView)
                .setAction("Coba lagi") {
                    viewModel.refreshFailedRequest()
                }
    }
}