package com.example.monatechickensteak.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monatechickensteak.R
import com.example.monatechickensteak.data.CartManager
import com.example.monatechickensteak.data.OrderRepository
import com.example.monatechickensteak.databinding.FragmentOrdersBinding
import com.example.monatechickensteak.model.Order
import com.google.android.material.snackbar.Snackbar

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderAdapter(
            onAdvance = { OrderRepository.advanceStatus(it.id) },
            onReorder = { reorder(it) }
        )
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrders.adapter = adapter

        OrderRepository.orders.observe(viewLifecycleOwner) { orders ->
            adapter.submitList(orders)
            binding.tvEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun reorder(order: Order) {
        order.items.forEach { line -> repeat(line.quantity) { CartManager.add(line.item) } }
        Snackbar.make(binding.root, "Items from order #${order.id} added to cart", Snackbar.LENGTH_LONG)
            .setAction("View cart") { findNavController().navigate(R.id.cartFragment) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}