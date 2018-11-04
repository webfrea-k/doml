package webfreak.si.doml

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_celebrity_details.view.*
import org.json.JSONArray
import org.json.JSONObject
import webfreak.si.doml.objects.Celebrity
import java.lang.Exception


class FragmentCelebrityDetails : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_celebrity_details, container, false)
        arguments?.getString("fullname").let {
            rootView.textView11.text = it
        }
        arguments?.getString("avatar").let {
            Glide.with(this).load(it).into(rootView.avatar_imageview)
        }
        rootView.button2.setOnClickListener {
            this.dismiss()
        }

        val queue = Volley.newRequestQueue(context)
        val url = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&titles=" + rootView.textView11.text.toString().replace(" ","%20")

        val stringReq = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                try {
                    val strResp = response.toString()
                    val jsonObj = JSONObject(strResp)
                    val jsonItem = jsonObj.get("query") as JSONObject
                    val jsonPage = jsonItem.get("pages") as JSONObject
                    val pageId = jsonPage.get(jsonPage.keys().next()) as JSONObject
                    val excerpt = pageId.get("extract") as String
                    rootView.textView13.text = excerpt
                } catch (ex: Exception) {
                    Log.d("No Excerpt", ex.toString())
                }
            },
            Response.ErrorListener {
                rootView.textView13.text = getString(R.string.data_retrieval_failed)
            })

        queue.add(stringReq)
        return rootView
    }

    companion object {
        fun newInstance(name: String, value: Int, celebrity: Celebrity): FragmentCelebrityDetails {
            val args = Bundle()
            args.putString("name", name)
            args.putInt("value", value)
            args.putString("avatar",celebrity.getAvatar())
            args.putString("fullname",celebrity.getName())
            val fragment = FragmentCelebrityDetails()
            fragment.arguments = args
            return fragment
        }
    }
}