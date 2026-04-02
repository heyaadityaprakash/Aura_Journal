package com.aadi.aurajournal.feature

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.aadi.aurajournal.JournalViewModel
import com.aadi.aurajournal.data.JournalEntry
import com.aadi.aurajournal.data.MoodType
import com.aadi.aurajournal.ui.components.MoodPicker
import com.aadi.aurajournal.utils.copyUriToInternalStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ComposeEntryScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit,
    entryId: Int = -1
) {
    val allEntries by viewModel.allEntries.collectAsState()
    val entryToEdit = remember(entryId, allEntries) {
        allEntries.find { it.id == entryId }
    }

    val initialPage = 1
    val inputTabs = listOf("Audio", "Manual")
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { inputTabs.size })
    val coroutineScope = rememberCoroutineScope()

    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var selectedImgs by remember { mutableStateOf(entryToEdit?.images ?: emptyList()) }
    var clickedImg by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newInternalPaths = uris.mapNotNull { uri ->
                copyUriToInternalStorage(context, uri)
            }
            selectedImgs = selectedImgs + newInternalPaths
        }
    }

    val aiPromptText by viewModel.prompt.collectAsState()

    LaunchedEffect(Unit) {
        if (entryId == -1) {
            viewModel.generateNewPrompt()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }

                        DropdownMenu(
                            shape = RoundedCornerShape(24.dp),
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Image") },
                                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Location") },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Song") },
                                leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Video") },
                                leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InputModeToggle(
                tabs = inputTabs,
                selectedIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = aiPromptText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f) // Use weight to allow children to occupy remaining space
            ) { page ->
                when (page) {
                    0 -> AudioInputView(viewModel)
                    1 -> ManualInputView(
                        viewModel = viewModel,
                        onNavigateBack = onNavigateBack,
                        existingEntry = entryToEdit,
                        selectedImgs = selectedImgs,
                        onImagesChange = { selectedImgs = it },
                        clickedImg = clickedImg,
                        onImageClick = { clickedImg = it }
                    )
                }
            }
        }
    }
}

@Composable
fun InputModeToggle(tabs: List<String>, selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedIndex == index
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "toggle_color"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "toggle_text"
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(containerColor)
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun AudioInputView(viewModel: JournalViewModel) {
    val context = LocalContext.current

    var transcribedText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf<MoodType?>(null) }

    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { transcribedText = "" }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isRecording = false }
                override fun onError(error: Int) { isRecording = false }
                override fun onPartialResults(partialResults: Bundle?) {
                    val data = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    data?.let { transcribedText = it[0] }
                }
                override fun onResults(results: Bundle?) {
                    val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    data?.let { transcribedText = it[0] }
                    isRecording = false
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isRecording = true
                speechRecognizer.startListening(recognizerIntent)
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isRecording && transcribedText.isEmpty()) {
            Text(
                text = "Voice Entry",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))
            MockWaveform()
            Box(modifier = Modifier.weight(1f))
        } else {
            Spacer(modifier = Modifier.height(40.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .weight(1f),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Box(
                    modifier = Modifier.padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (transcribedText.isEmpty()) "Listening..." else transcribedText,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        if (!isRecording && transcribedText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))
                            MoodPicker(
                                selectedMood = selectedMood,
                                onMoodSelected = { selectedMood = it }
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (transcribedText.isNotEmpty() && !isRecording) {
                IconButton(onClick = {
                    transcribedText = ""
                    selectedMood = null
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }

            Surface(
                onClick = {
                    if (isRecording) {
                        speechRecognizer.stopListening()
                        isRecording = false
                    } else {
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            isRecording = true
                            speechRecognizer.startListening(recognizerIntent)
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                shape = CircleShape,
                color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
                tonalElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Check else Icons.Default.SettingsVoice,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            if (transcribedText.isNotEmpty() && !isRecording) {
                IconButton(onClick = {
                    viewModel.saveEntry(transcribedText, mood = selectedMood?.name)
                    transcribedText = ""
                    selectedMood = null
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ManualInputView(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit,
    existingEntry: JournalEntry?,
    selectedImgs: List<String>,
    onImagesChange: (List<String>) -> Unit,
    clickedImg: String?,
    onImageClick: (String?) -> Unit
) {
    var textState by remember { mutableStateOf(existingEntry?.content ?: "") }
    val dateString = remember { SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date()) }
    val timeString = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) }

    var selectedMood by remember {
        mutableStateOf(
            existingEntry?.mood?.let { moodName ->
                MoodType.entries.find { it.name == moodName }
            }
        )
    }
    val scrollState = rememberScrollState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
        ) {
            Text(
                text = dateString,
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = timeString,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            MoodPicker(selectedMood = selectedMood, onMoodSelected = { newMood -> selectedMood = newMood })

            Spacer(modifier = Modifier.height(24.dp))

            // Textfield now grows with content and won't have its own scrollbar
            TextField(
                value = textState,
                onValueChange = { textState = it },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text("Start typing your entry here...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )

            // Image grid follows naturally below the text
            ImageGrid(
                images = selectedImgs,
                onImgClick = { onImageClick(it) }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        // FAB for saving
        FloatingActionButton(
            onClick = {
                viewModel.saveEntry(textState, entryId = existingEntry?.id ?: 0, mood = selectedMood?.name, images = selectedImgs)
                onNavigateBack()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = "Save Entry")
        }

        // Full-screen viewer
        clickedImg?.let { path ->
            FullScreenImgViewer(
                imgPath = path,
                onDismiss = { onImageClick(null) },
                onDelete = {
                    onImagesChange(selectedImgs.filter { it != path })
                    onImageClick(null)
                }
            )
        }
    }
}

@Composable
fun MockWaveform() {
    val heights = listOf(12, 24, 16, 32, 48, 64, 48, 32, 24, 40, 56, 40, 20, 12)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.height(80.dp)
    ) {
        heights.forEach { height ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            )
        }
    }
}

@Composable
fun ImageGrid(images: List<String>, onImgClick: (String) -> Unit) {
    if (images.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        images.chunked(2).forEach { rowImages ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowImages.forEach { imgPath ->
                    AsyncImage(
                        model = imgPath,
                        contentDescription = "journal Img",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onImgClick(imgPath) }
                    )
                }
                if (rowImages.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FullScreenImgViewer(imgPath: String, onDismiss: () -> Unit, onDelete: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = imgPath,
                    contentDescription = "Full Screen Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Bar with Close and Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }
            }
        }
    }
}
