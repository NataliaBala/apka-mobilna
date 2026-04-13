package pl.wsei.pam.lab06

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// --- MODELE DANYCH ---
enum class Priority {
    High, Medium, Low
}

data class TodoTask(
    val title: String,
    val deadline: LocalDate,
    val isDone: Boolean,
    val priority: Priority
)

fun todoTasks(): List<TodoTask> {
    return listOf(
        TodoTask("Programming", LocalDate.of(2024, 4, 18), false, Priority.Low),
        TodoTask("Teaching", LocalDate.of(2024, 5, 12), false, Priority.High),
        TodoTask("Learning", LocalDate.of(2024, 6, 28), true, Priority.Low),
        TodoTask("Cooking", LocalDate.of(2024, 8, 18), false, Priority.Medium),
    )
}

// --- MOTYW (Placeholder dla Lab06Theme) ---
@Composable
fun Lab06Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            secondaryContainer = Color(0xFFEADDFF)
        ),
        content = content
    )
}

class Lab06Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab06Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val tasks = remember { mutableStateListOf(*todoTasks().toTypedArray()) }
    
    NavHost(navController = navController, startDestination = "list") {
        composable("list") { ListScreen(navController = navController, tasks = tasks) }
        composable("form") { 
            FormScreen(
                navController = navController, 
                onAddTask = { newTask -> tasks.add(newTask) }
            ) 
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    navController: NavController,
    title: String,
    showBackIcon: Boolean,
    route: String,
    onActionClick: () -> Unit = {}
) {
    val context = LocalContext.current
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackIcon) {
                IconButton(onClick = { navController.navigate(route) }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            if (route == "list") { // Jesteśmy w Formularzu, pokazujemy Zapisz
                OutlinedButton(
                    onClick = onActionClick
                ) {
                    Text(text = "Zapisz", fontSize = 18.sp)
                }
            } else {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "")
                }
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "")
                }
            }
        }
    )
}

@Composable
fun ListScreen(navController: NavController, tasks: MutableList<TodoTask>) {
    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "List",
                showBackIcon = false,
                route = "form"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = { navController.navigate("form") }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add task",
                    modifier = Modifier.scale(1.5f)
                )
            }
        },
        content = { padding ->
            LazyColumn(modifier = Modifier.padding(padding)) {
                itemsIndexed(items = tasks) { index, item ->
                    ListItem(
                        item = item,
                        onCheckedChange = { isChecked ->
                            tasks[index] = item.copy(isDone = isChecked)
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun ListItem(item: TodoTask, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(120.dp) // Zgodnie z instrukcją: 120.dp
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = item.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = item.priority.name,
                    color = when(item.priority) {
                        Priority.High -> Color.Red
                        Priority.Medium -> Color.Blue
                        Priority.Low -> Color.Green
                    }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Deadline: ${item.deadline.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                Checkbox(checked = item.isDone, onCheckedChange = onCheckedChange)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(navController: NavController, onAddTask: (TodoTask) -> Unit) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.Low) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            AppTopBar(
                navController = navController,
                title = "Form",
                showBackIcon = true,
                route = "list",
                onActionClick = {
                    if (title.isNotBlank()) {
                        onAddTask(TodoTask(title, selectedDate, false, priority))
                        navController.navigate("list")
                    }
                }
            )
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tytuł zadania") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = "Priorytet:", style = MaterialTheme.typography.bodyLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Priority.values().forEach { p ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = (priority == p),
                                onClick = { priority = p }
                            )
                            Text(text = p.name)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Wybierz datę: ${selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val date = datePickerState.selectedDateMillis?.let {
                                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                                } ?: LocalDate.now()
                                selectedDate = date
                                showDatePicker = false
                            }) { Text("OK") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Lab06Theme {
        MainScreen()
    }
}
