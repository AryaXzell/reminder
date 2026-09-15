package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    onContinueClick: () -> Unit
) {
    Scaffold(
        containerColor = iOSWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Spacing & Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
            ) {
                Text(
                    text = "Welcome to\nReminders",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 44.sp,
                    letterSpacing = (-1.0).sp,
                    color = Color.Black
                )
            }

            // Features List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 40.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                OnboardingFeatureRow(
                    icon = { GreenIconCollage() },
                    title = "Quick Creation",
                    description = "Simply type in your list, ask Siri, or add a reminder from your Calendar app."
                )

                OnboardingFeatureRow(
                    icon = { OrangeCarrotIcon() },
                    title = "Grocery Shopping",
                    description = "Create a Grocery List that automatically sorts items you add by category."
                )

                OnboardingFeatureRow(
                    icon = { YellowPeopleIcon() },
                    title = "Easy Sharing",
                    description = "Collaborate on a list, and even assign individual tasks."
                )

                OnboardingFeatureRow(
                    icon = { BlueGridIcon() },
                    title = "Powerful Organization",
                    description = "Create new lists to match your needs, categorize reminders with tags, and manage reminders around your work flow with Smart Lists."
                )
            }

            // Continue Button
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iOSBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )
            }
        }
    }
}

@Composable
fun OnboardingFeatureRow(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .padding(top = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Column {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = (-0.4).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = iOSSilver,
                lineHeight = 19.sp,
                letterSpacing = (-0.2).sp
            )
        }
    }
}

// Custom Onboarding Icons to match Apple Reminders precisely
@Composable
fun GreenIconCollage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Filled.Schedule, contentDescription = null, tint = iOSGreen, modifier = Modifier.size(20.dp))
            Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = iOSGreen, modifier = Modifier.size(20.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Filled.Flag, contentDescription = null, tint = iOSGreen, modifier = Modifier.size(20.dp))
            Icon(Icons.Filled.Navigation, contentDescription = null, tint = iOSGreen, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun OrangeCarrotIcon() {
    // Standard icon representing a carrot or grocery items
    Icon(
        imageVector = Icons.Filled.ShoppingBag,
        contentDescription = null,
        tint = iOSOrange,
        modifier = Modifier.size(36.dp)
    )
}

@Composable
fun YellowPeopleIcon() {
    Icon(
        imageVector = Icons.Filled.People,
        contentDescription = null,
        tint = iOSYellow,
        modifier = Modifier.size(36.dp)
    )
}

@Composable
fun BlueGridIcon() {
    Column(
        modifier = Modifier.size(32.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.weight(1f).aspectRatio(1f).background(iOSBlue, RoundedCornerShape(2.dp)))
            Box(modifier = Modifier.weight(1f).aspectRatio(1f).background(iOSBlue, RoundedCornerShape(2.dp)))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.weight(1f).aspectRatio(1f).background(iOSBlue, RoundedCornerShape(2.dp)))
            Box(modifier = Modifier.weight(1f).aspectRatio(1f).background(iOSBlue, RoundedCornerShape(2.dp)))
        }
    }
}
