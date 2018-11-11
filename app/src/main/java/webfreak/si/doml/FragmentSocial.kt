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
import kotlinx.android.synthetic.main.fragment_my_data.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.DateTime
import org.joda.time.Days
import org.json.JSONArray
import org.json.JSONObject
import webfreak.si.doml.objects.NextToOutliveEvent
import webfreak.si.doml.objects.ShowNormalAd
import kotlin.random.Random


class FragmentSocial : Fragment() {

    lateinit var mAdView : AdView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_social, container, false)
        val prefs = PreferenceHelper.defaultPrefs(context!!)
        val daysAlive = Static.getDaysAlive(context!!)
        val queue = Volley.newRequestQueue(context)
        val url = "https://us-central1-days-of-my-life-57a3c.cloudfunctions.net/getQuote?daysAlive=$daysAlive"

        val stringReq = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                val randQuoteString = response.toString()
                prefs.edit().putString(Const.DAILY_QUOTE, daysAlive.toString() + "|" + randQuoteString).apply()
                loadImage(rootView, daysAlive, randQuoteString)

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
        return rootView
    }
    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowNormalAd) {
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }
    private fun loadImage(rootView: View, daysAlive: Int, quote: String){
        context?.let { c ->
            Glide.with(this)
                .asBitmap()
                .load("https://picsum.photos/900/600/?random&day=$daysAlive")
                .apply(RequestOptions().transform(ShareImage(c, quote)))
                .into(rootView.shareImage)
        }
    }
}