package com.teymoorianar.amnnote.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.teymoorianar.amnnote.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hosts the [SettingsFragment] that exposes user preferences.
 */
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(findViewById(R.id.settingsToolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.settings_container, SettingsFragment())
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        /** Creates an [Intent] that opens the settings screen. */
        fun newIntent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
