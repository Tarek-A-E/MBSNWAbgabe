package com.example.mbsnw_abgabe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mbsnw_abgabe.ui.theme.MBSNWAbgabeTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothPage() {

    var showDialog by remember { mutableStateOf(false) }

    // Mock Bluetooth devices
    val mockDevices = remember {
        listOf(
            "Xioami Mi Band 7",
            "Lisa's Airpods Pro",
            "Iphone 14 Pro",
            "92:34:43:5F:52:3G"
        )
    }

    // Bluetooth Page UI and
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Bluetooth") },
            navigationIcon = {
                IconButton(onClick = { /* TODO: Open drawer */ }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            }
        )
    }) { innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) { ConnectButton(text = "Connect yourself" , onClick = { showDialog = true } ) }
        }
    }

    // Bluetooth connection Pop-Up Mockup
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Available Devices", style = MaterialTheme.typography.titleLarge) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mockDevices) { device ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(device)
                                Button(
                                    onClick = {
                                        // Mock connection logic
                                        showDialog = false
                                    },
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Connect")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ConnectButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun BluetoothPreview() {
    MBSNWAbgabeTheme {
        BluetoothPage()
    }
}
