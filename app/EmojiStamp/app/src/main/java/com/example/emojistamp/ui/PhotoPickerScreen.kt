package com.example.emojistamp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import kotlin.math.roundToInt

data class Stamp(val id: Long, val emoji: String, val offset: Offset)

@Composable
fun PhotoPickerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedEmoji by remember { mutableStateOf("😀") }
    var stamps by remember { mutableStateOf(listOf<Stamp>()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
                stamps = emptyList() // 写真が変わったらスタンプをクリア
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                selectedImageUri = tempPhotoUri
                stamps = emptyList() // 写真が変わったらスタンプをクリア
            }
        }
    )

    PhotoPickerContent(
        selectedImageUri = selectedImageUri,
        selectedEmoji = selectedEmoji,
        stamps = stamps,
        onEmojiSelect = { selectedEmoji = it },
        onAddStamp = { emoji, offset ->
            stamps = stamps + Stamp(id = System.nanoTime(), emoji = emoji, offset = offset)
        },
        onMoveStamp = { id, dragAmount ->
            stamps = stamps.map {
                if (it.id == id) it.copy(offset = it.offset + dragAmount) else it
            }
        },
        onRemoveStamp = { id ->
            stamps = stamps.filter { it.id != id }
        },
        onPickImageClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onTakePhotoClick = {
            val directory = File(context.cacheDir, "images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File.createTempFile("captured_photo_", ".jpg", directory)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        },
        modifier = modifier
    )
}

@Composable
fun PhotoPickerContent(
    selectedImageUri: Uri?,
    selectedEmoji: String,
    stamps: List<Stamp>,
    onEmojiSelect: (String) -> Unit,
    onAddStamp: (String, Offset) -> Unit,
    onMoveStamp: (Long, Offset) -> Unit,
    onRemoveStamp: (Long) -> Unit,
    onPickImageClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = listOf("😀", "🎉", "❤️", "⭐", "🐱", "🔥")
    var draggingStampId by remember { mutableStateOf<Long?>(null) }

    val currentStamps by rememberUpdatedState(stamps)
    val currentOnMoveStamp by rememberUpdatedState(onMoveStamp)
    val currentOnAddStamp by rememberUpdatedState(onAddStamp)
    val currentOnRemoveStamp by rememberUpdatedState(onRemoveStamp)
    val currentSelectedEmoji by rememberUpdatedState(selectedEmoji)

    val density = LocalDensity.current
    val deleteThresholdPx = with(density) { 28.dp.toPx() }
    val deleteThresholdSq = deleteThresholdPx * deleteThresholdPx

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    currentOnAddStamp(currentSelectedEmoji, offset)
                                },
                                onLongPress = { longPressOffset ->
                                    val closestStamp = currentStamps.minByOrNull { stamp ->
                                        val dx = stamp.offset.x - longPressOffset.x
                                        val dy = stamp.offset.y - longPressOffset.y
                                        dx * dx + dy * dy
                                    }
                                    closestStamp?.let { stamp ->
                                        val dx = stamp.offset.x - longPressOffset.x
                                        val dy = stamp.offset.y - longPressOffset.y
                                        if (dx * dx + dy * dy < deleteThresholdSq) {
                                            currentOnRemoveStamp(stamp.id)
                                        }
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { startOffset ->
                                    draggingStampId = currentStamps
                                        .minByOrNull { stamp ->
                                            val dx = stamp.offset.x - startOffset.x
                                            val dy = stamp.offset.y - startOffset.y
                                            dx * dx + dy * dy
                                        }
                                        ?.id
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    draggingStampId?.let { id ->
                                        currentOnMoveStamp(id, dragAmount)
                                    }
                                },
                                onDragEnd = { draggingStampId = null },
                                onDragCancel = { draggingStampId = null }
                            )
                        }
                ) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    stamps.forEach { stamp ->
                        Text(
                            text = stamp.emoji,
                            fontSize = 40.sp,
                            modifier = Modifier.layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                layout(placeable.width, placeable.height) {
                                    placeable.placeRelative(
                                        stamp.offset.x.roundToInt() - placeable.width / 2,
                                        stamp.offset.y.roundToInt() - placeable.height / 2
                                    )
                                }
                            }
                        )
                    }
                }
            } else {
                Text(text = "画像が選択されていません")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onPickImageClick) {
                Text(text = "画像を選択")
            }
            Button(onClick = onTakePhotoClick) {
                Text(text = "カメラで撮影")
            }
        }

        EmojiPalette(
            emojis = emojis,
            selectedEmoji = selectedEmoji,
            onEmojiSelect = onEmojiSelect
        )
    }
}

@Composable
fun EmojiPalette(
    emojis: List<String>,
    selectedEmoji: String,
    onEmojiSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(emojis) { emoji ->
            val isSelected = emoji == selectedEmoji
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 2.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onEmojiSelect(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 32.sp)
            }
        }
    }
}