package webfreak.si.daysofmylife2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_my_data.view.*

class FragmentMyData : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_my_data, container, false)
        val listOfItems = arrayOf("You", "+ Add person")
        rootView.person_spinner.adapter = ArrayAdapter(activity, R.layout.spinner_item, listOfItems)
        return rootView
    }
}