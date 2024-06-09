package com.example.testingtaskapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.testingtaskapplication.database.LogItem
import com.example.testingtaskapplication.database.MainDb
import com.example.testingtaskapplication.ui.theme.TestingTaskApplicationTheme

class LogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: MainViewModel by viewModels { MainViewModel.factory}
        setContent {
            TestingTaskApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val itemList = viewModel.itemsList.collectAsState(initial = emptyList())
                    LazyColumn(
                        contentPadding = PaddingValues(all = 12.dp)
                    ) {
                        items(itemList.value){
                            CustomLogItem(logItem = LogItem(it.id, it.date, it.logText))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomLogItem(logItem: LogItem){
    Row(modifier= Modifier
        .background(Color.White)
        .fillMaxWidth()
        .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.SpaceAround
    ){
        Text(
            text = logItem.date,
            color = Color.Black
        )
        Text(
            text = " | ",
            color = Color.Black
        )
        Text(
            text = logItem.logText,
            color = Color.Black
        )
    }
}

@Preview
@Composable
fun ItemPreview() {
    CustomLogItem(LogItem(0, "09.06.2024 12:32:34", "Got dispatch message: Gesture was dispatched!"))
}

