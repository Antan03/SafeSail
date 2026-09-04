package no.uio.ifi.in2000.sofiaalo.team44.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

enum class PickerDialog {
    NONE,
    DATE,
    TIME
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    initialTimestampMillis: Long = System.currentTimeMillis(),
    onTimestampSelected: (date: Long?, time: TimePickerState) -> Unit
){
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialTimestampMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )
    val initialHour = java.util.Calendar.getInstance().also { it.timeInMillis = initialTimestampMillis }.get(java.util.Calendar.HOUR_OF_DAY)
    val initialMinute = java.util.Calendar.getInstance().also { it.timeInMillis = initialTimestampMillis }.get(java.util.Calendar.MINUTE)
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    val pickerDialogSaver = Saver<PickerDialog, Int>(
        save = { it.ordinal },
        restore = { PickerDialog.entries[it] }
    )
    var currentDialog by rememberSaveable(stateSaver = pickerDialogSaver) {
        mutableStateOf(PickerDialog.NONE)
    }

    fun closeDialog() {
        currentDialog = PickerDialog.NONE
    }

    Button(
        onClick = {
            currentDialog = PickerDialog.DATE
        }
    ) {
        Text("Velg dato")
    }

    if (currentDialog == PickerDialog.DATE) {

        DatePickerDialog(
            onDismissRequest = {
                closeDialog()
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        currentDialog = PickerDialog.TIME
                    }
                ) {
                    Text("Ok")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        closeDialog()
                    }
                ) {
                    Text("Avbryt")
                }
            }
        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }

    if (currentDialog == PickerDialog.TIME) {

        TimePickerDialog(

            onDismissRequest = {
                closeDialog()
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        onTimestampSelected(
                            datePickerState.selectedDateMillis
                                ?: System.currentTimeMillis(),
                            timePickerState
                        )

                        closeDialog()
                    }
                ) {
                    Text("Ok")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        closeDialog()
                    }
                ) {
                    Text("Avbryt")
                }
            },

            title = {
                Text("Velg tidspunkt")
            }

        ) {

            TimePicker(
                state = timePickerState
            )
        }
    }
}