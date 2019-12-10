package com.example.nmixer.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.nmixer.MainActivity
import com.example.nmixer.R
import com.example.nmixer.TrackActivity
import com.example.nmixer.models.Favorite
import com.example.nmixer.models.Music
import com.example.nmixer.models.Share
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_home.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    var auth : FirebaseAuth? = null
    private var musicAdapter = MusicsAdapter()
    var musics : MutableList<Music> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.musicAdapter = MusicsAdapter()
        listViewMusic.adapter = musicAdapter
        llProgressBarHome.visibility = View.VISIBLE

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        homeViewModel.musics?.observe(this, Observer {
            musics.clear()
            musicAdapter.notifyDataSetChanged()

            it?.let {
                for (music in it) {
                    musics.add(music)
                    musicAdapter.notifyDataSetChanged()
                }

                llProgressBarHome.visibility = View.GONE
                textViewListNull.visibility = View.GONE
            }

            if (it == null){
                textViewListNull.visibility = View.VISIBLE
                llProgressBarHome.visibility = View.GONE
            }
        })

        fab.setOnClickListener {
            Music.insertMusic("title", "2019/07/21", "2:30")
        }
    }


    inner class MusicsAdapter : BaseAdapter() {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val view = layoutInflater.inflate(R.layout.row_home_music,parent, false)
            val textViewTitleMusic = view.findViewById<TextView>(R.id.textViewTitleMusic)
            val textViewTime = view.findViewById<TextView>(R.id.textViewTime)
            val textViewDate = view.findViewById<TextView>(R.id.textViewDate)
            val music = musics[position]

            textViewTitleMusic.text = music.title
            textViewTime.text = music.time
            textViewDate.text = music.date

            view .setOnClickListener {

                Share.insertShare(music.id, "Public")

                /* Code for Share*/

                //Share.insertShareFirebase(music.id, "Private")
                /* Code for likes */
                //Favorite.insertFavoriteFirebase(auth!!.uid.toString(), music.id)

                /*  Code for delete  */
                //Music.deleteMusicFirebase(music)



                /*val intent = Intent(requireActivity(), TrackActivity::class.java)
                //intent.putExtra(ArticleActivity.ARTICLE, article.toJson().toString())
                requireActivity().startActivity(intent)*/
            }

            return view
        }

        override fun getItem(position: Int): Any {
            return musics[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return musics.size
        }
    }
}