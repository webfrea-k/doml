package webfreak.si.doml

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_my_data.*
import kotlinx.android.synthetic.main.fragment_my_data.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import webfreak.si.doml.R.id.*
import webfreak.si.doml.objects.*
import java.lang.ArithmeticException
import kotlin.system.measureTimeMillis

class FragmentMyData : Fragment() {
    private lateinit var mDatabase: DatabaseReference
    private var trackedUsers: MutableList<String> = arrayListOf()
    private var user: UserBirthday? = null
    private var globalUserId:String = ""
    private var actualBirthdayDate = DateTime()
    private var handler = Handler()
    private lateinit var runnableCode: Runnable
    private lateinit var nextToOutliveView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val prefs = PreferenceHelper.defaultPrefs(activity)
        val rootView = inflater.inflate(R.layout.fragment_my_data, container, false)
        val birthday = rootView.edit_birthday
        nextToOutliveView = rootView.next_to_outlive_view
        globalUserId = prefs.getString(Const.GLOBAL_USER_ID,"") ?: ""
        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("users").child(globalUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.getValue(UserBirthday::class.java).let { onlineUser ->
                    user = onlineUser
                    onlineUser?.birthdays?.keys?.let { list ->
                        val birthdays = mutableListOf(Const.YOU)
                        val birthdaysTemp = list.toMutableList()

                        birthdaysTemp.remove(Const.ADD_NEW)
                        birthdaysTemp.remove(Const.YOU)

                        birthdaysTemp.add(Const.ADD_NEW)
                        birthdaysTemp.forEach{
                            birthdays.add(it)
                        }
                        trackedUsers = birthdays
                        person_spinner?.let {
                            it.adapter = ArrayAdapter(activity, R.layout.spinner_item, trackedUsers)
                            user?.birthdays?.get(person_spinner.selectedItem)?.let {time ->
                                birthday.setText(convertLongToTime(time))
                                //prefs.edit().putLong(Const.DAYS_ALIVE, getDaysToNowFromString(time)).apply()
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                trackedUsers = mutableListOf(Const.YOU, Const.ADD_NEW)
                person_spinner.adapter = ArrayAdapter(activity, R.layout.spinner_item, trackedUsers)
            }
        })

        val birthdaysComponents = arrayOf(1,0,2000)
        val datePicker = rootView.btn_select_birthday
        datePicker.setOnClickListener {
            val dpd = DatePickerDialog(activity, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val birthdayDate =  "" + dayOfMonth + "." + (monthOfYear +1) + "."+ year
                val formatter = DateTimeFormat.forPattern("dd.MM.yyyy")
                val birthdayDateDate = formatter.parseDateTime(birthdayDate)
                if (user == null) {
                    user = UserBirthday()
                }
                user?.birthdays?.set(rootView.person_spinner.selectedItem.toString(), convertDateToLong(year, monthOfYear + 1, dayOfMonth))
                user?.token = prefs.getString(Const.TOKEN, "")!!

                birthday.setText(birthdayDate)
                mDatabase.child("users").child(prefs.getString(Const.GLOBAL_USER_ID,"todo")!!).setValue(user)

                //prefs.edit().putLong(Const.DAYS_ALIVE, getDaysToNow(birthdayDateDate)).apply()

                EventBus.getDefault().post(UpdateEvent(0, getDaysToNow(birthdayDateDate)))
                actualBirthdayDate = birthdayDateDate
                restartHandlerRuns()
            }, birthdaysComponents[2], birthdaysComponents[1], birthdaysComponents[0])
            dpd.show()
        }

        rootView.person_spinner.adapter = ArrayAdapter(activity, R.layout.spinner_item, trackedUsers)
        rootView.person_spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val text = rootView.person_spinner.getItemAtPosition(position).toString()
                if (text == Const.ADD_NEW) {
                    val pop = FragmentAddPerson()
                    val fm = activity?.supportFragmentManager
                    pop.setTargetFragment(this@FragmentMyData, 1000)
                    pop.show(fm, "Add Person")
                } else {
                    user?.birthdays?.get(text)?.let {
                        if(it == 0L) {
                            birthday.text.clear()
                            datePicker.performClick()
                        } else {
                            birthday.setText(convertLongToTime(it))
                            val birthDate = DateTime(it).withTimeAtStartOfDay()
                            actualBirthdayDate = birthDate
                            if(prefs.getLong(Const.BIRTHDAY,0) != birthDate.millis) {
                                prefs.edit().putLong(Const.BIRTHDAY, birthDate.millis).apply()
                            }
                            EventBus.getDefault().postSticky(UpdateEvent(0, Static.getDaysAlive(context!!).toLong()))
                            val random = kotlin.random.Random.nextInt(0,10)
                            //display ad probability 3/7
                            EventBus.getDefault().postSticky(ShowInterstitial(random > 7))
                            restartHandlerRuns()
                        }
                    }
                }
            }
        }

        return rootView
    }

    fun restartHandlerRuns() {
        handler.removeCallbacksAndMessages(null)
        runnableCode = object: Runnable {
            override fun run() {
                updateCounters(actualBirthdayDate)
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

    private fun getDaysToNowFromString(birth: Long): Long {
        val birthDate = DateTime(birth)
        val currentDate = DateTime().plusDays(1).withTimeAtStartOfDay()
        return Days.daysBetween(birthDate, currentDate).days.toLong()
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
            user?.birthdays?.set(it.toString(), 0)
            edit_birthday.text.clear()
            mDatabase.child("users").child(globalUserId).setValue(user)
            trackedUsers.remove(Const.ADD_NEW)
            trackedUsers.add(it.toString())
            trackedUsers.add(Const.ADD_NEW)
            person_spinner.adapter = ArrayAdapter(activity, R.layout.spinner_item, trackedUsers)
            person_spinner.setSelection(trackedUsers.size - 2)
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