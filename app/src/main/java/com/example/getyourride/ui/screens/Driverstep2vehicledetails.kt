package com.example.getyourride.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.components.GyrTextField
import com.example.getyourride.ui.theme.*

data class VehicleDetailsState(
    val regNumber    : String = "",
    val makeAndModel : String = "",
    val colour       : String = "",
    val seatCapacity : Int    = 4,
)

@Composable
fun DriverStep2VehicleDetails(
    state          : VehicleDetailsState,
    onRegChange    : (String) -> Unit,
    onMakeChange   : (String) -> Unit,
    onColourChange : (String) -> Unit,
    onSeatInc      : () -> Unit,
    onSeatDec      : () -> Unit,
) {
    Column(
        modifier            = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DriverStepHeading(
            title    = "Vehicle Details",
            subtitle = "Provide information about the vehicle you'll be using for rides.",
        )

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GyrTextField(
                    label         = "Registration Number",
                    value         = state.regNumber,
                    onValueChange = onRegChange,
                    placeholder   = "e.g. ABC 123 EC",
                    leadingIcon   = Icons.Outlined.Pin,
                )

                GyrTextField(
                    label         = "Make & Model",
                    value         = state.makeAndModel,
                    onValueChange = onMakeChange,
                    placeholder   = "e.g. VW Polo Vivo",
                    leadingIcon   = Icons.Outlined.DirectionsCar,
                )

                GyrTextField(
                    label         = "Vehicle Colour",
                    value         = state.colour,
                    onValueChange = onColourChange,
                    placeholder   = "e.g. White",
                    leadingIcon   = Icons.Outlined.Palette,
                )

                // Seat Capacity Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text          = "PASSENGER CAPACITY",
                        fontSize      = 11.sp,
                        fontWeight    = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color         = NavyPrimary,
                    )
                    
                    Row(
                        modifier           = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IconButton(
                                onClick = onSeatDec,
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Outlined.Remove, contentDescription = "Decrease", tint = NavyPrimary)
                            }
                            
                            Text(
                                text       = state.seatCapacity.toString(),
                                fontSize   = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color      = NavyPrimary
                            )
                            
                            IconButton(
                                onClick = onSeatInc,
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "Increase", tint = NavyPrimary)
                            }
                        }
                        
                        Text(
                            text     = "Max 8 seats",
                            fontSize = 12.sp,
                            color    = TextMuted
                        )
                    }
                }
            }
        }

        DriverInfoNotice(
            text = "Ensure your vehicle matches the registration documents you'll upload in the next step.",
            icon = Icons.Outlined.Info,
            tint = OrangeAccent,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Step 2 — Vehicle Details")
@Composable
fun DriverStep2Preview() {
    MaterialTheme {
        DriverStep2VehicleDetails(
            state          = VehicleDetailsState(),
            onRegChange    = {},
            onMakeChange   = {},
            onColourChange = {},
            onSeatInc      = {},
            onSeatDec      = {},
        )
    }
}
