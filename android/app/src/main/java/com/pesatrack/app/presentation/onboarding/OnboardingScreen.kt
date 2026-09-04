package com.pesatrack.app.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pesatrack.app.R
import com.pesatrack.app.core.OnboardingPreferences
import com.pesatrack.app.navigation.Screen
import com.pesatrack.app.ui.theme.OnboardingArtBackground
import com.pesatrack.app.ui.theme.OnboardingDotInactive
import com.pesatrack.app.ui.theme.Primary
import com.pesatrack.app.ui.theme.Surface
import com.pesatrack.app.ui.theme.TextPrimary
import com.pesatrack.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private data class OnboardingPage(
    @DrawableRes val illustration: Int,
    val title: String,
    val body: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        illustration = R.drawable.ic_onboarding_welcome,
        title = "Welcome to PesaTrack",
        body = "Take control of your money. Track, analyse and grow your finances with confidence."
    ),
    OnboardingPage(
        illustration = R.drawable.ic_onboarding_mpesa,
        title = "Import M-Pesa SMS",
        body = "Automatically import and categorise your M-Pesa transactions."
    ),
    OnboardingPage(
        illustration = R.drawable.ic_onboarding_budgets,
        title = "Set budgets",
        body = "Create budgets for categories and get alerts when you are nearing your limits."
    )
)

@Composable
fun OnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    fun completeOnboarding() {
        OnboardingPreferences.setSeenOnboarding(context)
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Skip",
            color = TextSecondary,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.End)
                .clickable(onClick = { completeOnboarding() })
                .padding(8.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(onboardingPages[page])
        }

        DotsIndicator(
            pageCount = onboardingPages.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )

        val isLastPage = pagerState.currentPage == onboardingPages.lastIndex
        Button(
            onClick = {
                if (isLastPage) {
                    completeOnboarding()
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Text(if (isLastPage) "Get started" else "Next")
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(OnboardingArtBackground),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = page.illustration),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
        }

        Text(
            text = page.title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
        )
    }
}

@Composable
private fun DotsIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val active = index == currentPage
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .width(if (active) 15.dp else 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (active) Primary else OnboardingDotInactive)
            )
        }
    }
}
