package webfreak.si.doml

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.fragment_my_data.*
import kotlinx.android.synthetic.main.fragment_my_data.view.*
import org.joda.time.DateTime
import org.joda.time.Minutes
import webfreak.si.doml.objects.UserBirthday

class FragmentMyData : Fragment() {
    private lateinit var mDatabase: DatabaseReference
    private var trackedUsers: MutableList<String> = arrayListOf()
    private var user: UserBirthday? = null
    private var globalUserId:String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val prefs = PreferenceHelper.defaultPrefs(activity)
        val rootView = inflater.inflate(R.layout.fragment_my_data, container, false)
        val birthday = rootView.edit_birthday
        globalUserId = prefs.getString(Const.GLOBAL_USER_ID,"todo")!!
        mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("users").child(globalUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.getValue(UserBirthday::class.java).let {
                    user = it
                    it?.birthdays?.keys?.let { list ->
                        val birthdays = mutableListOf(Const.YOU)
                        val birthdaysTemp = list.toMutableList()

                        birthdaysTemp.remove(Const.ADD_NEW)
                        birthdaysTemp.remove(Const.YOU)

                        birthdaysTemp.add(Const.ADD_NEW)
                        birthdaysTemp.forEach{
                            birthdays.add(it)
                        }

                        trackedUsers = birthdays

                        person_spinner.adapter = ArrayAdapter(activity, R.layout.spinner_item, trackedUsers)
                        val birthdayLong = user!!.birthdays[person_spinner.selectedItem]
                        birthday.setText(convertLongToTime(birthdayLong!!))
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                trackedUsers = mutableListOf(Const.YOU, Const.ADD_NEW)
                person_spinner.adapter = ArrayAdapter(activity, R.layout.spinner_item, trackedUsers)
            }
        })

        val birthdaysComponents = arrayOf(1,1,2000)
        val datePicker = rootView.btn_select_birthday
        datePicker.setOnClickListener {
            val dpd = DatePickerDialog(activity, DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val birthdayDate =  "" + dayOfMonth + "." + (monthOfYear +1) + "."+ year
                user!!.birthdays[rootView.person_spinner.selectedItem.toString()] = convertDateToLong(birthdayDate)
                birthday.setText(birthdayDate)
                mDatabase.child("users").child(prefs.getString(Const.GLOBAL_USER_ID,"todo")!!).setValue(user)

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
                    user?.birthdays?.get(text).let {
                        if(it == 0L) {
                            birthday.text.clear()
                            datePicker.performClick()
                        } else {
                            birthday.setText(it?.let { it1 -> convertLongToTime(it1) })
                            val currentDate = DateTime()
                            val birthDate = DateTime(it)
                            val minutesAlive = Minutes.minutesBetween(birthDate, currentDate).minutes
                            prefs.edit().putLong(Const.BIRTHDAY, (minutesAlive / 60L) / 24L).apply()
                        }
                    }
                }
            }
        }
        return rootView
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
        return df.parse(date).time
    }

    fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
        return format.format(date)
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
}