package com.android.quanlynhanvien

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.qhutch.bottomsheetlayout.BottomSheetLayout
import me.ibrahimsn.lib.SmoothBottomBar


class MainActivity : BaseActivity() {
    private lateinit var navController: NavController
    private lateinit var bottomBar: SmoothBottomBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navController = findNavController(R.id.main_fragment)
        bottomBar = findViewById(R.id.bottomBar)


        bottomBar.onItemSelected = {
            val a = it
        }

        setupActionBarWithNavController(navController)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main_drawer, menu)
        bottomBar.setupWithNavController(menu!!,navController)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()
        return true
    }
}