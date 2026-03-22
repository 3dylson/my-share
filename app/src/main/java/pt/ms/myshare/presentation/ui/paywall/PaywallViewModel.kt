package pt.ms.myshare.presentation.ui.paywall

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.repository.EntitlementRepository
import pt.ms.myshare.utils.logs.FirebaseUtils
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val entitlementRepository: EntitlementRepository
) : ViewModel() {

    val isPro = entitlementRepository.isPro
    val availableProducts = entitlementRepository.availableProducts

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        FirebaseUtils.logEvent("paywall_view")
    }

    fun purchasePlan(activity: Activity, product: StoreProduct) {
        FirebaseUtils.logEvent("purchase_start", Bundle().apply { 
            putString("product_id", product.productId) 
        })
        viewModelScope.launch {
            try {
                _isLoading.value = true
                entitlementRepository.purchasePlan(activity, product)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                entitlementRepository.restorePurchases()
                FirebaseUtils.logEvent("restore_success")
            } catch (e: Exception) {
                FirebaseUtils.logEvent("restore_failure")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
