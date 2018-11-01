package webfreak.si.daysofmylife2

import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_my_data.view.*


class FragmentMyData : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_my_data, container, false)
        val listOfItems = arrayOf("You", "+ Add person")
        val datePicker = rootView.btn_select_birthday
        datePicker.setOnClickListener { view ->
            val dpd = DatePickerDialog(activity, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val birthday = rootView.lbl_birthday
                birthday.text = "" + dayOfMonth + "." + (monthOfYear +1) + "."+ year
            }, 2000, 1, 1)

            dpd.show()
        }
        rootView.person_spinner.adapter = ArrayAdapter(activity, R.layout.spinner_item, listOfItems)
        return rootView

        //val prefs = defaultPrefs(this)
        //prefs[Consts.SharedPrefs.KEY] = "any_type_of_value" //setter
        //val value: String? = prefs[Consts.SharedPrefs.KEY] //getter
        //val anotherValue: Int? = prefs[Consts.SharedPrefs.KEY, 10] //getter with default value
    }
}