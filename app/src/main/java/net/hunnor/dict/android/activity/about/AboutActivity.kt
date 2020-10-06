package net.hunnor.dict.android.activity.about

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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

        var uri = Uri.parse(view.tag as String)

        try {
            val applicationInfo: ApplicationInfo = packageManager.getApplicationInfo("com.facebook.katana", 0)
            if (applicationInfo.enabled) {
                uri = Uri.parse("fb://facewebmodal/f?href=$uri")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // Facebook app isn't installed
        }

        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = uri
        startActivity(intent)

    }

}
