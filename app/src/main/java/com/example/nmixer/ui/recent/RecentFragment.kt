package com.example.nmixer.ui.recent

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
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
import com.example.nmixer.models.Music
import com.example.nmixer.models.Share
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_recent.*

class RecentFragment : Fragment()  {

    private lateinit var recentViewModel: RecentViewModel
    private var musicAdapter = MusicsAdapter()
    var musics : MutableList<Music> = ArrayList()
    private var shares : MutableList<Share> = ArrayList()
    private var conf = false
    var auth : FirebaseAuth? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_recent, container, false)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        musicAdapter = MusicsAdapter()
        listViewRecent.adapter = musicAdapter
        llProgressBarRecent.visibility = View.VISIBLE

        recentViewModel = ViewModelProviders.of(this).get(RecentViewModel::class.java)

        recentViewModel.shares?.observe(this, Observer {
            shares.clear()
            musics.clear()
            musicAdapter.notifyDataSetChanged()

            it?.let {
                conf = true
                for (share in it){
                    shares.add(share)
                }
                getMusics()
            }

            if (it == null){
                llProgressBarRecent.visibility = View.GONE
                textViewListNull.visibility = View.VISIBLE
            }
        })
    }

    private fun getMusics(){
        recentViewModel.musics?.observe(this, Observer {
            musics.clear()
            musicAdapter.notifyDataSetChanged()

            it?.let {
                for (share in shares){
                    for (music in it) {
                        if (share.idMusic == music.id){
                            musics.add(music)
                            musicAdapter.notifyDataSetChanged()
                        }
                    }
                }
                getUsers()
            }
        })
    }

    private fun getUsers(){
        recentViewModel.users?.observe(this, Observer {
            it?.let {
                for (user in it) {
                    for (music in musics)
                    {
                        if (user.id == music.idUser){
                            music.user = user
                            musicAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            if (conf){
                llProgressBarRecent.visibility = View.GONE
                textViewListNull.visibility = View.GONE
            }
        })
    }

    inner class MusicsAdapter : BaseAdapter() {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val view = layoutInflater.inflate(R.layout.row_recent_music,parent, false)
            val textViewTitleMusic = view.findViewById<TextView>(R.id.textViewTitleMusic)
            val textViewAuthor = view.findViewById<TextView>(R.id.textViewAuthor)
            val textViewTime = view.findViewById<TextView>(R.id.textViewTime)
            val music = musics[position]


            textViewTitleMusic.text = music.title
            textViewAuthor.text =  if (music.user?.name != null) music.user?.name else music.idUser
            textViewTime.text = music.time

            view .setOnClickListener {
                com.example.nmixer.playSong("https://firebasestorage.googleapis.com/v0/b/nmixer-97a91.appspot.com/o/musics%2FDark%20World.mp3?alt=media&token=f8564ca6-cf59-468d-bd98-13ff646a1752")

                /*auth = FirebaseAuth.getInstance()
                Favorite.insertFavorite(auth!!.uid.toString(), music.id)
                val intent = Intent(requireActivity(), TrackActivity::class.java)
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