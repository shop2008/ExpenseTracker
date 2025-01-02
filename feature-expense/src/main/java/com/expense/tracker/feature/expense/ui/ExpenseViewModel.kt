/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.expense.tracker.feature.expense.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.expense.tracker.core.data.ExpenseRepository
import com.expense.tracker.feature.expense.ui.ExpenseUiState.Error
import com.expense.tracker.feature.expense.ui.ExpenseUiState.Loading
import com.expense.tracker.feature.expense.ui.ExpenseUiState.Success
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val uiState: StateFlow<ExpenseUiState> = expenseRepository
        .expenses.map<List<String>, ExpenseUiState> { Success(data = it) }
        .catch { emit(Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)

    fun addExpense(name: String) {
        viewModelScope.launch {
            expenseRepository.add(name)
        }
    }
}

sealed interface ExpenseUiState {
    object Loading : ExpenseUiState
    data class Error(val throwable: Throwable) : ExpenseUiState
    data class Success(val data: List<String>) : ExpenseUiState
}
