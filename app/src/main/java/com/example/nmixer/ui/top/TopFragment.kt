package com.example.nmixer.ui.top

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.nmixer.R
import com.example.nmixer.models.Favorite
import com.example.nmixer.models.Music
import kotlinx.android.synthetic.main.fragment_top.*

class TopFragment : Fragment()  {

    private lateinit var topViewModel: TopViewModel
    private var musicAdapter = MusicsAdapter()
    private var favorites : MutableList<Favorite> = ArrayList()
    private var musics : MutableList<Music> = ArrayList()
    private var conf = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_top, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        musicAdapter = MusicsAdapter()
        listViewTop.adapter = musicAdapter
        llProgressBarTop.visibility = View.VISIBLE

        topViewModel = ViewModelProviders.of(this).get(TopViewModel::class.java)

        topViewModel.favorites?.observe(this, Observer {
            favorites.clear()
            musics.clear()
            musicAdapter.notifyDataSetChanged()
            it?.let {
                for (favorite in it){
                    favorites.add(favorite)
                }
                conf = true
                orderFavorites()
            }

            if (it == null) {
                llProgressBarTop.visibility = View.GONE
                textViewListNull.visibility = View.VISIBLE
            }
        })
    }

    private fun getMusics(){
        topViewModel.musics?.observe(this, Observer {
            musics.clear()
            musicAdapter.notifyDataSetChanged()

            it?.let {
                for (favorite in favorites){
                    for (music in it){
                        if (music.id.toString() == favorite.idMusic.toString()){
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
        topViewModel.users?.observe(this, Observer {
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
                llProgressBarTop.visibility = View.GONE
                textViewListNull.visibility = View.GONE
            }
        })
    }

    private fun orderFavorites(){
        val favorites : MutableList<Favorite> = ArrayList()
        val total : MutableList<Int> = ArrayList()
        var count = 0
        val sizeFavorites= this.favorites.size - 1
        val sizeFavorites2 : Int

        //Get all the "likes"
        for (i in 0..sizeFavorites){
            for (j in i..sizeFavorites){
                if (this.favorites[i].idMusic == this.favorites[j].idMusic && i + 1 <= sizeFavorites){
                    count++
                }
                else if (((favorites.size - 1) > 0 && this.favorites[i].idMusic != favorites[(favorites.size-1)].idMusic) || favorites.size == 0|| i+1 > sizeFavorites){
                    favorites.add(this.favorites[i])
                    total.add(count)
                    count = 0
                    break
                }
                else{
                    count = 0
                }
            }
        }

        sizeFavorites2 = favorites.size - 1

        //Order the favorites
        for (i in 0..sizeFavorites2){
            for (j in i..sizeFavorites2){
                if (total[i] < total[j]){
                    val value = favorites[i]
                    favorites[i] = favorites[j]
                    favorites[j] = value
                }
            }
        }

        this.favorites = favorites
        getMusics()
    }

    inner class MusicsAdapter : BaseAdapter() {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val view = layoutInflater.inflate(R.layout.row_top_music,parent, false)
            val textViewTitleMusic = view.findViewById<TextView>(R.id.textViewTitleMusic)
            val textViewAuthor = view.findViewById<TextView>(R.id.textViewAuthor)
            val textViewTime = view.findViewById<TextView>(R.id.textViewTime)
            val music = musics[position]

            textViewTitleMusic.text = music.title
            textViewAuthor.text =  if (music.user?.name != null) music.user?.name else music.idUser
            textViewTime.text = music.time

            view .setOnClickListener {
                com.example.nmixer.playSong("https://firebasestorage.googleapis.com/v0/b/nmixer-97a91.appspot.com/o/musics%2FDark%20World.mp3?alt=media&token=f8564ca6-cf59-468d-bd98-13ff646a1752")
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