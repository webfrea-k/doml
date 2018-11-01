package webfreak.si.daysofmylife2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.fragment_outlived.view.*
import org.json.JSONArray
import org.json.JSONObject
import webfreak.si.daysofmylife2.objects.Celebrity

class FragmentOutlived : Fragment() {
    lateinit var mAdView : AdView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_outlived, container, false)

        val queue = Volley.newRequestQueue(context)

        val url = "https://webfreak.si/daysofmylifeoutlived.json"
        val list: ArrayList<Celebrity> = ArrayList()
        val adapter = CelebrityAdapter(context!!,list)
        rootView.outlived_list.adapter = adapter

        val stringReq = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    var strResp = response.toString()
                    val jsonObj = JSONObject(strResp)
                    val jsonArray: JSONArray = jsonObj.getJSONArray("celebrity")
                    for (i in 0 until jsonArray.length()) {
                        var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        list.add(Celebrity(jsonInner.getString("name"),jsonInner.getString("daysalive").toInt(),0))
                    }
                    adapter.notifyDataSetChanged()
                },
                Response.ErrorListener {
                    Toast.makeText(context, getString(R.string.data_retrieval_failed), Toast.LENGTH_SHORT).show()
                })
        queue.add(stringReq)

        mAdView = rootView.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        return rootView
    }
}