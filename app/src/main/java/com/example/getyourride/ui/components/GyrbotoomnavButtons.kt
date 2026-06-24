package com.example.getyourride.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.getyourride.ui.theme.*

/**
 * Bottom action bar used across all multi-step flows.
 *
 * - Step 1      : only shows Next (full width), no Back
 * - Steps 2..n-1: Back (outlined) + Next (filled) side by side
 * - Last step   : Back (outlined) + Submit (filled) side by side
 *
 * @param currentStep   1-based current step number
 * @param totalSteps    total number of steps in the flow
 * @param nextLabel     label for the next/submit button on the last step
 * @param onBack        called when Back is tapped
 * @param onNext        called when Next is tapped (steps before last)
 * @param onSubmit      called when Submit is tapped (last step)
 */
@Composable
fun GyrBottomNavButtons(
    currentStep : Int,
    totalSteps  : Int,
    onBack      : () -> Unit,
    onNext      : () -> Unit,
    onSubmit    : () -> Unit,
    nextLabel   : String = "Next Step →",
    submitLabel : String = "Submit Profile",
) {
    val isFirstStep = currentStep == 1
    val isLastStep  = currentStep == totalSteps

    Surface(
        tonalElevation = 4.dp,
        color          = CardWhite,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Back — hidden on step 1
            if (!isFirstStep) {
                OutlinedButton(
                    onClick  = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape  = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NavyPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
                ) {
                    Text("Back", fontWeight = FontWeight.SemiBold)
                }
            }

            // Next / Submit
            Button(
                onClick  = if (isLastStep) onSubmit else onNext,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            ) {
                Text(
                    text       = if (isLastStep) submitLabel else nextLabel,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                )
            }
        }
    }
}