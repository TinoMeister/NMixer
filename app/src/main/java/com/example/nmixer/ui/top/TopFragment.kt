package com.example.nmixer.ui.top

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.nmixer.PlayerActivity
import com.example.nmixer.R
import com.example.nmixer.models.Favorite
import com.example.nmixer.models.Share
import kotlinx.android.synthetic.main.fragment_top.*

class TopFragment : Fragment()  {

    private lateinit var topViewModel: TopViewModel
    private var musicAdapter = MusicsAdapter()
    private var favorites : MutableList<Favorite> = ArrayList()
    private var shares : MutableList<Share> = ArrayList()
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
            shares.clear()
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

    private fun getShares(){
        topViewModel.shares?.observe(this, Observer {
            shares.clear()
            musicAdapter.notifyDataSetChanged()

            it?.let {
                for (favorite in favorites){
                    for (share in it){
                        if (share.idMusic == favorite.idMusic){
                            shares.add(share)
                            musicAdapter.notifyDataSetChanged()
                        }
                    }
                }
                getMusics()
            }
        })
    }

    private fun getMusics(){
        topViewModel.musics?.observe(this, Observer {
            it?.let {
                for (share in shares){
                    for (music in it){
                        if (music.id == share.idMusic){
                            share.music = music
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
                    for (share in shares)
                    {
                        if (user.id == share.music?.idUser){
                            share.music?.user = user
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
        getShares()
    }

    inner class MusicsAdapter : BaseAdapter() {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val view = layoutInflater.inflate(R.layout.row_top_music,parent, false)
            val textViewTitleMusic = view.findViewById<TextView>(R.id.textViewTitleMusic)
            val textViewAuthor = view.findViewById<TextView>(R.id.textViewAuthor)
            val textViewTime = view.findViewById<TextView>(R.id.textViewTime)
            val share = shares[position]

            textViewTitleMusic.text = share.music?.title
            textViewAuthor.text =  if (share.music?.user?.name != null) share.music?.user?.name else share.music?.idUser
            textViewTime.text = share.music?.time

            view .setOnClickListener {
                val intent = Intent(requireActivity(), PlayerActivity::class.java)
                intent.putExtra(PlayerActivity.MUSIC, shares[position].toJson().toString())
                requireActivity().startActivity(intent)
            }

            return view
        }

        override fun getItem(position: Int): Any {
            return shares[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return shares.size
        }
    }
}