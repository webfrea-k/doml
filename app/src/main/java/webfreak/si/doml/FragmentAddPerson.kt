package webfreak.si.doml

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import kotlinx.android.synthetic.main.fragment_add_person.view.*


class FragmentAddPerson : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_add_person, container, false)
        rootView.cancel.setOnClickListener {
            this.dismiss()
        }
        rootView.add.setOnClickListener {
            if(TextUtils.isEmpty(rootView.person_name.text)) {
                rootView.person_name.error = getString(R.string.cannot_be_empty)
            } else {
                val i = Intent().putExtra("name", rootView.person_name.text)
                targetFragment!!.onActivityResult(targetRequestCode, Activity.RESULT_OK, i)
                this.dismiss()
            }
        }
        return rootView
    }
}