package webfreak.si.doml

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageButton
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_add_person.*
import kotlinx.android.synthetic.main.fragment_my_data.*
import kotlinx.android.synthetic.main.fragment_my_data.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import webfreak.si.doml.objects.*
import java.lang.ArithmeticException

class FragmentMyData : Fragment() {
    private lateinit var mDatabase: DatabaseReference
    private var trackedUsers: MutableList<String> = arrayListOf()
    private var user: UserBirthday? = null
    private var prefs: SharedPreferences? = null
    private var globalUserId:String = ""
    private var setBirthdayText:String = ""
    private var handler = Handler()
    private var birthdaysComponents = arrayOf(1,0,2000)
    private lateinit var runnableCode: Runnable
    private lateinit var nextToOutliveView: View
    private lateinit var personAdapter: PersonAdapter
    private lateinit var birthday: EditText
    private lateinit var datePicker: ImageButton
    private val fragment = this
    private lateinit var dpd: DatePickerDialog
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        addDefaultUsers()
        prefs = PreferenceHelper.defaultPrefs(context!!)
        rootView = inflater.inflate(R.layout.fragment_my_data, container, false)
        birthday = rootView.edit_birthday
        datePicker = rootView.btn_select_birthday
        setBirthdayText = getString(R.string.set_your_birthday)
        personAdapter = PersonAdapter(context!!, trackedUsers, fragment)
        rootView.person_spinner.adapter = personAdapter

