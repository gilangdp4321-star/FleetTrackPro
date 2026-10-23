package com.industri.fleettrack.ui.manifest

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.industri.fleettrack.data.local.AppDatabase
import com.industri.fleettrack.data.local.entity.DeliveryOrderEntity
import com.industri.fleettrack.data.repository.DeliveryRepository
import com.industri.fleettrack.databinding.ActivityManifestBinding
import kotlinx.coroutines.launch

class ManifestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManifestBinding
    private lateinit var adapter: DeliveryListAdapter

    private val viewModel: ManifestViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = DeliveryRepository(database)
        ManifestViewModelFactory(repository)
    }

    private var fullOrdersList: List<DeliveryOrderEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManifestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeToRefresh()
        setupSearchBar()
        observeUiState()
    }

    private fun setupRecyclerView() {
        adapter = DeliveryListAdapter { order ->
            viewModel.updateOrderStatus(order.orderId, "DELIVERED")
            Toast.makeText(
                this,
                "Status resi ${order.trackingNumber} diperbarui menjadi DELIVERED",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.rvManifest.layoutManager = LinearLayoutManager(this)
        binding.rvManifest.adapter = adapter
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshManifest()
        }
    }

    private fun setupSearchBar() {
        binding.etSearchManifest.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterList(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            adapter.submitList(fullOrdersList.toMutableList())
        } else {
            val filtered = fullOrdersList.filter { order ->
                order.trackingNumber.contains(trimmedQuery, ignoreCase = true) ||
                        order.recipientName.contains(trimmedQuery, ignoreCase = true) ||
                        order.destinationAddress.contains(trimmedQuery, ignoreCase = true)
            }
            adapter.submitList(filtered.toMutableList())
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ManifestUiState.Loading -> {
                            if (!binding.swipeRefresh.isRefreshing) {
                                binding.pbMainLoading.visibility = View.VISIBLE
                            }
                        }
                        is ManifestUiState.Success -> {
                            binding.pbMainLoading.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            fullOrdersList = state.orders
                            val currentQuery = binding.etSearchManifest.text.toString()
                            filterList(currentQuery)
                        }
                        is ManifestUiState.Error -> {
                            binding.pbMainLoading.visibility = View.GONE
                            binding.swipeRefresh.isRefreshing = false
                            Toast.makeText(
                                this@ManifestActivity,
                                state.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}
