package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.hypot

data class WorldEntity(val id: Int, val x: Float, val y: Float, val emoji: String, val size: Float, val text: String? = null, val speaker: String? = null)

@Composable
fun VillageMapScreen(onFinish: () -> Unit) {
    var sabinaX by remember { mutableStateOf(500f) }
    var sabinaY by remember { mutableStateOf(500f) }
    
    val speed = 30f

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
         focusRequester.requestFocus()
    }

    val entities = remember {
        val list = mutableListOf<WorldEntity>()
        var id = 0
        // Borders
        for (i in -1000..2000 step 150) {
            list.add(WorldEntity(id++, i.toFloat(), -1000f, "🌲", 80f))
            list.add(WorldEntity(id++, i.toFloat(), 2000f, "🌳", 80f))
            list.add(WorldEntity(id++, -1000f, i.toFloat(), "🌲", 80f))
            list.add(WorldEntity(id++, 2000f, i.toFloat(), "🌳", 80f))
        }
        
        // Lake and Ship
        for(i in 1300..1800 step 80) {
            for(j in 1300..1800 step 80) {
                 list.add(WorldEntity(id++, i.toFloat(), j.toFloat(), "🌊", 80f))
            }
        }
        list.add(WorldEntity(id++, 1500f, 1500f, "⛵", 120f, "Ahoy Birthday Girl! The seas sing your name!", "Captain Barnacles"))

        // Houses
        list.add(WorldEntity(id++, 200f, 200f, "🏠", 150f))
        list.add(WorldEntity(id++, 800f, 300f, "🏡", 150f))
        list.add(WorldEntity(id++, 400f, 700f, "🏚️", 150f))
        list.add(WorldEntity(id++, 900f, 800f, "🛖", 120f))
        list.add(WorldEntity(id++, 0f, 500f, "⛺", 120f))
        list.add(WorldEntity(id++, 1200f, 100f, "🏯", 200f))

        // Animals
        list.add(WorldEntity(id++, 150f, 600f, "🐄", 60f))
        list.add(WorldEntity(id++, 200f, 650f, "🐄", 60f))
        list.add(WorldEntity(id++, 700f, 750f, "🐑", 50f))
        list.add(WorldEntity(id++, 750f, 720f, "🐑", 50f))
        list.add(WorldEntity(id++, 800f, 760f, "🐑", 50f))
        list.add(WorldEntity(id++, 850f, 200f, "🐎", 70f))
        list.add(WorldEntity(id++, 100f, 800f, "🐓", 40f))

        // NPCs
        list.add(WorldEntity(id++, 300f, 350f, "👨", 80f, "Happy Birthday Sabina! It's a wonderful day for an adventure.", "Village Chief"))
        list.add(WorldEntity(id++, 350f, 380f, "👦", 60f, "Wow, you're the strongest adventurer ever! Teach me how to fight!", "Timmy"))
        list.add(WorldEntity(id++, 700f, 400f, "👩", 80f, "We baked a massive cake for you in the town square! Go check it out!", "Mrs. Baker"))
        list.add(WorldEntity(id++, 500f, 500f, "🎂", 150f, "What a majestic birthday cake! You leveled up!", "System"))
        list.add(WorldEntity(id++, 100f, 100f, "👼", 100f, "I am the Celestial Angel. Receive my holy blessing of Wisdom, Sabina!", "Celestial Angel"))
        list.add(WorldEntity(id++, 1200f, 800f, "🐉", 150f, "ROAAAR! Even ancient dragons celebrate your birthday! Take this strength!", "Ancient Dragon"))
        list.add(WorldEntity(id++, 500f, 1200f, "🦅", 120f, "Screech! The skies bless your agility!", "Storm Wyvern"))
        list.add(WorldEntity(id++, -200f, 200f, "🐱", 50f, "Meow... (Happy Purr-thday)", "Stray Cat"))
        
        list.add(WorldEntity(id++, 500f, 300f, "🎁", 80f, "You found a hidden birthday gift! +50 Primogems", "System"))
        list.add(WorldEntity(id++, 800f, -200f, "🎁", 80f, "Another gift! +1 Wish", "System"))

        list
    }

    var currentSpeaker by remember { mutableStateOf<String?>(null) }
    var currentMessage by remember { mutableStateOf<String?>(null) }
    
    // Proximity check
    LaunchedEffect(sabinaX, sabinaY) {
        val closeEntity = entities.find {
            it.text != null && hypot(sabinaX - it.x, sabinaY - it.y) < 150f
        }
        currentSpeaker = closeEntity?.speaker
        currentMessage = closeEntity?.text
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionUp, Key.W -> { sabinaY -= speed; true }
                        Key.DirectionDown, Key.S -> { sabinaY += speed; true }
                        Key.DirectionLeft, Key.A -> { sabinaX -= speed; true }
                        Key.DirectionRight, Key.D -> { sabinaX += speed; true }
                        else -> false
                    }
                } else false
            }
            .background(Color(0xFF7CBA6B)) // Grass color
    ) {
        // Render world with camera offset
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val cx = maxWidth.value / 2f
            val cy = maxHeight.value / 2f
            
            // Tile background pattern
            
            // Render entities
            entities.forEach { entity ->
                val screenX = cx + (entity.x - sabinaX)
                val screenY = cy + (entity.y - sabinaY)
                
                // Only render if within screen bounds (roughly)
                if (screenX > -200 && screenX < maxWidth.value + 200 &&
                    screenY > -200 && screenY < maxHeight.value + 200) {
                    Text(
                        text = entity.emoji,
                        fontSize = entity.size.sp,
                        modifier = Modifier.offset(x = (screenX - entity.size/2).dp, y = (screenY - entity.size/2).dp)
                    )
                }
            }
            
            // Render Sabina
            var isFlipped by remember { mutableStateOf(false) }
            var lastX by remember { mutableStateOf(sabinaX) }
            var lastY by remember { mutableStateOf(sabinaY) }
            var playerState by remember { mutableStateOf("IDLE") }

            LaunchedEffect(sabinaX, sabinaY, currentMessage) {
                if (currentMessage != null && currentSpeaker != "System") {
                    playerState = "INTERACTING"
                } else if (sabinaX != lastX || sabinaY != lastY) {
                    playerState = "WALKING"
                    if (sabinaX > lastX) isFlipped = false
                    else if (sabinaX < lastX) isFlipped = true
                    lastX = sabinaX
                    lastY = sabinaY
                    delay(100)
                    if (sabinaX == lastX && sabinaY == lastY) {
                        playerState = if (currentMessage != null && currentSpeaker != "System") "INTERACTING" else "IDLE"
                    }
                } else {
                    playerState = "IDLE"
                }
            }

            val transition = rememberInfiniteTransition(label = "sprite_frame")
            val frameFloat by transition.animateFloat(
                initialValue = 0f,
                targetValue = 4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "frame"
            )
            val frame = frameFloat.toInt()

            // Calculate discrete "frame-based" values
            val fScale = if (playerState == "IDLE") {
                if (frame >= 2) 1.05f else 1f
            } else 1f

            val fOffsetY = when (playerState) {
                "WALKING" -> if (frame % 2 == 0) -8f else 0f
                "INTERACTING" -> if (frame < 2) -15f else 0f
                else -> 0f
            }

            val fRotate = when (playerState) {
                "WALKING" -> if (frame == 0) 10f else if (frame == 2) -10f else 0f
                else -> 0f
            }
            
            Image(
                 painter = painterResource(id = R.drawable.img_sabina_hero),
                 contentDescription = "Sabina",
                 modifier = Modifier
                     .align(Alignment.Center)
                     .offset(y = fOffsetY.dp)
                     .size(64.dp)
                     .clip(CircleShape)
                     .border(3.dp, com.example.ui.theme.PinkHighlight, CircleShape)
                     .graphicsLayer {
                         scaleX = (if (isFlipped) -1f else 1f) * fScale
                         scaleY = fScale
                         rotationZ = fRotate
                     },
                 contentScale = ContentScale.Crop
            )

            // Show an action emoji above her head based on state
            AnimatedVisibility(
                visible = playerState == "INTERACTING" || playerState == "WALKING",
                modifier = Modifier.align(Alignment.Center).offset(y = (-50).dp)
            ) {
                Text(
                    text = if (playerState == "INTERACTING") "✨" else if (frame % 2 == 0) "💨" else "",
                    fontSize = 24.sp
                )
            }

            // UI Overlay
            // Joystick
            Box(modifier = Modifier.align(Alignment.BottomStart).padding(32.dp)) {
                Joystick { dx, dy ->
                    sabinaX += dx
                    sabinaY += dy
                }
            }

            // Close button -> onFinish
            Button(
                onClick = onFinish,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha=0.5f))
            ) {
                Text("Exit Village", color = Color.White)
            }

            // Dialog box for currentMessage
            if (currentMessage != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 120.dp, end = 16.dp) // Avoid joystick
                        .fillMaxWidth(0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(com.example.ui.theme.DialogBg)
                            .border(2.dp, com.example.ui.theme.PinkHighlight, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            if (currentSpeaker != null) {
                                Text(
                                    text = currentSpeaker!!,
                                    color = com.example.ui.theme.AmberBadgeBg,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            TypewriterText(
                                text = currentMessage!!, 
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Instruction
            Text(
                text = "Use Joystick or WASD/Arrows to move around. Find NPCs and gifts!",
                color = Color.White,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp).background(Color.Black.copy(alpha=0.4f), RoundedCornerShape(8.dp)).padding(8.dp)
            )
        }
    }
}

@Composable
fun Joystick(onMove: (Float, Float) -> Unit) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val maxRadius = 100f
    
    // Need a coroutine that fires off move events while offset > 0
    LaunchedEffect(offsetX, offsetY) {
        while(offsetX != 0f || offsetY != 0f) {
            onMove(offsetX / maxRadius * 25f, offsetY / maxRadius * 25f)
            delay(16)
        }
    }

    Box(
        modifier = Modifier
            .size(150.dp)
            .background(com.example.ui.theme.DialogBg.copy(alpha = 0.5f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { offsetX = 0f; offsetY = 0f },
                    onDragCancel = { offsetX = 0f; offsetY = 0f }
                ) { change, dragAmount ->
                    change.consume()
                    val newX = offsetX + dragAmount.x
                    val newY = offsetY + dragAmount.y
                    val dist = hypot(newX, newY)
                    if (dist <= maxRadius) {
                        offsetX = newX
                        offsetY = newY
                    } else {
                        offsetX = (newX / dist) * maxRadius
                        offsetY = (newY / dist) * maxRadius
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(offsetX.toInt(), offsetY.toInt()) }
                .size(60.dp)
                .background(com.example.ui.theme.PinkHighlight.copy(alpha = 0.8f), CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )
    }
}
