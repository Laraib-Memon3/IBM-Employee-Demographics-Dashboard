package com.example.lab05

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
//            MadLibApp(this)
//            AppNav()
            Task4()
        }
    }
}

// Task 01

@Composable
fun MadLibApp(context: Context) {

    var screen by rememberSaveable { mutableStateOf("welcome") }

    var story by remember { mutableStateOf("") }
    var rawPlaceholders by remember { mutableStateOf(listOf<String>()) }
    var displayPlaceholders by remember { mutableStateOf(listOf<String>()) }
    var currentIndex by rememberSaveable { mutableStateOf(0) }
    var userInputs by remember { mutableStateOf(mutableListOf<String>()) }
    var finalStory by remember { mutableStateOf("") }

    when (screen) {

        // Welcome Screen
        "welcome" -> WelcomeScreen(
            onStart = {

                val newStory = loadRandomStory(context)

                val raw = extractRawPlaceholders(newStory)
                val display = raw.map { normalizePlaceholder(it) }

                story = newStory
                rawPlaceholders = raw
                displayPlaceholders = display
                userInputs = MutableList(raw.size) { "" }
                currentIndex = 0

                screen = "input"
            }
        )

        // Input Screen
        "input" -> {
            if (displayPlaceholders.isNotEmpty() && currentIndex < displayPlaceholders.size) {

                InputScreen(
                    word = displayPlaceholders[currentIndex],
                    index = currentIndex,
                    total = rawPlaceholders.size,

                    onNext = { input ->

                        userInputs[currentIndex] = input

                        val nextIndex = currentIndex + 1
                        currentIndex = nextIndex

                        if (nextIndex >= rawPlaceholders.size) {

                            finalStory = generateStory(
                                story,
                                rawPlaceholders,
                                userInputs
                            )

                            screen = "result"
                        }
                    }
                )
            }
        }

        // Result Screen
        "result" -> ResultScreen(
            story = finalStory,
            onRestart = {
                screen = "welcome"
            }
        )
    }
}

fun loadRandomStory(context: Context): String {
    val files = context.assets.list("")!!.filter { it.endsWith(".txt") }
    val randomFile = files.random()

    return context.assets.open(randomFile)
        .bufferedReader()
        .use { it.readText() }
}

fun extractRawPlaceholders(text: String): List<String> {
    val regex = "<[^>]+>".toRegex()
    return regex.findAll(text).map { it.value }.toList()
}

fun normalizePlaceholder(input: String): String {
    return input.removeSurrounding("<", ">")
        .lowercase()
        .replace("-", " ")
}

fun generateStory(
    original: String,
    rawPlaceholders: List<String>,
    inputs: List<String>
): String {
    var result = original
    rawPlaceholders.forEachIndexed { index, placeholder ->
        result = result.replaceFirst(placeholder, inputs[index])
    }
    return result
}

@Composable
fun WelcomeScreen(onStart: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "MAD LIBS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Fill in the blanks to create a funny story!",
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GET STARTED")
        }
    }
}

@Composable
fun InputScreen(
    word: String,
    index: Int,
    total: Int,
    onNext: (String) -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }

    val remaining = total - index

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            Text(
                text = "Fill in the words to complete the story!",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("$remaining word(s) left")

            Spacer(modifier = Modifier.height(30.dp))

            Text(word, color = Color.Gray)

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Type a $word") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Please type a/an $word",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Button(
            onClick = {
                onNext(input)
                input = ""
            },
            enabled = input.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Great! Keep going!")
        }
    }
}

@Composable
fun ResultScreen(
    story: String,
    onRestart: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column {

            Text(
                text = "Your Mad Lib Story!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(story)
        }

        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("MAKE ANOTHER STORY")
        }
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dbHelper = remember { DBHelper(context) }
   
    NavHost(navController = navController, startDestination = "home") {
        composable(route = "home") { Task02(navController, dbHelper) }
        composable(route = "contactScreen") { ContactListScreen(dbHelper = dbHelper)}
    }
}

