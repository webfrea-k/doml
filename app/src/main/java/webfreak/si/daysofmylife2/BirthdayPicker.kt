package webfreak.si.daysofmylife2

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast

class BirthdayPicker : DialogFragment(), DatePickerDialog.OnDateSetListener  {

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {



        //dialogDatePicker.datePicker.spinnersShown = true
        //dialogDatePicker.datePicker.calendarViewShown = false
        return null
    }}