package com.example.nmixer.ui.home

import android.annotation.SuppressLint
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
import com.example.nmixer.R
import com.example.nmixer.models.Music
import com.example.nmixer.models.Share
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    var auth : FirebaseAuth? = null
    lateinit var database : FirebaseDatabase
    private lateinit var myRef : DatabaseReference
    private var musicAdapter = MusicsAdapter()
    var musics : MutableList<Music> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        return inflater.inflate(R.layout.fragment_home, container, false)
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
            val idMusic = UUID.randomUUID().toString()
            myRef = database.getReference("Musics")
                .child(idMusic)

            val music = Music (
                idMusic,
                "title" ,
                "2019/07/21",
                "2:30",
                auth?.uid.toString()
            )

            myRef.setValue(music)
        }
    }

    fun getShares(musicId : String, callback: (Boolean) -> Unit ){
        var conf = false

        doAsync {
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Shares")

            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    if (data.exists()){
                        for (d in data.children){
                            if (d.child("idMusic" ).value.toString() == musicId){
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

            val view = layoutInflater.inflate(R.layout.row_home_music,parent, false)
            val textViewTitleMusic = view.findViewById<TextView>(R.id.textViewTitleMusic)
            val textViewTime = view.findViewById<TextView>(R.id.textViewTime)
            val textViewDate = view.findViewById<TextView>(R.id.textViewDate)
            val music = musics[position]

            textViewTitleMusic.text = music.title
            textViewTime.text = music.time
            textViewDate.text = music.date

            view .setOnClickListener {

                /* Code for Share*/
                getShares(music.id!!){
                    if (!it){
                        val idShare = UUID.randomUUID().toString()
                        val database = FirebaseDatabase.getInstance()
                        val myRef = database.getReference("Shares")
                            .child(idShare)

                        val share = Share(
                            idShare,
                            music.id,
                            "Public",
                            "https://firebasestorage.googleapis.com/v0/b/nmixer-97a91.appspot.com/o/musics%2FDark%20World.mp3?alt=media&token=f8564ca6-cf59-468d-bd98-13ff646a1752"
                        )

                        myRef.setValue(share)
                    }
                }



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