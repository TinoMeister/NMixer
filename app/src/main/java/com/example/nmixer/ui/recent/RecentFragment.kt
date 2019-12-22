package com.example.nmixer.ui.recent

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
import com.example.nmixer.PlayerActivity
import com.example.nmixer.R
import com.example.nmixer.models.Favorite
import com.example.nmixer.models.Share
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_recent.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecentFragment : Fragment()  {

    private lateinit var recentViewModel: RecentViewModel
    private var musicAdapter = MusicsAdapter()
    private var shares : MutableList<Share> = ArrayList()
    private var conf = false
    var auth : FirebaseAuth? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recent, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        musicAdapter = MusicsAdapter()
        listViewRecent.adapter = musicAdapter
        llProgressBarRecent.visibility = View.VISIBLE

        recentViewModel = ViewModelProviders.of(this).get(RecentViewModel::class.java)

        recentViewModel.shares?.observe(this, Observer {
            shares.clear()
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
            it?.let {
                for (share in shares){
                    for (music in it) {
                        if (share.idMusic == music.id){
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
        recentViewModel.users?.observe(this, Observer {
            it?.let {
                for (user in it) {
                    for (share in shares)
                    {
                        if (user.id == share.music!!.idUser){
                            share.music!!.user = user
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

    fun getFavorites(idUser : String, musicId : String, callback: (Boolean) -> Unit ){
        var conf = false

        doAsync {
            auth = FirebaseAuth.getInstance()
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Favorites")

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    if (data.exists()){
                        for (d in data.children){
                            if (d.child("idUser" ).value.toString() == idUser && d.child("idMusic" ).value.toString() == musicId){
                                conf = true
                                break
                            }
                        }
                    }

                    uiThread {
                        callback(conf)
                    }
                }

                override fun onCancelled(erro: DatabaseError) {
                    Log.d("Error / Share", erro.message)
                }
            })
        }
    }

    inner class MusicsAdapter : BaseAdapter() {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val view = layoutInflater.inflate(R.layout.row_recent_music,parent, false)
            val textViewTitleMusic = view.findViewById<TextView>(R.id.textViewTitleMusic)
            val textViewAuthor = view.findViewById<TextView>(R.id.textViewAuthor)
            val textViewTime = view.findViewById<TextView>(R.id.textViewTime)
            val music = shares[position].music


            textViewTitleMusic.text = music?.title
            textViewAuthor.text =  if (music?.user?.name != null) music.user?.name else music?.idUser
            textViewTime.text = music?.time

            view .setOnClickListener {
                //com.example.nmixer.playSong("https://firebasestorage.googleapis.com/v0/b/nmixer-97a91.appspot.com/o/musics%2FDark%20World.mp3?alt=media&token=f8564ca6-cf59-468d-bd98-13ff646a1752")

                auth = FirebaseAuth.getInstance()
                /* Code for likes */
                getFavorites(auth!!.uid!!, music?.id!!){
                    if (!it){
                        val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS")
                        val currentDate = sdf.format(Date())
                        val idFavorite = UUID.randomUUID().toString()
                        val database = FirebaseDatabase.getInstance()
                        val myRef = database.getReference("Favorites")
                            .child(idFavorite)

                        val favorite = Favorite (
                            idFavorite,
                            currentDate,
                            auth!!.uid,
                            music?.id
                        )

                        myRef.setValue(favorite)
                    }
                }

                /*val intent = Intent(requireActivity(), PlayerActivity::class.java)
                intent.putExtra(PlayerActivity.MUSIC, shares[position].toJson().toString())
                requireActivity().startActivity(intent)*/
            }

            return view
        }

        override fun getItem(position: Int): Any {
            return shares[position].music!!
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return shares.size
        }
    }
}