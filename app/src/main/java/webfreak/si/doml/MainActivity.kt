package webfreak.si.doml

import android.support.design.widget.TabLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import webfreak.si.doml.objects.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var mInterstitialAd: InterstitialAd
    private var searchVisible: Boolean = false
    private var optionsMenu: Menu? = null
    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    private var notificationsOn: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = PreferenceHelper.defaultPrefs(applicationContext)

        val uuid = java.util.UUID.randomUUID().toString()
        if (prefs.getString(Const.GLOBAL_USER_ID, "empty") == "empty") {
            prefs.edit().putString(Const.GLOBAL_USER_ID, uuid).apply()
        }

        auth = FirebaseAuth.getInstance()
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                mDatabase = FirebaseDatabase.getInstance().reference
                if (task.isSuccessful && !prefs.getBoolean(Const.ADDED_TO_DB,false)) {
                    val map = HashMap<String, Long>()
                    map[Const.YOU] = 0
                    map[Const.ADD_NEW] = 0
                    val user = UserBirthday(map)
                    prefs.getString(Const.GLOBAL_USER_ID, "todo")?.let {
                        mDatabase.child("users").child(it).setValue(user)
                        notificationsOn = user.active
                        prefs.edit().putBoolean(Const.ADDED_TO_DB, true).apply()
                    }
                }
            }
        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        fab.setOnClickListener {
            EventBus.getDefault().post(ToggleSearchEvent(searchVisible))
            searchVisible = !searchVisible
        }
        MobileAds.initialize(this, getString(R.string.admob_app_unit_id))
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.admob_interstitial_ad_unit_id)
        mInterstitialAd.loadAd(AdRequest.Builder().build())


        fab.hide()
        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                if (position !=1) { fab.hide() }
                if (position == 1) {
                    fab.show()
                }
                if (position == 2) {
                    EventBus.getDefault().postSticky(ShowNormalAd(kotlin.random.Random.nextBoolean()))
                }
            }
        })
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowInterstitial) {
        if (mInterstitialAd.isLoaded && event.show) {
            mInterstitialAd.show()
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.")
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(event: ToggleNotifications) {
        notificationsOn = event.active
        optionsMenu?.getItem(0)?.setIcon(if (event.active) R.drawable.notifications_on else  R.drawable.notifications_off)
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        optionsMenu = menu
        val prefs = PreferenceHelper.defaultPrefs(applicationContext)
        val notificationsOn = prefs.getBoolean(Const.NOTIFICATION_ENABLED, true)
        menu.getItem(0).setIcon(if (notificationsOn) R.drawable.notifications_on else R.drawable.notifications_off)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
            item.setIcon(if (notificationsOn) R.drawable.notifications_off else R.drawable.notifications_on)
            notificationsOn = ! notificationsOn
            val prefs = PreferenceHelper.defaultPrefs(applicationContext)
            prefs.getString(Const.GLOBAL_USER_ID, "todo")?.let {
                mDatabase.child("users").child(prefs.getString(Const.GLOBAL_USER_ID,"todo")!!).child("active").setValue(notificationsOn)
            }
            Snackbar.make(window.decorView.main_content, java.lang.String.format(getString(R.string.notification_change), if(notificationsOn) "enabled" else "disabled"), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return FragmentMyData()
                1 -> return FragmentOutlived()
                2 -> return FragmentSocial()
            }
            return FragmentMyData()
        }

        override fun getCount(): Int {
            // Show 3 total pages.
            return 3
        }
    }
}