// Task 02
@Composable
fun Task02(navController: NavController, dbHelper: DBHelper) {
    val (name, setName) = rememberSaveable { mutableStateOf("") }
    val (phone, setPhone) = rememberSaveable { mutableStateOf("") }
    val (street, setStreet) = rememberSaveable { mutableStateOf("") }
    val (email, setEmail) = rememberSaveable { mutableStateOf("") }
    val (city, setCity) = rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Name", modifier = Modifier.width(120.dp))
                    TextField(
                        value = name,
                        onValueChange = { setName(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Phone", modifier = Modifier.width(120.dp))
                    TextField(
                        value = phone,
                        onValueChange = { setPhone(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Street", modifier = Modifier.width(120.dp))
                    TextField(
                        value = street,
                        onValueChange = { setStreet(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Email", modifier = Modifier.width(120.dp))
                    TextField(
                        value = email,
                        onValueChange = { setEmail(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("City/State/Zip", modifier = Modifier.width(120.dp))
                    TextField(
                        value = city,
                        onValueChange = { setCity(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Button(
                onClick = {
                    val success = dbHelper.insertContact(
                        name,
                        phone,
                        street,
                        email,
                        city
                    )

                    if(success) {
                        setName("")
                        setPhone("")
                        setStreet("")
                        setEmail("")
                        setCity("")

                        navController.navigate("contactScreen")
                    }

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SAVE CONTACT")
            }
        }
    }
}

data class Contact(
    val name: String,
    val phone: String,
    val street: String,
    val email: String,
    val city: String
)

@Composable
fun ContactListScreen(dbHelper: DBHelper) {

    val contacts = remember { mutableStateListOf<Contact>() }

    LaunchedEffect(Unit) {
        contacts.clear()
        contacts.addAll(dbHelper.getAllContacts())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Saved Contacts",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(contacts.size) { index ->

                val contact = contacts[index]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(text = contact.phone)
                        Text(text = contact.email)
                        Text(text = contact.street)
                        Text(text = contact.city)
                    }
                }
            }
        }
    }
}

@Composable
fun Task3() {
    val (input, setInput) = rememberSaveable {
        mutableStateOf("")
    }

    val todos = remember { mutableStateListOf<String>() }
    val context = LocalContext.current

    LaunchedEffect(true) {
        try {
            val file = context.openFileInput("todos.txt")
                .bufferedReader()
                .readLines()

            todos.clear()
            todos.addAll(file)
        } catch (e: Exception) {
           // file not found
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Simple TODO",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Input Row
        Row(modifier = Modifier.fillMaxWidth()) {

            TextField(
                value = input,
                onValueChange = { setInput(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter a new item") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                if (input.isNotBlank()) {
                    todos.add(input)

                    context.openFileOutput("todos.txt", Context.MODE_PRIVATE).use {
                        it.write(todos.joinToString("\n").toByteArray())
                    }

                    setInput("")
                }
            }) {
                Text("Add Item")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(todos.size) { index ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        text = todos[index],
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun Task4() {
    val (input, setInput) = rememberSaveable {
        mutableStateOf("")
    }

    val todos = remember { mutableStateListOf<String>() }
    val context = LocalContext.current
    val dbHelper = remember { DBHelper(context) }


    LaunchedEffect(true) {
        todos.clear()
        todos.addAll(dbHelper.getAllTodos())

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Simple TODO",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Input Row
        Row(modifier = Modifier.fillMaxWidth()) {

            TextField(
                value = input,
                onValueChange = { setInput(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter a new item") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                if (input.isNotBlank()) {

                    dbHelper.insertTodo(input)
                    todos.add(input)

                    setInput("")
                }
            }) {
                Text("Add Item")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(todos.size) { index ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        text = todos[index],
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}