package com.example.draggablelazylist

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.draggablelazylist.ui.DragDropList
import com.example.draggablelazylist.ui.theme.DraggableLazyListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DraggableLazyListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val items = remember {
                        mutableStateListOf(ReorderItem(id = 1, title = "1 item"),
                            ReorderItem(id = 2, title = "2 item"),
                            ReorderItem(id = 3, title = "3 item"),
                            ReorderItem(id = 4, title = "4 item"),
                            ReorderItem(id = 5, title = "5 item"),
                            ReorderItem(id = 6, title = "6 item"),
                            ReorderItem(id = 7, title = "7 item"),
                            ReorderItem(id = 8, title = "8 item"),
                            ReorderItem(id = 9, title = "9 item"),
                            ReorderItem(id = 10, title = "10 item"),
                            ReorderItem(id = 11, title = "11 item"),
                            ReorderItem(id = 12, title = "12 item"),
                            ReorderItem(id = 13, title = "13 item"),
                            ReorderItem(id = 14, title = "14 item"),
                            ReorderItem(id = 15, title = "15 item"),
                            ReorderItem(id = 16, title = "16 item"),
                            ReorderItem(id = 17, title = "17 item"),
                            ReorderItem(id = 18, title = "18 item"),)
                    }

                    DragDropList(items = items.toList(), onMove = { i1, i2 ->
                        items.move(i1, i2)
                    })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DraggableLazyListTheme {

    }
}