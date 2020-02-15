package com.example.takenotes.view

import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.text.SimpleDateFormat
import java.util.*

private var dateFormat = SimpleDateFormat("yyy-MM-dd", Locale.getDefault())
fun riseFallSortBottomSheet(isRise: Boolean , bottomSheet : BottomSheetBehavior<ConstraintLayout>) {
    if (isRise) {
        if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
    } else {
        if (bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }
}

fun riseFallFilterBottomSheet(isRise: Boolean , bottomSheet : BottomSheetBehavior<ConstraintLayout>) {
    if (isRise) {
        if (bottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        }
    } else {
        if (bottomSheet.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }
}

//Below Methods is used to Return Start Date & End Date:-
fun selectedFilterDate(filterType : String) : Pair<String , String>{
    val startCalendar = Calendar.getInstance()
    val endCalendar = Calendar.getInstance()
    var startDate : String?=null
    var endDate : String?=null

    when (filterType){
        "0" ->{
            //Today Filter Dates:-
            startDate = dateFormat.format(startCalendar.time)
            endDate = dateFormat.format(startCalendar.time)
        }

        "1" ->{
            //Yesterday Filter Dates:-
            startCalendar.add(Calendar.DATE , -1)
            startDate = dateFormat.format(startCalendar.time)
            endDate = dateFormat.format(startCalendar.time)
        }

        "2" ->{
            //Weekly Filter Dates:-
            startCalendar.add(Calendar.DATE , -7)
            startDate = dateFormat.format(startCalendar.time)
            endDate = dateFormat.format(endCalendar.time)
        }

        "3" ->{
            //Monthly Filter Dates:-
            startCalendar.add(Calendar.DATE , -30)
            startDate = dateFormat.format(startCalendar.time)
            endDate = dateFormat.format(endCalendar.time)
        }
    }

    return Pair(startDate!! , endDate!!)
}