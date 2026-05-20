package pt.ms.myshare.presentation.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import kotlinx.coroutines.launch
import pt.ms.myshare.BuildConfig
import pt.ms.myshare.R
import pt.ms.myshare.domain.model.BillingPlan
import pt.ms.myshare.domain.model.OnboardingPaywallVariant
import pt.ms.myshare.domain.model.PlanPreview
import pt.ms.myshare.domain.model.PremiumStoreProductSelector
import pt.ms.myshare.domain.model.PricingStrategy
import pt.ms.myshare.domain.model.StoreProduct
import pt.ms.myshare.domain.model.UserPreferences
import pt.ms.myshare.presentation.ui.auth.GoogleIdTokenReadResult
import pt.ms.myshare.presentation.ui.auth.GoogleIdTokenReader
import pt.ms.myshare.presentation.ui.components.GoogleSignInButton
import pt.ms.myshare.presentation.ui.components.KeyboardDismissEffect
import pt.ms.myshare.presentation.ui.components.PremiumButton
import pt.ms.myshare.presentation.ui.components.PremiumInfoCard
import pt.ms.myshare.presentation.ui.components.PremiumPaywallCard
import pt.ms.myshare.presentation.ui.components.rememberKeyboardDismissOnScrollConnection
import pt.ms.myshare.presentation.ui.formatting.SubscriptionSavingsFormatter
import pt.ms.myshare.presentation.ui.paywall.PaywallAutopilotPreviewMapper
import pt.ms.myshare.presentation.ui.paywall.PaywallAutopilotPreviewUiState
import pt.ms.myshare.presentation.ui.theme.*
import timber.log.Timber
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaywallScreen(
    pricingStrategy: PricingStrategy,
    userPreferences: UserPreferences,
    planPreview: PlanPreview? = null,
    goalName: String = "",
    availableProducts: List<StoreProduct> = emptyList(),
    selectedPlan: BillingPlan,
    paywallVariant: OnboardingPaywallVariant = OnboardingPaywallVariant.PAYDAY_PROOF,
    isBillingActionInProgress: Boolean = false,
    billingMessage: String? = null,
    showSecurePremiumAccessPrompt: Boolean = false,
    isGoogleConnectionInProgress: Boolean = false,
    googleConnectionMessage: String? = null,
    googleConnectionError: String? = null,
    onPlanSelected: (BillingPlan) -> Unit,
    onClose: () -> Unit,
    onRestore: () -> Unit,
    onPurchaseSelected: (android.app.Activity) -> Unit,
    onConnectGoogleAccount: (String) -> Unit = {},
    onGoogleConnectionCredentialError: (String) -> Unit = {},
    onContinueWithoutSecuring: () -> Unit = {}
) {
    val activity = androidx.activity.compose.LocalActivity.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val fontScale = LocalDensity.current.fontScale
    val paywallLayoutMode = remember(configuration.screenHeightDp, fontScale) {
        PaywallLayoutMode.from(configuration.screenHeightDp, fontScale)
    }
    val isCompactHeight = paywallLayoutMode == PaywallLayoutMode.Compact
    val coroutineScope = rememberCoroutineScope()
    val googleIdTokenReader = remember(context) {
        GoogleIdTokenReader(
            credentialManager = CredentialManager.create(context),
            serverClientId = BuildConfig.GOOGLE_CLIENT_ID
        )
    }
    val scrollState = rememberScrollState()
    val keyboardDismissOnScrollConnection = rememberKeyboardDismissOnScrollConnection()
    var isGoogleCredentialRequestInProgress by remember { mutableStateOf(false) }
    val monthlyProduct = PremiumStoreProductSelector.standardProduct(availableProducts, BillingPlan.MONTHLY)
    val annualProduct = PremiumStoreProductSelector.standardProduct(availableProducts, BillingPlan.ANNUAL)
    val annualComparison = remember(monthlyProduct, annualProduct) {
        SubscriptionSavingsFormatter.formatAnnualComparison(
            monthlyProduct = monthlyProduct,
            annualProduct = annualProduct,
            locale = Locale.getDefault()
        )
    }
    val autopilotPreview = remember(planPreview, userPreferences) {
        PaywallAutopilotPreviewMapper.map(planPreview, userPreferences)
    }
    val selectedProduct = when (selectedPlan) {
        BillingPlan.MONTHLY -> monthlyProduct
        BillingPlan.ANNUAL -> annualProduct
    }
    val selectedPeriod = when (selectedPlan) {
        BillingPlan.MONTHLY -> stringResource(R.string.paywall_period_month)
        BillingPlan.ANNUAL -> stringResource(R.string.paywall_period_year)
    }
    val selectedTrialDays = selectedProduct?.freeTrialDays?.takeIf { it > 0 }
    val displayTrialDays = availableProducts
        .mapNotNull { product -> product.freeTrialDays?.takeIf { days -> days > 0 } }
        .maxOrNull()
    val currencyMismatchNotice = selectedProduct?.priceCurrencyCode
        ?.takeUnless { it.equals(userPreferences.currencyCode, ignoreCase = true) }
        ?.let {
            stringResource(
                R.string.paywall_currency_mismatch_notice,
                userPreferences.currencyCode,
                it
            )
        }
    val checkoutTerms = when {
        selectedProduct == null -> stringResource(R.string.paywall_footer_store_terms_unavailable)
        selectedTrialDays != null -> stringResource(
            R.string.paywall_footer_trial_terms,
            selectedTrialDays,
            selectedProduct.price,
            selectedPeriod
        )
        else -> stringResource(
            R.string.paywall_footer_no_trial_terms,
            selectedProduct.price,
            selectedPeriod
        )
    }
    val isPurchaseReady = selectedProduct != null && !isBillingActionInProgress
    val resolvedBillingMessage = remember(billingMessage, context) {
        billingMessage?.let {
            val resId = context.resources.getIdentifier(it, "string", context.packageName)
            if (resId != 0) context.getString(resId) else it
        }
    }
    val googleConnectionFeedback = remember(googleConnectionMessage, googleConnectionError, context) {
        val messageKey = googleConnectionError ?: googleConnectionMessage
        messageKey?.let {
            val resId = context.resources.getIdentifier(it, "string", context.packageName)
            if (resId != 0) context.getString(resId) else it
        }
    }
    val startGoogleConnection: () -> Unit = {
        if (!isGoogleCredentialRequestInProgress && !isGoogleConnectionInProgress) {
            coroutineScope.launch {
                isGoogleCredentialRequestInProgress = true
                when (val result = googleIdTokenReader.readIdToken(context, "onboarding_paywall")) {
                    is GoogleIdTokenReadResult.Success -> onConnectGoogleAccount(result.idToken)
                    GoogleIdTokenReadResult.NoCredential -> {
                        Timber.e("Google account link failed because no credential is available")
                        onGoogleConnectionCredentialError("home_more_account_connect_google_error_no_credentials")
                    }
                    GoogleIdTokenReadResult.UnsupportedCredential -> {
                        onGoogleConnectionCredentialError("home_more_account_connect_google_error_generic")
                    }
                    is GoogleIdTokenReadResult.Failure -> {
                        onGoogleConnectionCredentialError("home_more_account_connect_google_error_generic")
                    }
                }
                isGoogleCredentialRequestInProgress = false
            }
        }
    }

    KeyboardDismissEffect(showSecurePremiumAccessPrompt)
    LaunchedEffect(showSecurePremiumAccessPrompt) {
        if (showSecurePremiumAccessPrompt) {
            scrollState.scrollTo(0)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showSecurePremiumAccessPrompt) {
                SecurePremiumAccessFooter(onContinue = onContinueWithoutSecuring)
            } else {
                PaywallPurchaseFooter(
                    checkoutTerms = checkoutTerms,
                    ctaText = when {
                        selectedProduct == null -> stringResource(R.string.paywall_price_loading)
                        selectedTrialDays != null -> stringResource(R.string.paywall_start_trial_button)
                        else -> stringResource(R.string.paywall_upgrade_button)
                    },
                    billingMessage = resolvedBillingMessage,
                    currencyMismatchNotice = currencyMismatchNotice,
                    isBillingActionInProgress = isBillingActionInProgress,
                    isPurchaseEnabled = isPurchaseReady,
                    onPurchaseClick = {
                        if (isPurchaseReady) {
                            activity?.let(onPurchaseSelected)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .nestedScroll(keyboardDismissOnScrollConnection)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Close, 
                        contentDescription = stringResource(R.string.paywall_close_content_description), 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                TextButton(
                    onClick = onRestore,
                    enabled = !isBillingActionInProgress
                ) { 
                    Text(stringResource(R.string.paywall_restore_button), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                }
            }
            
            Spacer(Modifier.height(8.dp))

            val headline = stringResource(paywallVariant.headlineRes)
            val subhead = stringResource(paywallVariant.subheadRes)

            if (showSecurePremiumAccessPrompt) {
                PaywallPurchaseSuccessContent(compactHeight = isCompactHeight)

                Spacer(Modifier.height(if (isCompactHeight) 16.dp else 22.dp))

                SecurePremiumAccessCard(
                    isLoading = isGoogleConnectionInProgress || isGoogleCredentialRequestInProgress,
                    feedback = googleConnectionFeedback,
                    isError = googleConnectionError != null,
                    onConnectGoogle = startGoogleConnection
                )

                Spacer(Modifier.height(24.dp))
            } else {
                PaywallAutopilotPreviewCard(
                    state = autopilotPreview,
                    goalName = goalName,
                    headline = headline,
                    subhead = subhead,
                    badge = if (displayTrialDays != null) {
                        stringResource(R.string.paywall_hero_trial_badge, displayTrialDays)
                    } else {
                        stringResource(R.string.premium_badge)
                    },
                    compactHeight = isCompactHeight
                )

                Spacer(Modifier.height(if (isCompactHeight) 16.dp else 22.dp))

                PaywallSectionLabel(text = stringResource(R.string.paywall_plan_section_title))

                Column(verticalArrangement = Arrangement.spacedBy(if (paywallLayoutMode == PaywallLayoutMode.Compact) 8.dp else 12.dp)) {
                    val hasLivePrices = monthlyProduct != null || annualProduct != null
                    val planCards = remember(pricingStrategy.heroPlan, monthlyProduct, annualProduct, selectedPlan, hasLivePrices) {
                        val monthlyCard = PaywallPlanCardState(
                            plan = BillingPlan.MONTHLY,
                            product = monthlyProduct,
                            badgeKey = if (pricingStrategy.heroPlan == BillingPlan.MONTHLY) {
                                R.string.paywall_badge_easy_start
                            } else {
                                null
                            }
                        )
                        val annualCard = PaywallPlanCardState(
                            plan = BillingPlan.ANNUAL,
                            product = annualProduct,
                            badgeKey = if (hasLivePrices) R.string.paywall_badge_best_value else null
                        )
                        if (pricingStrategy.heroPlan == BillingPlan.ANNUAL) {
                            listOf(annualCard, monthlyCard)
                        } else {
                            listOf(monthlyCard, annualCard)
                        }
                    }

                    planCards.forEach { card ->
                        val title = stringResource(card.plan.titleRes)
                        val price = card.product?.price ?: stringResource(R.string.paywall_price_loading)
                        val period = stringResource(card.plan.periodRes)
                        val description = stringResource(card.plan.descriptionRes)
                        val badge = card.badgeKey?.let { stringResource(it) }
                        val comparisonPrice = if (card.plan == BillingPlan.ANNUAL) {
                            annualComparison?.monthlyEquivalentPrice
                        } else {
                            null
                        }
                        val savingsLabel = if (card.plan == BillingPlan.ANNUAL) {
                            annualComparison?.savingsPrice?.let { stringResource(R.string.paywall_annual_savings_label, it) }
                        } else {
                            null
                        }
                        if (paywallLayoutMode == PaywallLayoutMode.Compact) {
                            CompactPaywallPlanRow(
                                title = title,
                                price = price,
                                period = period,
                                description = description,
                                savingsLabel = savingsLabel,
                                isSelected = selectedPlan == card.plan,
                                onClick = { onPlanSelected(card.plan) }
                            )
                        } else {
                            PremiumPaywallCard(
                                title = title,
                                price = price,
                                period = period,
                                description = description,
                                badge = badge,
                                comparisonPrice = comparisonPrice,
                                savingsLabel = savingsLabel,
                                isSelected = selectedPlan == card.plan,
                                onClick = { onPlanSelected(card.plan) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(if (isCompactHeight) 16.dp else 22.dp))

                if (paywallLayoutMode != PaywallLayoutMode.Compact) {
                    PaywallTrustList()
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private enum class PaywallLayoutMode {
    Compact,
    Standard,
    Expanded;

    companion object {
        fun from(screenHeightDp: Int, fontScale: Float): PaywallLayoutMode = when {
            screenHeightDp < 720 || fontScale >= 1.25f -> Compact
            screenHeightDp >= 880 && fontScale < 1.15f -> Expanded
            else -> Standard
        }
    }
}

@Composable
private fun PaywallPurchaseSuccessContent(
    compactHeight: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(if (compactHeight) 16.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(if (compactHeight) 12.dp else 16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MySharePrimary.copy(alpha = 0.18f)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.padding(12.dp).size(if (compactHeight) 30.dp else 36.dp)
                )
            }
            Text(
                text = stringResource(R.string.paywall_purchase_success_title),
                style = if (compactHeight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = if (compactHeight) 29.sp else 34.sp
            )
            Text(
                text = stringResource(R.string.paywall_purchase_success_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )
        }
    }

    Spacer(Modifier.height(14.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PaywallPurchaseSuccessStep(
            icon = Icons.Default.Visibility,
            title = stringResource(R.string.paywall_purchase_success_watch_title),
            body = stringResource(R.string.paywall_purchase_success_watch_body)
        )
        PaywallPurchaseSuccessStep(
            icon = Icons.Default.EventAvailable,
            title = stringResource(R.string.paywall_purchase_success_review_title),
            body = stringResource(R.string.paywall_purchase_success_review_body)
        )
        PaywallPurchaseSuccessStep(
            icon = Icons.Default.AutoAwesome,
            title = stringResource(R.string.paywall_purchase_success_next_move_title),
            body = stringResource(R.string.paywall_purchase_success_next_move_body)
        )
    }
}

@Composable
private fun PaywallPurchaseSuccessStep(
    icon: ImageVector,
    title: String,
    body: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MySharePrimary.copy(alpha = 0.14f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PaywallAutopilotPreviewCard(
    state: PaywallAutopilotPreviewUiState,
    goalName: String,
    headline: String,
    subhead: String,
    badge: String,
    compactHeight: Boolean,
    modifier: Modifier = Modifier
) {
    val recommendationBody = when {
        state.suggestedAdjustmentAmount != null && goalName.isNotBlank() -> stringResource(
            R.string.paywall_autopilot_recommendation_body,
            state.suggestedAdjustmentAmount,
            goalName,
            state.weeklyFlexibleSpend.orEmpty()
        )
        state.hasPersonalPlan -> stringResource(
            R.string.paywall_autopilot_recommendation_body_without_priority,
            state.weeklyFlexibleSpend.orEmpty()
        )
        else -> stringResource(R.string.paywall_autopilot_recommendation_body_generic)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(if (compactHeight) 12.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(if (compactHeight) 8.dp else 14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MySharePrimary
            ) {
                Text(
                    text = badge.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Text(
                text = headline,
                style = if (compactHeight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = if (compactHeight) 23.sp else 31.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (!compactHeight) {
                Text(
                    text = subhead,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                    lineHeight = 21.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(if (compactHeight) 8.dp else 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                if (!compactHeight) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MySharePrimary.copy(alpha = 0.14f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MySharePrimary,
                            modifier = Modifier.padding(8.dp).size(22.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (goalName.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.paywall_autopilot_preview_goal_label, goalName),
                            style = MaterialTheme.typography.labelSmall,
                            color = MySharePrimary,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 14.sp
                        )
                    }
                    Text(
                        text = stringResource(R.string.paywall_autopilot_recommendation_title),
                        style = if (compactHeight) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = recommendationBody,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                        lineHeight = 18.sp
                    )
                }
            }

            if (!compactHeight) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.14f))

                PaywallAutopilotComparisonGrid()
            }
        }
    }
}

@Composable
private fun PaywallAutopilotComparisonGrid() {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val shouldStack = maxWidth < 330.dp || LocalDensity.current.fontScale >= 1.3f
        if (shouldStack) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaywallAutopilotComparisonPill(
                    label = stringResource(R.string.paywall_autopilot_preview_free_label),
                    body = stringResource(R.string.paywall_autopilot_preview_free_body),
                    icon = Icons.Default.RadioButtonUnchecked,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                PaywallAutopilotComparisonPill(
                    label = stringResource(R.string.premium_badge),
                    body = stringResource(R.string.paywall_autopilot_preview_premium_body),
                    icon = Icons.Default.CheckCircle,
                    iconColor = MySharePrimary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PaywallAutopilotComparisonPill(
                    label = stringResource(R.string.paywall_autopilot_preview_free_label),
                    body = stringResource(R.string.paywall_autopilot_preview_free_body),
                    icon = Icons.Default.RadioButtonUnchecked,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                PaywallAutopilotComparisonPill(
                    label = stringResource(R.string.premium_badge),
                    body = stringResource(R.string.paywall_autopilot_preview_premium_body),
                    icon = Icons.Default.CheckCircle,
                    iconColor = MySharePrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PaywallAutopilotComparisonPill(
    label: String,
    body: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = iconColor,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CompactPaywallPlanRow(
    title: String,
    price: String,
    period: String,
    description: String,
    savingsLabel: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        if (isSelected) MySharePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        label = "compactPaywallPlanBorder"
    )
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface,
        label = "compactPaywallPlanBackground"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = savingsLabel ?: description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(R.string.price_per_period, price, period),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isSelected) stringResource(R.string.content_description_selected) else null,
                tint = if (isSelected) MySharePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PaywallHeroCard(
    headline: String,
    subhead: String,
    badge: String,
    compactHeight: Boolean,
    modifier: Modifier = Modifier
) {
    val cardPadding = if (compactHeight) 12.dp else 22.dp
    val iconPadding = if (compactHeight) 9.dp else 12.dp
    val iconSize = if (compactHeight) 22.dp else 28.dp
    val verticalSpacing = if (compactHeight) 8.dp else 14.dp
    val headlineStyle = if (compactHeight) {
        MaterialTheme.typography.titleLarge
    } else {
        MaterialTheme.typography.headlineMedium
    }
    val headlineLineHeight = if (compactHeight) 26.sp else 34.sp
    val bodyStyle = if (compactHeight) {
        MaterialTheme.typography.bodySmall
    } else {
        MaterialTheme.typography.bodyMedium
    }
    val bodyLineHeight = if (compactHeight) 17.sp else 21.sp

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!compactHeight) {
                Surface(
                    shape = CircleShape,
                    color = MySharePrimary.copy(alpha = 0.18f)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = MySharePrimary,
                        modifier = Modifier.padding(iconPadding).size(iconSize)
                    )
                }
                Spacer(Modifier.height(verticalSpacing))
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MySharePrimary
            ) {
                Text(
                    text = badge.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(verticalSpacing))
            Text(
                headline,
                style = headlineStyle,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = headlineLineHeight,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                subhead,
                style = bodyStyle,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                lineHeight = bodyLineHeight,
                modifier = Modifier.fillMaxWidth().padding(top = if (compactHeight) 4.dp else 8.dp)
            )
        }
    }
}

@Composable
private fun PaywallSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    )
}

@Composable
private fun PaywallPurchaseFooter(
    checkoutTerms: String,
    ctaText: String,
    billingMessage: String?,
    currencyMismatchNotice: String?,
    isBillingActionInProgress: Boolean,
    isPurchaseEnabled: Boolean,
    onPurchaseClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (billingMessage != null) {
                PaywallFooterBillingNotice(
                    message = billingMessage,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            Text(
                checkoutTerms,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            if (currencyMismatchNotice != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    currencyMismatchNotice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(10.dp))
            PremiumButton(
                text = if (isBillingActionInProgress) {
                    stringResource(R.string.paywall_upgrade_loading)
                } else {
                    ctaText
                },
                onClick = onPurchaseClick,
                enabled = isPurchaseEnabled,
                isLoading = isBillingActionInProgress
            )
        }
    }
}

@Composable
private fun SecurePremiumAccessFooter(
    onContinue: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PremiumButton(
                text = stringResource(R.string.paywall_secure_account_continue),
                onClick = onContinue
            )
        }
    }
}

@Composable
private fun SecurePremiumAccessCard(
    isLoading: Boolean,
    feedback: String?,
    isError: Boolean,
    onConnectGoogle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MySharePrimary.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MySharePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.paywall_secure_account_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.paywall_secure_account_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                        lineHeight = 18.sp
                    )
                }
            }
            if (feedback != null) {
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            GoogleSignInButton(
                text = stringResource(R.string.paywall_secure_account_button),
                isLoading = isLoading,
                onClick = onConnectGoogle
            )
        }
    }
}

@Composable
private fun PaywallFooterBillingNotice(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PaywallTrustList() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PaywallTrustRow(
            icon = Icons.Default.Payments,
            title = stringResource(R.string.paywall_trust_play_title),
            body = stringResource(R.string.paywall_trust_play_body)
        )
        PaywallTrustRow(
            icon = Icons.Default.Lock,
            title = stringResource(R.string.paywall_trust_manual_title),
            body = stringResource(R.string.paywall_trust_manual_body)
        )
        PaywallTrustRow(
            icon = Icons.Default.Cancel,
            title = stringResource(R.string.paywall_trust_cancel_title),
            body = stringResource(R.string.paywall_trust_cancel_body)
        )
    }
}

@Composable
private fun PaywallTrustRow(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.padding(7.dp).size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun PaywallFeatureGrid() {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val shouldStack = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.3f
        val features = listOf(
            stringResource(R.string.paywall_feature_automation_title) to stringResource(R.string.paywall_feature_automation_desc),
            stringResource(R.string.paywall_feature_reminders_title) to stringResource(R.string.paywall_feature_reminders_desc),
            stringResource(R.string.paywall_feature_goals_title) to stringResource(R.string.paywall_feature_goals_desc),
            stringResource(R.string.paywall_feature_tracking_title) to stringResource(R.string.paywall_feature_tracking_desc)
        )
        if (shouldStack) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                features.forEach { (title, body) ->
                    CompactPaywallFeature(title = title, body = body)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                features.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { (title, body) ->
                            CompactPaywallFeature(
                                title = title,
                                body = body,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactPaywallFeature(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MySharePrimary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class PaywallPlanCardState(
    val plan: BillingPlan,
    val product: StoreProduct?,
    val badgeKey: Int?
)

private val BillingPlan.titleRes: Int
    get() = when (this) {
        BillingPlan.MONTHLY -> R.string.paywall_plan_monthly
        BillingPlan.ANNUAL -> R.string.paywall_plan_annual
    }

private val BillingPlan.periodRes: Int
    get() = when (this) {
        BillingPlan.MONTHLY -> R.string.paywall_period_month
        BillingPlan.ANNUAL -> R.string.paywall_period_year
    }

private val BillingPlan.descriptionRes: Int
    get() = when (this) {
        BillingPlan.MONTHLY -> R.string.paywall_desc_monthly
        BillingPlan.ANNUAL -> R.string.paywall_desc_annual
    }

private val OnboardingPaywallVariant.headlineRes: Int
    get() = when (this) {
        OnboardingPaywallVariant.PAYDAY_PROOF -> R.string.paywall_headline_default
        OnboardingPaywallVariant.REVIEW_MOMENTUM -> R.string.paywall_headline_review_momentum
    }

private val OnboardingPaywallVariant.subheadRes: Int
    get() = when (this) {
        OnboardingPaywallVariant.PAYDAY_PROOF -> R.string.paywall_subhead_default
        OnboardingPaywallVariant.REVIEW_MOMENTUM -> R.string.paywall_subhead_review_momentum
    }
