package com.example.projectjarvis.presentation


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn


import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.projectjarvis.utils.SpeechRecognizerManager
import com.example.projectjarvis.viewmodel.JarvisViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun JarvisScreen(jarvisViewModel: JarvisViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var recognizedText by remember { mutableStateOf("") }
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    var isListening by remember { mutableStateOf(false) }

    val jarvisResponse by jarvisViewModel.jarvisResponse.collectAsState()

    val speechManager = remember {
        SpeechRecognizerManager(
            context = context,
            onResult = { text ->
                recognizedText = text
                coroutineScope.launch { jarvisViewModel.handleRecognizedText(text) }
            },
            onPartialResult = { text -> recognizedText = text },
            onListeningStateChanged = { listening -> isListening = listening }
        )
    }

    DisposableEffect(Unit) { onDispose { speechManager.destroy() } }

    // Globe pulse animation
    val pulseAnim by animateFloatAsState(
        targetValue = if (isListening || jarvisResponse.isNotEmpty()) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF1F6FC)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // --- Globe ---
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 80.dp) // bring it down toward center
            ) {
                JarvisGlobe(
                    isActive = isListening || jarvisResponse.isNotEmpty(),
                    modifier = Modifier.size(220.dp) // bigger globe
                )
            }

            // --- LazyColumn for chats ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(340.dp)) // leave space for globe at top center

                LazyColumn(
                    modifier = Modifier
                        .weight(1f) // take all space between globe and controls
                        .padding(bottom = 185.dp), // leave space for TextField and mic
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (recognizedText.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(Color(0xFFB6E1F5)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = recognizedText,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    if (jarvisResponse.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(Color(0xFFE3F2FD)),
                                modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = jarvisResponse,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // --- Controls at bottom ---
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 12.dp, top = 4.dp, bottom = 40.dp, end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Modern TextField + Send button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp), // slightly taller
                        placeholder = { Text("Type your query…") },
                        singleLine = true,
                        shape = RoundedCornerShape(30.dp), // fully rounded
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF5795F3),
                            unfocusedBorderColor = Color(0xFFA5C2EE),
                            cursorColor = Color(0xFF2525AF),
                            focusedContainerColor = Color(0xFFF6F8FF),
                            unfocusedContainerColor = Color(0xFFF7F9FD)
                        )
                    )

                    Button(
                        onClick = {
                            if (inputText.text.isNotBlank()) {
                                val text = inputText.text
                                recognizedText = text
                                coroutineScope.launch { jarvisViewModel.handleRecognizedText(text) }
                                inputText = TextFieldValue("")
                            }
                        },
                        modifier = Modifier
                            .height(55.dp)
                            .width(90.dp), // make it more balanced
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B3BDE)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Send",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Mic button (already animated)
                AnimatedMicButton(
                    isListening = isListening,
                    onClick = {
                        if (!isListening) speechManager.startListening()
                        else speechManager.stopListening()
                    }
                )
            }


        }
    }
}


