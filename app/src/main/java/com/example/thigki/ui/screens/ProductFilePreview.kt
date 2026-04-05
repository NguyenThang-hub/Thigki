package com.example.thigki.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ProductFilePreview(
    fileUrl: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    val handler = LocalUriHandler.current
    if (fileUrl.isBlank()) {
        Text("Chưa có file", style = MaterialTheme.typography.bodyMedium, modifier = modifier)
        return
    }

    var imageFailed by rememberSaveable(fileUrl) { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (!imageFailed) {
            AsyncImage(
                model = fileUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Fit,
                onError = { imageFailed = true },
            )
        }
        if (imageFailed) {
            Text("Tệp đính kèm", style = MaterialTheme.typography.titleSmall)
            Text(
                fileUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            TextButton(onClick = { handler.openUri(fileUrl) }) {
                Text("Mở / tải file")
            }
        }
    }
}
