package webfreak.si.daysofmylife2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.outlived_list_item.view.*
import webfreak.si.daysofmylife2.objects.Celebrity

class CelebrityAdapter(private var c: Context, private var quotes: ArrayList<Celebrity>) : BaseAdapter() {

    override fun getCount(): Int   {  return quotes.size  }
    override fun getItem(i: Int): Any {  return quotes[i] }
    override fun getItemId(i: Int): Long { return i.toLong()}

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        if (view == null) {
            view = LayoutInflater.from(c).inflate(R.layout.outlived_list_item, viewGroup, false)
        }
        val quote = this.getItem(i) as Celebrity

        val img = view!!.avatar
        val nameTxt = view.title
        val propTxt = view.subtitle

        nameTxt.text = quote.getName()
        propTxt.text = quote.getDaysLived().toString()
        //img.setImageResource(quote.getAvatar())

        view.setOnClickListener { Toast.makeText(c, quote.getName(), Toast.LENGTH_SHORT).show() }

        return view
    }
}
