package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay

// ===== BIRTHDAY CONFIGURATION =====
// Edit this object to change the birthday details easily later!
object BirthdayConfig {
    val name = "Sabina"
    val date = "10 Oct"
    val age = 25 // Set to her actual age, this drives the LVL display
    val title = "Level Up! 🌟"
    val personalNote = """
        Happy Birthday, our favorite adventurer!
        May your HP always be full, your drop rates be legendary, and your quest log be filled with exciting new adventures this year.
    """.trimIndent()

    val messages = listOf(
        Pair("Party Member A", "Happy Birthday! Tanking another year like a pro!"),
        Pair("Healer Friend", "Sending you a +100 to Happiness buff today!"),
        Pair("Mage Pal", "Hope your birthday is absolutely magical!")
    )
}

data class RpgQuest(
    val id: Int,
    val title: String,
    val description: String,
    val rewards: String,
    val penalty: String,
    val icon: String
)

val defaultQuests = listOf(
    RpgQuest(1, "Cake Time!", "Eat a slice of birthday cake without dropping it. (Or just pretend you did!)", "Rewards: +50 PRIMOGEMS, +10 Happiness", "Penalty: -5 HP, Sadness", "🧁"),
    RpgQuest(2, "The Great Wish", "Make a wish and blow out all the candles in one breath.", "Rewards: Wish Granted, SSR Character", "Penalty: -1 Wish", "✨"),
    RpgQuest(3, "Open Presents", "Unwrap the mysterious gifts waiting for you.", "Rewards: Epic Loot", "Penalty: None (It's your birthday!)", "🎁")
)

enum class QuestStatus { NOT_STARTED, COMPLETED, FAILED }

data class StoryNode(val speaker: String, val text: String, val isNarrator: Boolean = false)

