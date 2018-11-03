package webfreak.si.doml

import android.app.PendingIntent.getActivity
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.outlived_list_item.view.*
import webfreak.si.doml.objects.Celebrity
import android.support.v4.app.FragmentActivity
import android.R.attr.fragment
import android.R.attr.key
import android.os.Bundle





class CelebrityAdapter(private var c: Context, private var celebrities: ArrayList<Celebrity>, days: Long) : BaseAdapter() {

    override fun getCount(): Int   {  return celebrities.size  }
    override fun getItem(i: Int): Any {  return celebrities[i] }
    override fun getItemId(i: Int): Long { return i.toLong()}
    var daysAlive = days

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        if (view == null) {
            view = LayoutInflater.from(c).inflate(R.layout.outlived_list_item, viewGroup, false)
        }
        val celebrity = this.getItem(i) as Celebrity

        val img = view!!.avatar
        val nameTxt = view.title
        val propTxt = view.subtitle
        nameTxt.text = celebrity.getName()

        val difference = daysAlive - celebrity.getDaysLived()
        if (difference > 0) {
            propTxt.text = java.lang.String.format(c.getString(R.string.outlived_by), difference)
            propTxt.setTextColor(ContextCompat.getColor(c, R.color.colorPrimaryDark))
        } else {
            propTxt.text = java.lang.String.format(c.getString(R.string.days_more), difference)
            propTxt.setTextColor(Color.RED)
        }

        celebrity.getAvatar().let { user ->
            if (user != null) {
                Glide.with(c).load(user).into(img)
            } else {
                img.setImageResource(R.drawable.avatar)
            }
        }
        view.setOnClickListener { it
            val activity = c as FragmentActivity
            val fm = activity.supportFragmentManager
            val pop = FragmentCelebrityDetails.newInstance("",0,celebrity)
            pop.setTargetFragment(fm.primaryNavigationFragment, 1000)
            pop.show(fm, "Celebrity details")
        }

        return view
    }
}
