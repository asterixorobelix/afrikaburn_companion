package io.asterixorobelix.afrikaburn.ui.components

import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.external_link_dialog_cancel
import afrikaburn.composeapp.generated.resources.external_link_dialog_confirm
import afrikaburn.composeapp.generated.resources.external_link_dialog_message
import afrikaburn.composeapp.generated.resources.external_link_dialog_title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExternalLinkConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.external_link_dialog_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.external_link_dialog_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(Res.string.external_link_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.external_link_dialog_cancel))
            }
        }
    )
}
