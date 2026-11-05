package com.expend.tracker.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.expend.tracker.data.preferences.ThemeMode
import com.expend.tracker.ui.components.ConfirmDialog
import com.expend.tracker.ui.components.MonochromeOutlinedButton
import com.expend.tracker.ui.components.MonochromePrimaryButton
import com.expend.tracker.ui.theme.MaliFamily
import com.expend.tracker.viewmodel.ExpendViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: ExpendViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // SAF Export Launcher (Create Document)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri) { result ->
                if (result.isSuccess) {
                    Toast.makeText(context, "Backup exported successfully", Toast.LENGTH_SHORT).show()
                    statusMessage = "Backup successfully exported."
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Export failed"
                    Toast.makeText(context, "Export error: $err", Toast.LENGTH_LONG).show()
                    statusMessage = "Export error: $err"
                }
            }
        }
    }

    // SAF Import Launcher (Open Document)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirmDialog = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ─── CLEAN HEADER (No Back Icon in Header) ───────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        val currentThemeMode by viewModel.themeMode.collectAsState()

        // ─── 1. THEME SELECTION (Clean minimal layout, no boxed container) ───
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Theme",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Select your preferred visual style. Device follows your Android system appearance.",
                fontFamily = MaliFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            ThemeSelectorRow(
                selectedMode = currentThemeMode,
                onModeSelected = { viewModel.setThemeMode(it) }
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )

        // ─── 2. BACKUP & RESTORE (Clean minimal layout, no boxed container) ──
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Backup & Restore",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Export your entire financial ledger, wallets, and templates as an offline backup file. You can restore this file at any time on this or another device.",
                fontFamily = MaliFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Equal-width action buttons: Export Backup & Import Backup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonochromePrimaryButton(
                    text = "Export Backup",
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                        val fileName = "expend_backup_$timestamp.json"
                        exportLauncher.launch(fileName)
                    },
                    modifier = Modifier.weight(1f)
                )

                MonochromeOutlinedButton(
                    text = "Import Backup",
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (!statusMessage.isNullOrEmpty()) {
            Text(
                text = statusMessage!!,
                fontFamily = MaliFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )

        // ─── 3. APPLICATION INFORMATION (Clean minimal layout, no boxed container)
        val appVersion = remember {
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                pInfo.versionName ?: "1.4"
            } catch (e: Exception) {
                "1.4"
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Application Information",
                fontFamily = MaliFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))

            InfoRow(label = "Application Name", value = "Expend")
            InfoRow(label = "Application Version", value = appVersion)
            InfoRow(label = "Developer", value = "apandey")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ─── RESTORE CONFIRMATION DIALOG (Destructive Always AlertDialog) ─────────
    if (showRestoreConfirmDialog && pendingRestoreUri != null) {
        ConfirmDialog(
            title = "Restore Backup?",
            message = "Restoring will replace your current wallets, transactions, and templates with the data in the selected backup file. Make sure you have exported your current data if needed.\n\nDo you want to proceed with restore?",
            confirmButtonText = "Restore Data",
            dismissButtonText = "Cancel",
            onConfirm = {
                val uri = pendingRestoreUri!!
                showRestoreConfirmDialog = false
                pendingRestoreUri = null
                viewModel.restoreBackup(uri) { result ->
                    if (result.isSuccess) {
                        val msg = result.getOrNull() ?: "Restore successful"
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        statusMessage = msg
                    } else {
                        val err = result.exceptionOrNull()?.message ?: "Restore failed"
                        Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                        statusMessage = "Restore failed: $err"
                    }
                }
            },
            onDismiss = {
                showRestoreConfirmDialog = false
                pendingRestoreUri = null
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = MaliFamily,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontFamily = MaliFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Three horizontally aligned options: [ Device ]  [ Day ]  [ Night ]
 * Equal-width, monochrome selected state, Mali typography.
 */
@Composable
private fun ThemeSelectorRow(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(
        ThemeMode.DEVICE to "Device",
        ThemeMode.DAY to "Day",
        ThemeMode.NIGHT to "Night"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        modes.forEach { (mode, title) ->
            val isSelected = selectedMode == mode
            val shape = RoundedCornerShape(8.dp)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.surface
                        else androidx.compose.ui.graphics.Color.Transparent
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontFamily = MaliFamily,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
