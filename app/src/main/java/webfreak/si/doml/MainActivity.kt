package webfreak.si.doml

import android.content.Context
import android.support.design.widget.TabLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import webfreak.si.doml.objects.UserBirthday

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    /**
     * The [android.support.v4.view.PagerAdapter] that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * [android.support.v4.app.FragmentStatePagerAdapter].
     */
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = PreferenceHelper.defaultPrefs(this as Context)

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
                    val user = UserBirthday(map, prefs.getString(Const.GLOBAL_USER_ID, "todo")!!)
                    mDatabase.child("users").child(user.id).setValue(user)
                    prefs.edit().putBoolean(Const.ADDED_TO_DB, true).apply()
                }
            }
        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        container.adapter = mSectionsPagerAdapter

        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        MobileAds.initialize(this, "ca-app-pub-9883575696396484~4631354970")

        fab.hide()
        container.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                if (position !=0) { fab.hide() }
                if (position == 1) {
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_settings) {
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