val adventureStory = listOf(
    StoryNode("System", "Sabina equips her beginner gear and steps into the bright, bustling streets of her hometown.", true),
    StoryNode("Mom & Dad", "Happy Birthday, our beautiful adventurer Sabina! We've prepared your favorite gear and the ultimate birthday cake!"),
    StoryNode("Best Friend", "Sabina!! Happy Level Up! Let's go explore the mythical realm and then come back for a massive party!"),
    StoryNode("Sabina", "Thank you guys! I'm ready for today's grand quest."),
    StoryNode("System", "She ventures beyond the village gates, entering the mythical Dragon's Peak where legends reside.", true),
    StoryNode("Ancient Flame Dragon", "I am the ancient guardian... and even I know it is your birthday. May your fire burn eternally! Receive my blessing!"),
    StoryNode("System", "The colossal Dragon breathes a majestic plume of warm, colorful fire. (+50 Strength & Magic)", true),
    StoryNode("Storm Wyvern", "*SCREEECH!* The winds whisper of your special day, Sabina. May you always fly high!"),
    StoryNode("System", "A gentle, swirling gust of wind embraces Sabina. (+20 Agility)", true),
    StoryNode("Celestial Angel", "Blessed child, Sabina. The heavens themselves celebrate you today. May your path always be illuminated with grace and love."),
    StoryNode("System", "A halo of golden light descends, showering her with warmth. (+100 Holy Charm & Wisdom)", true),
    StoryNode("Sabina", "Wow... I feel so powerful today! Thank you magical friends!"),
    StoryNode("System", "With new mythical blessings, Sabina returns to the village as evening falls.", true),
    StoryNode("Everyone", "SURPRISE!! HAPPY BIRTHDAY SABINA!!!"),
    StoryNode("System", "The town square is decorated with magical glowing lanterns, fireworks in the sky, and a massive multi-tiered cake that looks like a treasure chest.", true),
    StoryNode("Sabina", "This is the best real-life quest ever... Thank you, everyone!")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    RpgBirthdayScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun RpgBirthdayScreen(modifier: Modifier = Modifier) {
    var startQuest by remember { mutableStateOf(false) }
    var inAdventure by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    
    // Quest states
    val questStates = remember { mutableStateMapOf<Int, QuestStatus>() }
    var showQuestDialog by remember { mutableStateOf<RpgQuest?>(null) }
    var lastOutcome by remember { mutableStateOf<QuestStatus?>(null) }
    var showCelebration by remember { mutableStateOf(false) }

    LaunchedEffect(startQuest) {
        if (startQuest) {
            delay(300)
            showContent = true
        }
    }

    LaunchedEffect(lastOutcome, showQuestDialog) { // Need to re-trigger if another quest is completed
        if (lastOutcome == QuestStatus.COMPLETED && showQuestDialog != null) {
            showCelebration = true
            delay(4000)
            showCelebration = false
        }
    }

    val bgBrush = Brush.linearGradient(
        colors = listOf(com.example.ui.theme.GradientStart, com.example.ui.theme.GradientEnd)
    )
    Box(modifier = modifier.fillMaxSize().background(bgBrush)) {
        if (!startQuest) {
            // Start Screen
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "A New Quest Awaits...",
                    style = MaterialTheme.typography.titleLarge,
                    color = com.example.ui.theme.PinkTextTitle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { startQuest = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Press Start ✨",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        } else if (inAdventure) {
            VillageMapScreen(onFinish = { inAdventure = false })
        } else {
            // Main Content
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        // RPG Status Header (Frosted Glass Pattern)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(com.example.ui.theme.GlassSurface)
                                .border(1.dp, com.example.ui.theme.GlassBorder, RoundedCornerShape(24.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(com.example.ui.theme.PinkHighlight)
                                    .border(2.dp, Color.White, RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👑", fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = BirthdayConfig.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = com.example.ui.theme.TextMain
                                    )
                                    Text(
                                        text = "LVL ${BirthdayConfig.age}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = com.example.ui.theme.PinkTextTitle
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // HP Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(com.example.ui.theme.PinkHighlight.copy(alpha = 0.3f))
                                        .border(1.dp, com.example.ui.theme.PinkHighlight.copy(alpha = 0.5f), RoundedCornerShape(50))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth() // 100%
                                            .fillMaxHeight()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(com.example.ui.theme.PinkHighlight, com.example.ui.theme.ActionBg)
                                                )
                                            )
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("HAPPINESS PTS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = com.example.ui.theme.TextMuted)
                                    Text("MAX / MAX", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = com.example.ui.theme.TextMuted)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        // Glass box for Main Banner
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(com.example.ui.theme.GlassSurface)
                                .border(1.dp, com.example.ui.theme.GlassBorder, RoundedCornerShape(24.dp))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Amber Badge
                            Text(
                                text = "QUEST COMPLETE!",
                                style = MaterialTheme.typography.labelSmall,
                                color = com.example.ui.theme.AmberBadgeText,
                                modifier = Modifier
                                    .background(com.example.ui.theme.AmberBadgeBg, RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = BirthdayConfig.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = com.example.ui.theme.PinkTextTitle,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Happy Birthday, ${BirthdayConfig.name}! (${BirthdayConfig.date})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = com.example.ui.theme.TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        // Hero Image in RPG frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(24.dp))
                                .border(1.dp, com.example.ui.theme.GlassBorder, RoundedCornerShape(24.dp))
                                .background(Color.Black)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_sabina_hero),
                                contentDescription = "Hero illustration",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        // Expanded Character Profile with Animated Stat Bars
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(com.example.ui.theme.DialogBg)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Character Profile",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            AnimatedStatBar(label = "Magic", endValue = 0.95f, color = Color(0xFF9333EA))
                            Spacer(modifier = Modifier.height(12.dp))
                            AnimatedStatBar(label = "Charm", endValue = 1.0f, color = Color(0xFFF43F5E))
                            Spacer(modifier = Modifier.height(12.dp))
                            AnimatedStatBar(label = "Wisdom", endValue = 0.85f, color = Color(0xFF3B82F6))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    item {
                        // Adventure Mode Entry
                        Button(
                            onClick = { inAdventure = true },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.PinkHighlight),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("🌲 Enter the Whispering Woods (Adventure)", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center)
                        }

                        // Quest Progress Tracker
                        val completedCount = questStates.values.count { it == QuestStatus.COMPLETED }
                        val totalQuests = defaultQuests.size
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Daily Commissions",
                                style = MaterialTheme.typography.titleMedium,
                                color = com.example.ui.theme.PinkTextTitle
                            )
                            Text(
                                text = "$completedCount/$totalQuests",
                                style = MaterialTheme.typography.titleMedium,
                                color = com.example.ui.theme.PinkTextTitle
                            )
                        }
                    }

                    // Loop through quests
                    items(defaultQuests) { quest ->
                        val status = questStates[quest.id] ?: QuestStatus.NOT_STARTED
                        QuestCard(
                            quest = quest, 
                            status = status, 
                            onAction = { newStatus -> 
                                questStates[quest.id] = newStatus
                                lastOutcome = newStatus
                                showQuestDialog = quest
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        // Personal Note Dialog Box
                        RpgDialogBox(title = "Quest Log") {
                            Text(
                                text = BirthdayConfig.personalNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Text(
                            text = "Adventure Dialogue Log",
                            style = MaterialTheme.typography.titleMedium,
                            color = com.example.ui.theme.PinkTextTitle
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val wishes = adventureStory.filter { !it.isNarrator && it.speaker != "Sabina" }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(350.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(com.example.ui.theme.DialogBg.copy(alpha = 0.4f))
                                .border(1.dp, com.example.ui.theme.GlassBorder, RoundedCornerShape(24.dp))
                                .padding(16.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(wishes) { node ->
                                    RpgDialogBox(title = node.speaker) {
                                        Text(
                                            text = node.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
        
        // Reward / Penalty Dialog Overlay
        if (showQuestDialog != null) {
            val q = showQuestDialog!!
            val isSuccess = lastOutcome == QuestStatus.COMPLETED
            AlertDialog(
                onDismissRequest = { showQuestDialog = null },
                containerColor = com.example.ui.theme.DialogBg,
                titleContentColor = if (isSuccess) com.example.ui.theme.AmberBadgeBg else com.example.ui.theme.PinkHighlight,
                title = {
                    Text(if (isSuccess) "Quest Complete!" else "Quest Failed...")
                },
                text = {
                    Text(
                        text = if (isSuccess) q.rewards else q.penalty,
                        color = Color.White
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showQuestDialog = null }) {
                        Text("OK", color = Color.White)
                    }
                }
            )
        }
        
        // Celebration overlay
        CelebrationOverlay(show = showCelebration)
    }
}

@Composable
fun CelebrationOverlay(show: Boolean) {
    if (!show) return
    val infiniteTransition = rememberInfiniteTransition(label = "confetti transition")
    val fall by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 2500f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "confetti fall"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        val emojis = listOf("✨", "🎉", "🎂", "💖", "🌸", "🎊", "🎁")
        for (i in 0..20) {
            val emoji = emojis[i % emojis.size]
            Text(
                text = emoji,
                fontSize = 48.sp,
                modifier = Modifier
                    .absoluteOffset(
                        x = ((i * 45 + (i * 17) % 50)).dp, 
                        y = ((fall + (i * 300)) % 2500 - 150).dp
                    )
            )
        }
    }
}



@Composable
fun TypewriterText(text: String, modifier: Modifier = Modifier, color: Color = Color.White, style: androidx.compose.ui.text.TextStyle) {
    var displayedText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        displayedText = ""
        for (i in text.indices) {
            displayedText += text[i]
            kotlinx.coroutines.delay(15) // Typing speed
        }
    }
    
    Text(
        text = displayedText,
        modifier = modifier,
        color = color,
        style = style
    )
}

@Composable
fun AnimatedStatBar(label: String, endValue: Float, color: Color) {
    var trigger by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (trigger) endValue else 0f,
        animationSpec = tween(durationMillis = 1500, delayMillis = 300),
        label = "statBar"
    )

    LaunchedEffect(Unit) {
        trigger = true
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            Text(text = "${(animatedProgress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = color)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

@Composable
fun QuestCard(quest: RpgQuest, status: QuestStatus, onAction: (QuestStatus) -> Unit) {
    val isDone = status != QuestStatus.NOT_STARTED
    val isCompleted = status == QuestStatus.COMPLETED
    
    val bgColor = if (isDone) com.example.ui.theme.GlassSurface.copy(alpha = 0.2f) else com.example.ui.theme.GlassSurface
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, com.example.ui.theme.GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = quest.icon, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = quest.title, 
                        style = MaterialTheme.typography.titleMedium, 
                        color = com.example.ui.theme.TextMain,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = quest.description, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = com.example.ui.theme.TextMuted,
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
            
            if (!isDone) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { onAction(QuestStatus.FAILED) }) {
                        Text("Fail", color = com.example.ui.theme.PinkTextTitle)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAction(QuestStatus.COMPLETED) },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.ActionBg)
                    ) {
                        Text("Complete", color = Color.White)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = if (isCompleted) "COMPLETED" else "FAILED", 
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) com.example.ui.theme.ActionBg else com.example.ui.theme.PinkTextTitle,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RpgDialogBox(title: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(com.example.ui.theme.DialogBg)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left Pink border
            Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(com.example.ui.theme.DialogBorder))
            // Content Area
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 12.dp, end = 16.dp)) {
                // Title pill
                Box(
                    modifier = Modifier
                        .background(com.example.ui.theme.DialogBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "From: ${title.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Content 
                content()
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Press to continue...",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

