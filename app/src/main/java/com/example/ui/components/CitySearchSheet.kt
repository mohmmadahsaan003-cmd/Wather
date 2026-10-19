package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CityLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitySearchSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    savedCities: List<CityLocation>,
    searchResults: List<CityLocation>,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectCity: (CityLocation) -> Unit,
    onRemoveSavedCity: (CityLocation) -> Unit
) {
    if (!isOpen) return

    var query by remember { mutableStateOf("") }

    val popularCities = remember {
        listOf(
            CityLocation("Tokyo", "Japan", 35.6762, 139.6503, "Tokyo"),
            CityLocation("New York", "United States", 40.7128, -74.0060, "New York"),
            CityLocation("London", "United Kingdom", 51.5074, -0.1278, "England"),
            CityLocation("Reykjavik", "Iceland", 64.1466, -21.9426, "Capital Region"),
            CityLocation("Paris", "France", 48.8566, 2.3522, "Île-de-France"),
            CityLocation("Sydney", "Australia", -33.8688, 151.2093, "New South Wales"),
            CityLocation("Honolulu", "United States", 21.3069, -157.8583, "Hawaii"),
            CityLocation("Dubai", "United Arab Emirates", 25.2048, 55.2708, "Dubai")
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        contentColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.4f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select Location",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    onSearchQueryChange(it)
                },
                placeholder = {
                    Text("Search global cities (e.g. Zurich, Tokyo)...", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            query = ""
                            onSearchQueryChange("")
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.7f))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.06f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.06f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_search_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Popular Quick Chips
            Text(
                text = "POPULAR DESTINATIONS",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(popularCities) { city ->
                    Box(
                        modifier = Modifier
                            .glassPill(alpha = 0.2f, borderAlpha = 0.3f)
                            .clickable {
                                onSelectCity(city)
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = city.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search Results or Saved Cities
            if (query.isNotEmpty()) {
                Text(
                    text = if (isSearching) "SEARCHING..." else "SEARCH RESULTS",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (searchResults.isEmpty() && !isSearching) {
                    Text(
                        text = "No cities found matching \"$query\"",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(searchResults) { city ->
                            CityItemRow(
                                city = city,
                                onSelect = {
                                    onSelectCity(city)
                                    onDismiss()
                                },
                                onRemove = null
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "SAVED CITIES",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(savedCities) { city ->
                        CityItemRow(
                            city = city,
                            onSelect = {
                                onSelectCity(city)
                                onDismiss()
                            },
                            onRemove = { onRemoveSavedCity(city) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CityItemRow(
    city: CityLocation,
    onSelect: () -> Unit,
    onRemove: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationCity,
            contentDescription = null,
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = city.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            val sub = listOfNotNull(city.region.ifEmpty { null }, city.country.ifEmpty { null }).joinToString(", ")
            if (sub.isNotEmpty()) {
                Text(
                    text = sub,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
            }
        }
        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
