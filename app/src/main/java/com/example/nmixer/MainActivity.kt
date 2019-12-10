package com.example.nmixer

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.nmixer.models.Music
import com.example.nmixer.models.User
import com.example.nmixer.ui.search.SearchFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.nav_header_main.*
import java.text.Normalizer


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_recent, R.id.nav_search,
                R.id.nav_top, R.id.nav_liked, R.id.nav_tools
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.

        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)

        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.item_search)
        val searchView = searchItem.actionView as SearchView

        SearchItem = searchItem


        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                searchView.setQuery(query, false)
                searchItem.collapseActionView()
                return true
            }

            @SuppressLint("DefaultLocale")
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()){
                    val m = musicsTotal.filter {
                        removeAccents(it.title!!.toLowerCase()).contains(newText, true)
                    }
                    val u = musicsTotal.filter {
                            removeAccents(it.user!!.name!!.toLowerCase()).contains(newText, true)
                        }

                    when {
                        m.isNotEmpty() -> {
                            SearchFragment.musicsList = ArrayList(m)
                            SearchFragment.listView.adapter = SearchFragment.musicAdapterResult
                            SearchFragment.musicAdapterResult.notifyDataSetChanged()
                        }
                        u.isNotEmpty() -> {
                            SearchFragment.userList.clear()
                            var conf = false

                            for (i in 0 until ArrayList(u).size){
                                for (j in  (i + 1) until ArrayList(u).size){
                                    if (ArrayList(u)[i].user!!.name == ArrayList(u)[j].user!!.name)
                                        conf = true
                                }

                                if (!conf)
                                    SearchFragment.userList.add(ArrayList(u)[i])

                                conf = false
                            }

                            SearchFragment.listView.adapter = SearchFragment.userAdapterResult
                            SearchFragment.userAdapterResult.notifyDataSetChanged()
                        }
                        else -> {
                            SearchFragment.musicsList = ArrayList(m)
                            SearchFragment.listView.adapter = SearchFragment.musicAdapterResult
                            SearchFragment.musicAdapterResult.notifyDataSetChanged()
                        }
                    }
                }
                else{
                    SearchFragment.musicsList = musicsTotal
                    SearchFragment.listView.adapter = SearchFragment.musicAdapterResult
                    SearchFragment.musicAdapterResult.notifyDataSetChanged()
                }
                return false
            }

        })
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)

        User.getUser{
            it?.let {
                nomeUser.text = it.name
                emailUser.text = it.email

                if (it.imageUrl != "") {
                    Glide.with(this@MainActivity)
                        .load(it.imageUrl)
                        .onlyRetrieveFromCache(true)
                        .into(imageView)
                }
            }
        }

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object{
        var SearchItem : MenuItem? = null
        var musicsTotal : MutableList<Music> = ArrayList()

        fun getSearch(typeFragment: String){
            SearchItem?.isVisible = typeFragment == "Search"
        }

        fun removeAccents(text: String): String{
            return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        }
    }
}
