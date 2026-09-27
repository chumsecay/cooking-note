package com.cookingnote.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cookingnote.app.data.remote.model.SyncStatus

/**
 * Modern Material 3 badge indicating the current data synchronization status.
 */
@Composable
fun SyncStatusBadge(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (label, icon, containerColor, contentColor) = when (status) {
        SyncStatus.Synced -> Tuple4(
            "Đã đồng bộ",
            Icons.Filled.CloudDone,
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32)
        )
        SyncStatus.Syncing -> Tuple4(
            "Đang đồng bộ...",
            Icons.Filled.Sync,
            Color(0xFFE3F2FD),
            Color(0xFF1565C0)
        )
        SyncStatus.Offline -> Tuple4(
            "Ngoại tuyến",
            Icons.Filled.CloudOff,
            Color(0xFFF5F5F5),
            Color(0xFF616161)
        )
        SyncStatus.Error -> Tuple4(
            "Lỗi đồng bộ",
            Icons.Filled.SyncProblem,
            Color(0xFFFFEBEE),
            Color(0xFFC62828)
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
