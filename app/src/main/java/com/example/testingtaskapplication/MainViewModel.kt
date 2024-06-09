package com.example.testingtaskapplication

import androidx.lifecycle.ViewModel
import com.example.testingtaskapplication.database.MainDb
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val database: MainDb) : ViewModel() {
    val itemsList = database.getDao().getAllLogItems()
}
