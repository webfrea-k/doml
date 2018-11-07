package webfreak.si.doml

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_social.view.*
import com.bumptech.glide.request.RequestOptions
import webfreak.si.doml.transformations.ShareImage
import android.graphics.drawable.BitmapDrawable
import android.graphics.Bitmap
import android.R.attr.bitmap
import android.net.Uri
import android.provider.MediaStore.Images
import android.R.attr.bitmap
import android.R.attr.bitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.support.v4.content.FileProvider
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
import kotlin.random.Random


class FragmentSocial : Fragment() {

    lateinit var mAdView : AdView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_social, container, false)
        val prefs = PreferenceHelper.defaultPrefs(activity)
        val daysAlive = prefs.getLong(Const.DAYS_ALIVE,0)
        val queue = Volley.newRequestQueue(context)
        val url = "https://webfreak.si/daysofmylifequotes.json"

        val stringReq = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                val strResp = response.toString()
                val jsonObj = JSONObject(strResp)
                val jsonArray: JSONArray = jsonObj.getJSONArray("quote")
                val randQuote = jsonArray.getJSONObject(Random.nextInt(0, jsonArray.length()-1))
                prefs.edit().putString(Const.DAILY_QUOTE, daysAlive.toString() + "|" + randQuote.get("name").toString()).apply()
                loadImage(rootView, daysAlive, randQuote.get("name").toString())

            },
            Response.ErrorListener {
                Toast.makeText(context, getString(R.string.data_retrieval_failed), Toast.LENGTH_SHORT).show()
            })

        prefs.getString(Const.DAILY_QUOTE,"")?.let {
            if (!it.startsWith(daysAlive.toString())) {
                queue.add(stringReq)
            } else {
                loadImage(rootView, daysAlive, it.split("|").last())
            }
        }
        rootView.button.setOnClickListener {
            try {
                context?.let { ctx ->
                    val cachePath = File(ctx.cacheDir, "images")
                    cachePath.mkdirs()
                    val stream = FileOutputStream(cachePath.toString() + "/image.png")
                    (rootView.shareImage.drawable as BitmapDrawable).bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()
                    val imagePath = File(ctx.cacheDir, "images")
                    val newFile = File(imagePath, "image.png")
                    val contentUri = FileProvider.getUriForFile(context!!, "webfreak.si.doml.fileprovider", newFile)

                    if (contentUri != null) {
                        val shareIntent = Intent()
                        shareIntent.action = Intent.ACTION_SEND
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        shareIntent.setDataAndType(contentUri, ctx.contentResolver.getType(contentUri))
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
                        startActivity(Intent.createChooser(shareIntent, "Choose an app"))
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, getString(R.string.data_retrieval_failed), Toast.LENGTH_SHORT).show()
            }
        }

        mAdView = rootView.adViewSocial
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        return rootView
    }
    fun loadImage(rootView: View, daysAlive: Long, quote: String){
        context?.let { c ->
            Glide.with(this)
                .asBitmap()
                .load("https://picsum.photos/900/600?day=$daysAlive")
                .apply(RequestOptions().transform(ShareImage(c, quote)))
                .into(rootView.shareImage)
        }
    }
}