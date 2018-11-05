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








class FragmentSocial : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_social, container, false)
        rootView.shareImage
        Glide.with(this)
            .asBitmap()
            .load("https://picsum.photos/900/600?d=faaas")
            .apply(RequestOptions().transform(ShareImage(context!!)))
            .into(rootView.shareImage)

        rootView.button.setOnClickListener {
            val intent = Intent().apply {
                val bitmapPath = Images.Media.insertImage(activity?.contentResolver, (rootView.shareImage.drawable as BitmapDrawable).bitmap, "title", null)
                val bitmapUri = Uri.parse(bitmapPath)
                this.action = Intent.ACTION_SEND
                this.putExtra(Intent.EXTRA_STREAM, bitmapUri)
                this.type = "image/png"
            }
            startActivity(Intent.createChooser(intent, "Share with friends"))
        }
        return rootView
    }
}