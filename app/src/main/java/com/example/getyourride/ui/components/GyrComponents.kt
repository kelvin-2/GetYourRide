package com.example.getyourride.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

/**
 * Shared components for the GetYourRide app.
 * Moved here to fix 'Unresolved reference' errors in screen files.
 */

@Composable
fun GyrTextField(
    label         : String,
    value         : String,
    onValueChange : (String) -> Unit,
    placeholder   : String,
    leadingIcon   : ImageVector,
    modifier      : Modifier = Modifier, // Fix: Moved to first optional parameter
    keyboardType  : KeyboardType = KeyboardType.Text,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier) {
        Text(
            text          = label.uppercase(),
            fontSize      = 11.sp,
            fontWeight    = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color         = NavyPrimary,
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = { Text(placeholder, color = TextHint, fontSize = 14.sp) },
            leadingIcon   = {
                Icon(leadingIcon, contentDescription = null, tint = IconTint, modifier = Modifier.size(20.dp))
            },
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(10.dp),
            singleLine      = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors          = gyrOutlinedTextFieldColors(),
        )
    }
}

@Composable
fun gyrOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = NavyPrimary,
    unfocusedBorderColor = BorderLight,
    cursorColor          = NavyPrimary,
    focusedTextColor     = TextPrimary,
    unfocusedTextColor   = TextPrimary,
    focusedContainerColor   = CardWhite,
    unfocusedContainerColor = SurfaceGrey,
)

@Composable
fun OrDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.fillMaxWidth(),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
        Text(
            text     = "OR",
            fontSize = 12.sp,
            color    = TextMuted,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = BorderLight)
    }
}

@Composable
fun NsfasRadioOption(
    label    : String,
    selected : Boolean,
    onSelect : () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 2.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick  = onSelect,
            colors   = RadioButtonDefaults.colors(selectedColor = NavyPrimary),
        )
        Text(
            text     = label,
            fontSize = 14.sp,
            color    = if (selected) NavyPrimary else TextMuted,
        )
    }
}
