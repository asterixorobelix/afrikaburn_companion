package io.asterixorobelix.afrikaburn.ui.screens.map

import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.camp_pin_cancel
import afrikaburn.composeapp.generated.resources.camp_pin_delete
import afrikaburn.composeapp.generated.resources.camp_pin_delete_confirm
import afrikaburn.composeapp.generated.resources.camp_pin_delete_message
import afrikaburn.composeapp.generated.resources.camp_pin_delete_title
import afrikaburn.composeapp.generated.resources.camp_pin_move
import afrikaburn.composeapp.generated.resources.camp_pin_move_confirm
import afrikaburn.composeapp.generated.resources.camp_pin_move_message
import afrikaburn.composeapp.generated.resources.camp_pin_move_title
import afrikaburn.composeapp.generated.resources.camp_pin_options_title
import afrikaburn.composeapp.generated.resources.camp_pin_place_confirm
import afrikaburn.composeapp.generated.resources.camp_pin_place_message
import afrikaburn.composeapp.generated.resources.camp_pin_place_title
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.asterixorobelix.afrikaburn.Dimens
import org.jetbrains.compose.resources.stringResource

/**
 * Dialog to confirm placing a new camp pin.
 */
@Composable
fun CampPinPlaceDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.camp_pin_place_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.camp_pin_place_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(Res.string.camp_pin_place_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.camp_pin_cancel))
            }
        }
    )
}

/**
 * Dialog showing options for existing camp pin (move/delete).
 */
@Composable
fun CampPinOptionsDialog(
    onMoveRequest: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.camp_pin_options_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                OutlinedButton(
                    onClick = onMoveRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.camp_pin_move))
                }
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                OutlinedButton(
                    onClick = onDeleteRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(Res.string.camp_pin_delete))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.camp_pin_cancel))
            }
        }
    )
}

/**
 * Dialog to confirm moving camp pin to new location.
 */
@Composable
fun CampPinMoveDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.camp_pin_move_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.camp_pin_move_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(Res.string.camp_pin_move_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.camp_pin_cancel))
            }
        }
    )
}

/**
 * Dialog to confirm deleting camp pin.
 */
@Composable
fun CampPinDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(Res.string.camp_pin_delete_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.camp_pin_delete_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(Res.string.camp_pin_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.camp_pin_cancel))
            }
        }
    )
}
