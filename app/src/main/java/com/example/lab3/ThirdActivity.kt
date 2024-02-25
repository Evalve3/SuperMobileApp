package com.example.lab3

import android.content.ContentValues
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp

class ThirdActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApp {
                MyThirdScreenContent()
            }
        }
    }
}

fun loadNotes(dbManager: DbManager): List<String> {
    val projection = arrayOf("Title")
    val cursor = dbManager.Query(projection, "", arrayOf(), "")
    val notes = mutableListOf<String>()
    while (cursor.moveToNext()) {
        val note = cursor.getString(cursor.getColumnIndexOrThrow("Title"))
        notes.add(note)
    }
    cursor.close()
    return notes
}

@Composable
fun EditNoteScreen(dbManager: DbManager, noteTitle: String, onNoteEdited: () -> Unit) {
    var editedNoteText by remember { mutableStateOf(noteTitle) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = editedNoteText,
            onValueChange = { editedNoteText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Edit your note") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val values = ContentValues().apply {
                    put("Title", editedNoteText)
                }
                val updatedCount = dbManager.update(values, "Title = ?", arrayOf(noteTitle))
                if (updatedCount > 0) {
                    // Если обновление прошло успешно, вызываем колбэк для оповещения об этом
                    onNoteEdited()
                }
            }
        ) {
            Text("Save Changes")
        }
    }
}


@Composable
fun MyThirdScreenContent() { // Передача контекста в качестве аргумента
    var noteText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val dbManager = DbManager(context)
    var notes by remember { mutableStateOf(emptyList<String>()) }
    var selectedNote by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        notes = loadNotes(dbManager)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column {
            notes.forEach { note ->
                Text(text = note)
                Row {
                    Button(
                        onClick = {
                            // Удаление заметки по ее названию
                            val deletedCount = dbManager.delete("Title = ?", arrayOf(note))
                            if (deletedCount > 0) {
                                // Если удаление прошло успешно, обновляем список заметок
                                notes = loadNotes(dbManager)
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedNote = note
                        }
                    ) {
                        Text("Edit")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Enter your note") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val values = ContentValues().apply {
                    put("Title", noteText)
                }
                val newRowId = dbManager.insert(values)
                if (newRowId > 0) {
                    // Успешно сохранено
                    noteText = ""
                    notes = loadNotes(dbManager)
                } else {
                    Log.e("Db", "NOT SAVE")
                }
            }
        ) {
            Text("Save")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Text("Go to Main Screen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(context, SecondActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Text("Go to Second Screen")
        }

        if (selectedNote.isNotEmpty()) {
            EditNoteScreen(
                dbManager = dbManager,
                noteTitle = selectedNote,
                onNoteEdited = {
                    // Обновляем список заметок после редактирования
                    notes = loadNotes(dbManager)
                    // Сбрасываем выбранную заметку
                    selectedNote = ""
                }
            )
        }
    }
}

