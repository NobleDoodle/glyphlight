package com.glyphlight.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glyphlight.TorchViewModel

/** Where the Support button sends people. */
private const val SUPPORT_URL = "https://buymeacoffee.com/nobledoodle"

@Composable
fun SettingsScreen(viewModel: TorchViewModel) {
    var showAbout by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 8.dp, end = 20.dp),
        ) {
            IconButton(onClick = viewModel::closeToHome) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Text(
                text = "Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }

        SectionHeader("Screen", viewModel.selectedColor)

        SettingSwitchRow(
            title = "Brightness bar",
            description = "You control the brightness directly from the app, otherwise the flashlight uses the system brightness level.",
            checked = viewModel.brightnessBarEnabled,
            accent = viewModel.selectedColor,
            onCheckedChange = viewModel::updateBrightnessBarEnabled,
        )
        SettingSwitchRow(
            title = "Full brightness control",
            description = "The brightness bar affects all screens in the app, otherwise it only affects the flashlight when it's on.",
            checked = viewModel.fullBrightnessControl,
            accent = viewModel.selectedColor,
            onCheckedChange = viewModel::updateFullBrightnessControl,
        )
        SettingSwitchRow(
            title = "Prevent main screen lock",
            description = null,
            checked = viewModel.preventMainScreenLock,
            accent = viewModel.selectedColor,
            onCheckedChange = viewModel::updatePreventMainScreenLock,
        )
        SettingSwitchRow(
            title = "Prevent color picker screen lock",
            description = null,
            checked = viewModel.preventColorPickerScreenLock,
            accent = viewModel.selectedColor,
            onCheckedChange = viewModel::updatePreventColorPickerScreenLock,
        )

        HorizontalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

        SectionHeader("Other options", viewModel.selectedColor)

        SettingLinkRow(title = "About") { showAbout = true }

        HorizontalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 12.dp))

        SectionHeader("Support", viewModel.selectedColor)

        Text(
            text = "Glyphlight is free and open source. If you find it useful, you can buy me a coffee.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
        )

        Button(
            onClick = {
                // A device with no browser would otherwise throw ActivityNotFoundException.
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(SUPPORT_URL))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = viewModel.selectedColor),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = "BUY ME A COFFEE",
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            title = { Text("Glyphlight") },
            text = { Text("A dot-matrix, OLED-friendly screen flashlight.\nVersion 1.0") },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("CLOSE") }
            },
        )
    }
}

@Composable
private fun SectionHeader(text: String, accent: Color) {
    Text(
        text = text,
        color = accent,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String?,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 17.sp)
            if (description != null) {
                Text(
                    description,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp, end = 12.dp),
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = accent,
                checkedTrackColor = accent.copy(alpha = 0.45f),
            ),
        )
    }
}

@Composable
private fun SettingLinkRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(title, color = Color.White, fontSize = 17.sp)
    }
}
