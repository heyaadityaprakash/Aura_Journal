package com.aadi.aurajournal.feature

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aadi.aurajournal.JournalViewModel
import com.aadi.aurajournal.data.JournalEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.SettingsVoice
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.aadi.aurajournal.data.MoodType
import com.aadi.aurajournal.ui.components.MoodPicker

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ComposeEntryScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit,
    entryId:Int=-1
) {
//    Find the entry if we are editing
    val allEntries by viewModel.allEntries.collectAsState()
    val entryToEdit = remember(entryId, allEntries) {
        allEntries.find { it.id == entryId }
    }

// If editing, start on the Manual page (Index 1). Otherwise, Audio (Index 0).
    val initialPage=1

    val inputTabs = listOf("Audio", "Manual")
    val pagerState = rememberPagerState(initialPage = initialPage,pageCount = { inputTabs.size })
    val coroutineScope = rememberCoroutineScope()

    // State to control the visibility of the Dropdown Menu
    var menuExpanded by remember { mutableStateOf(false) }



    //for ai prompt
    //collecting prompt
    val aiPromptText by viewModel.prompt.collectAsState()

    //trigger only for new entries
    LaunchedEffect(Unit) {
        if(entryId==-1){
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
                    // Wrap the icon and menu in a Box so the dropdown anchors correctly to the icon
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }

                        // The Dropdown Menu for adding context
                        DropdownMenu(
                            shape = RoundedCornerShape(24.dp),
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add Image") },
                                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                                contentPadding = PaddingValues(
                                    horizontal = 20.dp,
                                    vertical = 12.dp
                                ),
                                onClick = {
                                    menuExpanded = false
                                    /* TODO: Launch Photo Picker */
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Location") },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                                contentPadding = PaddingValues(
                                    horizontal = 20.dp,
                                    vertical = 12.dp
                                ),
                                onClick = {
                                    menuExpanded = false
                                    /* TODO: Fetch GPS Context */
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Song") },
                                leadingIcon = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                                contentPadding = PaddingValues(
                                    horizontal = 20.dp,
                                    vertical = 12.dp
                                ),
                                onClick = {
                                    menuExpanded = false
                                    /* TODO: Fetch Now Playing context */
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Video") },
                                leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null) },
                                contentPadding = PaddingValues(
                                    horizontal = 20.dp,
                                    vertical = 12.dp
                                ),
                                onClick = {
                                    menuExpanded = false
                                    /* TODO: Launch Video Picker */
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

            // 1. The Audio / Manual Toggle Pill
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

            // 2. AI Prompt Bubble
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

            // 3. Swipeable Input Area
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> AudioInputView(viewModel)
                    1 -> ManualInputView(viewModel, onNavigateBack,entryToEdit)
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

// === Audio Mode View ===

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
        // --- UI Logic: Hide when recording ---
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
            // --- Live Transcription Window ---
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

        // --- Controls ---
        Row(
            modifier = Modifier.padding(vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cancel/Clear button if text exists
            if (transcribedText.isNotEmpty() && !isRecording) {
                IconButton(onClick = {
                    transcribedText = ""
                    selectedMood = null
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }

            // Mic Button
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

            // Save Button
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

// === Manual Mode View ===
@Composable
fun ManualInputView(viewModel: JournalViewModel, onNavigateBack: () -> Unit,existingEntry: JournalEntry?) {
    // Pre-fill the text state if we are editing!
    var textState by remember { mutableStateOf(existingEntry?.content ?: "") }

    val dateString = remember { SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date()) }
    val timeString = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) }


//    mood picker
    var selectedMood by remember {
        mutableStateOf(
            existingEntry?.mood?.let { moodName ->
                MoodType.entries.find { it.name == moodName }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

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

            MoodPicker(selectedMood = selectedMood, onMoodSelected = {newMood->selectedMood=newMood})

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = textState,
                onValueChange = { textState = it },
                modifier = Modifier
                    .weight(1f)
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

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Bottom Action Area (Only the Save FAB now, aligned to the end)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(
                onClick = {
                    // Pass the existing ID so Room knows to update, not duplicate!
                    viewModel.saveEntry(textState, entryId = existingEntry?.id?:0,mood=selectedMood?.name)
                    onNavigateBack()
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Entry")
            }
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