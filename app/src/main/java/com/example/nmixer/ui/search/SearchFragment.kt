package com.example.nmixer.ui.search

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
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

        MainActivity.getSearch("Search")

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
                MainActivity.musicsTotal = musicsTotal
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