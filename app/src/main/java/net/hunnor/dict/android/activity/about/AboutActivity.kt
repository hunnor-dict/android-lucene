package net.hunnor.dict.android.activity.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import net.hunnor.dict.android.R
import net.hunnor.dict.android.activity.ActivityTemplate


class AboutActivity : ActivityTemplate() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }

    fun openLink(view: View) {
        val url = view.tag as String
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

}
