package com.example.cinewave.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.cinewave.R
import com.example.cinewave.fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment=supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController=navHostFragment.navController
        val bottomNavigationView=findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setupWithNavController(navController)

        val searchBar = findViewById<ImageView>(R.id.searchicon)
        searchBar.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
        val upperLayout = findViewById<ConstraintLayout>(R.id.upperlayout)
        searchBar.setOnClickListener {
            navController.navigate(R.id.searchFragment)
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.searchFragment) {
                searchBar.visibility = View.GONE
                upperLayout.visibility = View.GONE
            } else {
                searchBar.visibility = View.VISIBLE
                upperLayout.visibility = View.VISIBLE
            }
        }
    }
}