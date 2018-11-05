package webfreak.si.doml

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
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONObject
import webfreak.si.doml.objects.Celebrity
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.eventbus.Subscribe
import webfreak.si.doml.objects.NextToOutliveEvent
import webfreak.si.doml.objects.UpdateEvent


class FragmentOutlived : Fragment() {

    lateinit var mAdView : AdView
    lateinit var adapter: CelebrityAdapter
    private val list: ArrayList<Celebrity> = ArrayList()
    private var daysAlive = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_outlived, container, false)
        val prefs = PreferenceHelper.defaultPrefs(activity)
        val queue = Volley.newRequestQueue(context)
        daysAlive = prefs.getLong(Const.BIRTHDAY,0)
        val url = "https://webfreak.si/daysofmylifeoutlived.json"
        adapter = CelebrityAdapter(context!!, list, daysAlive)
        rootView.outlived_list.adapter = adapter

        val stringReq = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    val strResp = response.toString()
                    val jsonObj = JSONObject(strResp)
                    val jsonArray: JSONArray = jsonObj.getJSONArray("celebrity")

                    for (i in 0 until jsonArray.length()) {
                        val jsonInner: JSONObject = jsonArray.getJSONObject(i)
                        if (jsonInner.has("avatar")) {
                            list.add(Celebrity(jsonInner.getString("name"), jsonInner.getString("daysalive").toInt(),jsonInner.getString("avatar")))
                        } else {
                            list.add(Celebrity(jsonInner.getString("name"), jsonInner.getString("daysalive").toInt(),null))
                        }
                        /*
                        var stringJson = ""
                        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                        StrictMode.setThreadPolicy(policy)
                        Jsoup.connect("https://www.bing.com/images/search?q="+ jsonInner.getString("name").replace(" ","+") +"&FORM=HDRSC2").get().run {
                            val x =select("img[class=mimg]").first()
                            val xxy =x.attr("src")
                            val cccc = "{\"name\":\""+jsonInner.getString("name")+"\",\"daysalive\":\""+jsonInner.getString("daysalive")+"\","+"\"avatar\":\""+xxy+"\"  },"
                            stringJson += cccc
                            Log.d("IMG", cccc)
                        }*/
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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateEvent) {
        adapter.daysAlive = event.value
        adapter.notifyDataSetChanged()
        val celebrity = list.find { s -> s.getDaysLived() > event.value }
        celebrity?.let {
            EventBus.getDefault().post(NextToOutliveEvent(it.getName(), it.getDaysLived() - event.value, it.getAvatar()))
        }
    }
    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(activity)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}