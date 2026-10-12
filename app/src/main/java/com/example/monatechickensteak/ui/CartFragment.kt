package com.example.monatechickensteak.ui

import com.example.monatechickensteak.data.AuthRepository
import com.example.monatechickensteak.data.SessionManager
import com.example.monatechickensteak.util.LoyaltyRules
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.monatechickensteak.R
import com.example.monatechickensteak.data.CartManager
import com.example.monatechickensteak.data.OrderRepository
import com.example.monatechickensteak.databinding.FragmentCartBinding
import com.example.monatechickensteak.util.CheckoutValidator
import com.example.monatechickensteak.util.toRand
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CartAdapter

    private val timeOptions = listOf(
        "As soon as possible",
        "In 15 minutes",
        "In 30 minutes",
        "In 1 hour"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CartAdapter(
            onPlus = { CartManager.increase(it.item.id) },
            onMinus = { CartManager.decrease(it.item.id) },
            onRemove = { CartManager.remove(it.item.id) }
        )
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter

        // Collection time dropdown
        binding.dropdownTime.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, timeOptions)
        )
        binding.dropdownTime.setText(timeOptions.first(), false)

        binding.btnBrowse.setOnClickListener { findNavController().navigate(R.id.menuFragment) }
        binding.btnPlaceOrder.setOnClickListener { placeOrder() }

        // Redraw the screen every time the cart changes
        CartManager.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            val empty = items.isEmpty()
            binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE
            binding.content.visibility = if (empty) View.GONE else View.VISIBLE

            binding.tvSubtotal.text = CartManager.subtotal().toRand()
            binding.tvDelivery.text = CartManager.deliveryFee().toRand()
            binding.tvTotal.text = CartManager.total().toRand()
        }
    }

    private fun selectedPayment(): String? {
        val id = binding.rgPayment.checkedRadioButtonId
        return if (id == -1) null else binding.root.findViewById<RadioButton>(id).text.toString()
    }

    private fun placeOrder() {
        val items = CartManager.items.value.orEmpty()
        val payment = selectedPayment()
        val error = CheckoutValidator.error(items.isEmpty(), payment)
        if (error != null) {
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            return
        }
        // Ordering is only available to authenticated account holders. Keep the cart intact
        // and let guests choose either sign-in or registration before continuing.
        if (!SessionManager.isLoggedIn() || SessionManager.apiToken().isBlank()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Account required")
                .setMessage("Please sign in or create an account before placing your order. Your cart will be saved while you do this.")
                .setNegativeButton("Not now", null)
                .setNeutralButton("Create account") { _, _ ->
                    findNavController().navigate(R.id.registerFragment)
                }
                .setPositiveButton("Sign in") { _, _ ->
                    findNavController().navigate(R.id.loginFragment)
                }
                .show()
            return
        }

        binding.btnPlaceOrder.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = com.example.monatechickensteak.data.ApiClient.service.placeOrder(
                    "Bearer ${SessionManager.apiToken()}",
                    com.example.monatechickensteak.data.PlaceOrderRequest(
                        items.map { com.example.monatechickensteak.data.PlaceOrderItem(it.item.id, it.quantity) },
                        binding.dropdownTime.text?.toString().orEmpty().ifBlank { "As soon as possible" },
                        payment!!
                    )
                )
                val placed = response.order
                if (!response.success || placed == null) {
                    Snackbar.make(binding.root, response.message ?: "Order could not be placed", Snackbar.LENGTH_LONG).show()
                    binding.btnPlaceOrder.isEnabled = true
                    return@launch
                }
                val order = OrderRepository.placeOrder(
                    items = items,
                    total = placed.total,
                    collectionTime = binding.dropdownTime.text?.toString().orEmpty(),
                    paymentMethod = payment,
                    serverOrderId = placed.id
                )
                CartManager.clear()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Order placed! 🎉")
                    .setMessage("Order #${placed.id}\nTotal: ${order.total.toRand()}\nCollection: ${order.collectionTime}\nPayment: ${order.paymentMethod}\nLoyalty points earned: +${placed.loyaltyPointsEarned}")
                    .setCancelable(false)
                    .setPositiveButton("Track order") { _, _ -> findNavController().navigate(R.id.ordersFragment) }
                    .show()
            } catch (e: Exception) {
                Snackbar.make(binding.root, "Could not reach the order API. Your cart has been kept. Check your connection.", Snackbar.LENGTH_LONG).show()
            } finally {
                if (_binding != null) binding.btnPlaceOrder.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}