        nextToOutliveView = rootView.next_to_outlive_view
        globalUserId = prefs?.getString(Const.GLOBAL_USER_ID,"") ?: ""
        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("users").child(globalUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.getValue(UserBirthday::class.java).let { onlineUser ->
                    user = onlineUser
                    onlineUser?.active?.let {
                        EventBus.getDefault().post(ToggleNotifications(it))
                    }
                    onlineUser?.birthdays?.keys?.let { list ->
                        val birthdays = list.toMutableList()
                        trackedUsers.clear()
                        trackedUsers.add(Const.YOU)
                        for (birthday in birthdays) {
                            if (birthday != Const.ADD_NEW && birthday != Const.YOU) {
                                trackedUsers.add(birthday)
                            }
                        }
                        trackedUsers.add(Const.ADD_NEW)
                        personAdapter.notifyDataSetChanged()
                        val time = user?.birthdays?.get(person_spinner.selectedItem) ?: 0L
                        if(time > 0) {
                            birthday.setText(convertLongToTime(time))
                            val date = DateTime(time).withTimeAtStartOfDay()
                            updateBirthdayComponents(date.millis)
                            restartHandlerRuns(date)
                            EventBus.getDefault().post(UpdateEvent(0, getDaysToNow(date)))
                        } else if (person_spinner.selectedItem == Const.YOU) {
                            showDateDialog()
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        rootView.person_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val text = rootView.person_spinner.getItemAtPosition(position).toString()
                handlePersonSelection(text)
            }
        }

        datePicker.setOnClickListener {
            showDateDialog()
        }
        return rootView
    }

    private fun addDefaultUsers() {
        if (!trackedUsers.contains(Const.YOU)) {
            trackedUsers.add(Const.YOU)
        }
        if (!trackedUsers.contains(Const.ADD_NEW)) {
            trackedUsers.add(Const.ADD_NEW)
        }
    }
    fun handlePersonSelection(text: String) {
        birthday.clearFocus()
        if (text == Const.ADD_NEW) {
            val pop = FragmentAddPerson()
            val fm = activity?.supportFragmentManager
            pop.setTargetFragment(this@FragmentMyData, 999)
            pop.show(fm, "Add Person")
        } else {
            setBirthdayText = prepareDialogPickerMessage(text)
            user?.birthdays?.get(text)?.let {
                if(it == 0L) {
                    birthday.text.clear()
                    showDateDialog()
                } else {
                    birthday.setText(convertLongToTime(it))
                    val birthDate = DateTime(it).withTimeAtStartOfDay()
                    updateBirthdayComponents(birthDate.millis)
                    if(prefs?.getLong(Const.BIRTHDAY,0) != birthDate.millis && text == Const.YOU) {
                        prefs?.edit()?.putLong(Const.BIRTHDAY, birthDate.millis)?.apply()
                    }
                    EventBus.getDefault().postSticky(UpdateEvent(0, getDaysToNow(birthDate)))
                    val random = kotlin.random.Random.nextInt(0,10)
                    //display ad probability 3/7
                    EventBus.getDefault().postSticky(ShowInterstitial(random > 7 && !BuildConfig.DEBUG))
                    restartHandlerRuns(birthDate)
                }
            }
        }
    }
    private fun restartHandlerRuns(birthday: DateTime) {
        handler.removeCallbacksAndMessages(null)
        runnableCode = object: Runnable {
            override fun run() {
                updateCounters(birthday)
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnableCode, 0)
    }

    fun updateCounters(birthDate: DateTime) {
        val currentDate = DateTime().plusDays(1)
        val period = Period(birthDate, currentDate)
        try {
            digit_seconds.text = java.lang.String.format(getString(R.string.big_number_formatter), Seconds.secondsBetween(birthDate, currentDate).seconds)
        } catch (ex: ArithmeticException) {
            digit_seconds.text = getString(R.string.arithmetic_too_big)
        }

        digit_minutes.text = java.lang.String.format(getString(R.string.big_number_formatter), Minutes.minutesBetween(birthDate, currentDate).minutes)
        digit_hours.text = java.lang.String.format(getString(R.string.big_number_formatter), Hours.hoursBetween(birthDate, currentDate).hours)
        digit_days.text = java.lang.String.format(getString(R.string.big_number_formatter), Days.daysBetween(birthDate, currentDate).days)
        digit_months.text = java.lang.String.format(getString(R.string.big_number_formatter), Months.monthsBetween(birthDate, currentDate).months)
        digit_years.text = java.lang.String.format(getString(R.string.big_number_formatter), period.years)
    }

    private fun updateBirthdayComponents(dateLong: Long) {
        val date = DateTime(dateLong)
        birthdaysComponents = arrayOf(date.dayOfMonth, date.monthOfYear - 1, date.year)
    }
    private fun prepareDialogPickerMessage(name: String): String {
        if (name == Const.YOU) {
            return getString(R.string.set_your_birthday)
        }
        return getString(R.string.set_your_birthday).replace("your", "$name's")
    }
    private fun getDaysToNow(birthDate: DateTime): Long {
        val currentDate = DateTime().plusDays(1).withTimeAtStartOfDay()
        return Days.daysBetween(birthDate, currentDate).days.toLong()
    }
    private fun convertDateToLong(year: Int, month: Int, day: Int): Long {
        var date = DateTime(DateTimeZone.UTC).withTimeAtStartOfDay()
        date = date.withYear(year)
        date = date.withMonthOfYear(month)
        date = date.withDayOfMonth(day)
        return date.millis
    }

    fun convertLongToTime(time: Long): String {
        val date = DateTime(time)
        return date.toString("dd.MM.yyyy")
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.extras?.get("name").let {
            if (requestCode == 777) {
                handlePersonSelection(it.toString())
                person_spinner.setSelection(trackedUsers.indexOf(it.toString()), true)
            }
            if (requestCode == 888) {
                user?.birthdays?.remove(it)
                trackedUsers.remove(it)
                edit_birthday.text.clear()
                mDatabase.child("users").child(globalUserId).setValue(user)
                personAdapter.notifyDataSetChanged()
                person_spinner.setSelection(trackedUsers.size - 2)
            }
            if(requestCode == 999) {
                user?.birthdays?.set(it.toString(), 0)
                edit_birthday.text.clear()
                mDatabase.child("users").child(globalUserId).setValue(user)
                trackedUsers.remove(Const.ADD_NEW)
                trackedUsers.add(it.toString())
                trackedUsers.add(Const.ADD_NEW)
                setBirthdayText = prepareDialogPickerMessage(it.toString())
                personAdapter.notifyDataSetChanged()
                person_spinner.setSelection(trackedUsers.size - 2)
                showDateDialog()
            }
        }
    }

    private fun showDateDialog(){
        dpd = DatePickerDialog(activity, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            val birthdayDate =  "" + dayOfMonth + "." + (monthOfYear +1) + "."+ year
            val formatter = DateTimeFormat.forPattern("dd.MM.yyyy")
            val birthdayDateDate = formatter.parseDateTime(birthdayDate)
            if (user == null) {
                user = UserBirthday()
            }
            val userName = rootView.person_spinner.selectedItem.toString()
            user?.birthdays?.set(userName, convertDateToLong(year, monthOfYear + 1, dayOfMonth))
            user?.token = prefs?.getString(Const.TOKEN, "")!!

            birthday.setText(birthdayDate)
            updateBirthdayComponents(birthdayDateDate.millis)
            mDatabase.child("users").child(prefs?.getString(Const.GLOBAL_USER_ID,"todo")!!).setValue(user)
            EventBus.getDefault().post(UpdateEvent(0, getDaysToNow(birthdayDateDate)))
            restartHandlerRuns(birthdayDateDate)
            if (userName == Const.YOU) {
                prefs?.edit()?.putLong(Const.BIRTHDAY, birthdayDateDate.millis)?.apply()
            }
        }, birthdaysComponents[2], birthdaysComponents[1], birthdaysComponents[0])
        if(!dpd.isShowing){
            dpd.setMessage(setBirthdayText)
            dpd.show()
        }
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
        handler.removeCallbacksAndMessages(null)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(event: NextToOutliveEvent) {
        next_to_outlive_title.text = event.name
        if (event.daysMore == 0L) {
            next_to_outlive_subtitle.text = getString(R.string.outlived_today)
        } else {
            next_to_outlive_subtitle.text = java.lang.String.format(getString(R.string.days_more), event.daysMore)
        }
        Glide.with(this).load(event.avatar).into(avatar)

        nextToOutliveView.setOnClickListener {
            val activity = context as FragmentActivity
            val fm = activity.supportFragmentManager
            val pop = FragmentCelebrityDetails.newInstance("",0, Celebrity(event.name,123,event.avatar))
            pop.setTargetFragment(fm.primaryNavigationFragment, 1000)
            pop.show(fm, "Celebrity details")
        }

    }
}