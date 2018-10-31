package webfreak.si.daysofmylife2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.fragment_outlived.view.*

class FragmentOutlived : Fragment() {
    lateinit var mAdView : AdView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_outlived, container, false)
        val testList = arrayOf("hu", "tu", "su", "send", "pe", "t", "tu", "su", "send", "pe", "t", "tu", "su", "send", "pe", "t", "tu", "su", "send", "pe", "t", "tu", "su", "send", "pe", "t", "tu", "su", "send", "pe", "t")
        val adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, testList)
        rootView.outlived_list.adapter = adapter

        mAdView = rootView.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        return rootView
    }
}