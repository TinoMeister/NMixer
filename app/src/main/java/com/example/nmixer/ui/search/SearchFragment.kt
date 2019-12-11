package com.example.nmixer.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.nmixer.R
import com.example.nmixer.models.Music
import com.example.nmixer.models.Share
import kotlinx.android.synthetic.main.fragment_search.*
import java.text.Normalizer

class SearchFragment : Fragment()  {

    private lateinit var searchViewModel: SearchViewModel
    private var musicAdapter = MusicsAdapter()
    private var musicsList : MutableList<Music> = ArrayList()
    private var musicsTotal : MutableList<Music> = ArrayList()
    private var shares : MutableList<Share> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        this.musicAdapter = MusicsAdapter()
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
                        }
                        u.isNotEmpty() -> {
                            musicsList = ArrayList(u)
                        }
                    }
                }
                else{
                    musicsList = musicsTotal
                }

                musicAdapter.notifyDataSetChanged()
                return false
            }

        })
    }



    inner class MusicsAdapter : BaseAdapter() {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            val view = layoutInflater.inflate(R.layout.row_search_music, parent, false)
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

    fun removeAccents(text: String): String{
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }
}