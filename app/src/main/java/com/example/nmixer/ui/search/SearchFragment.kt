package com.example.nmixer.ui.search

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nmixer.MainActivity
import com.example.nmixer.R
import com.example.nmixer.UserDetailActivity
import com.example.nmixer.models.Music
import com.example.nmixer.models.Share
import kotlinx.android.synthetic.main.fragment_search.*
import java.text.Normalizer

class SearchFragment : Fragment()  {

    private lateinit var searchViewModel: SearchViewModel
    private var musicAdapter = MusicsAdapter()
    private var userAdapter = UserAdapter()
    private var musicsTotal : MutableList<Music> = ArrayList()
    private var shares : MutableList<Share> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        musicAdapterResult = musicAdapter
        userAdapterResult = userAdapter
        listView = listViewSearch
        listViewSearch.adapter = musicAdapter

        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)

        searchViewModel.shares?.observe(this, Observer {
            musicsList.clear()
            musicsTotal.clear()
            musicAdapter.notifyDataSetChanged()

            it?.let {
                for (share in it){
                    shares.add(share)
                }
                getMusics()
            }

            /*if (it == null){
                llProgressBarRecent.visibility = View.GONE
                textViewListNull.visibility = View.VISIBLE
            }*/
        })

        setHasOptionsMenu(true)
    }

    fun removeAccents(text: String): String{
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_search, menu)

        val searchItem = menu?.findItem(R.id.item_search)
        val searchView = searchItem?.actionView as SearchView

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
                            musicsList = ArrayList(m)
                            listView.adapter = musicAdapterResult
                            musicAdapterResult.notifyDataSetChanged()
                        }
                        u.isNotEmpty() -> {
                            userList.clear()
                            var conf = false

                            for (i in 0 until ArrayList(u).size){
                                for (j in  (i + 1) until ArrayList(u).size){
                                    if (ArrayList(u)[i].user!!.name == ArrayList(u)[j].user!!.name)
                                        conf = true
                                }

                                if (!conf)
                                    userList.add(ArrayList(u)[i])

                                conf = false
                            }

                            listView.adapter = userAdapterResult
                            userAdapterResult.notifyDataSetChanged()
                        }
                        else -> {
                           musicsList = ArrayList(m)
                           listView.adapter = SearchFragment.musicAdapterResult
                           musicAdapterResult.notifyDataSetChanged()
                        }
                    }
                }
                else{
                    musicsList = musicsTotal
                    listView.adapter = musicAdapterResult
                    musicAdapterResult.notifyDataSetChanged()
                }
                return false
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return super.onOptionsItemSelected(item)

    }

    private fun getMusics(){
        searchViewModel.musics?.observe(this, Observer {
            musicsList.clear()
            musicsTotal.clear()
            musicAdapter.notifyDataSetChanged()
            it?.let {
                for (share in shares){
                    for (music in it) {
                        if (share.idMusic == music.id){
                            musicsTotal.add(music)
                        }
                    }
                }
                musicsList = musicsTotal
                musicAdapter.notifyDataSetChanged()
                getUsers()
            }
        })
    }

    private fun getUsers(){
        searchViewModel.users?.observe(this, Observer {
            it?.let {
                for (user in it) {
                    for (music in musicsList)
                    {
                        if (user.id == music.idUser){
                            music.user = user
                            musicAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            /*if (conf){
                llProgressBarRecent.visibility = View.GONE
                textViewListNull.visibility = View.GONE
            }*/
        })
    }

    inner class MusicsAdapter : BaseAdapter() {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val view = layoutInflater.inflate(R.layout.row_search_music,parent, false)
            val textViewName = view.findViewById<TextView>(R.id.textViewName)
            val textViewAuthor = view.findViewById<TextView>(R.id.textViewAuthor)
            val music = musicsList[position]

            textViewName.text = music.title
            textViewAuthor.text =  if (music.user?.name != null) music.user?.name else music.idUser


            view.setOnClickListener {
                com.example.nmixer.playSong("https://firebasestorage.googleapis.com/v0/b/nmixer-97a91.appspot.com/o/musics%2FDark%20World.mp3?alt=media&token=f8564ca6-cf59-468d-bd98-13ff646a1752")
                /*val intent = Intent(requireActivity(), UserDetailActivity::class.java)
                intent.putExtra(UserDetailActivity.USER, music.user!!.toJson().toString())
                requireActivity().startActivity(intent)*/
            }

            return view
        }

        override fun getItem(position: Int): Any {
            return musicsList[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return musicsList.count()
        }
    }

    inner class UserAdapter : BaseAdapter() {
        @SuppressLint("ViewHolder", "CheckResult")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val view = layoutInflater.inflate(R.layout.row_search_user,parent, false)
            val imageViewUser = view.findViewById<ImageView>(R.id.imageViewUser)
            val textViewUser = view.findViewById<TextView>(R.id.textViewUser)
            val music = userList[position]

            if (!music.user!!.imageUrl.isNullOrEmpty()){
                Glide.with(requireActivity())
                    .load(music.user!!.imageUrl)
                    .into(imageViewUser)
            }

            textViewUser.text = music.user!!.name

            view.setOnClickListener {
                val intent = Intent(requireActivity(), UserDetailActivity::class.java)
                intent.putExtra(UserDetailActivity.USER, music.user!!.toJson().toString())
                requireActivity().startActivity(intent)
            }

            return view
        }

        override fun getItem(position: Int): Any {
            return userList[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return userList.count()
        }
    }

    companion object {
        var musicsList : MutableList<Music> = ArrayList()
        lateinit var musicAdapterResult : MusicsAdapter
        var userList : MutableList<Music> = ArrayList()
        lateinit var userAdapterResult : UserAdapter
        lateinit var listView : ListView
    }
}