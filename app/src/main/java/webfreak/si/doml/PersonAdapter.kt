package webfreak.si.doml

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class PersonAdapter(c: Context, persons: MutableList<String>, tFragment: Fragment) : BaseAdapter() {

    private var people = persons
    private val context = c
    private val targetFragment = tFragment
    override fun getItem(position: Int): Any {
        return  people[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return people.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getCustomView(position, convertView, parent)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.people_list_item, parent, false)
        }
        val buttonView = view?.findViewById<ImageButton>(R.id.button3)
        val textView = view?.findViewById<TextView>(R.id.textView14)
        if (position == 0 || position == count - 1) {
            buttonView?.visibility = View.GONE
        } else {
            buttonView?.visibility = View.VISIBLE
        }
        textView?.text = people[position]
        buttonView?.setOnClickListener {
            val i = Intent().putExtra("name", people[position])
            targetFragment.onActivityResult(888, Activity.RESULT_OK, i)
        }
        //textView?.setOnClickListener {
        //    val i = Intent().putExtra("name", people[position])
        //    targetFragment.onActivityResult(777, Activity.RESULT_OK, i)
        //}
        return view!!
    }
}
