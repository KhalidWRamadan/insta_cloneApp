package com.example.insta.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.insta.Adapter.PostAdapter
import com.example.insta.Adapter.StoryAdapter
import com.example.insta.ChatActivity
import com.example.insta.Model.Post
import com.example.insta.Model.Story
import com.example.insta.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home.view.*


class HomeFragment : Fragment()  {
    // TODO: Rename and change types of parameters
    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var followingList: MutableList<String>? = null

    private var storyAdapter: StoryAdapter? = null
    private var storyList: MutableList<Story>? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)






        var recyclerView: RecyclerView? = null
        var recyclerViewStory: RecyclerView? = null

        recyclerView = view.findViewById(R.id.recycler_view_home)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        recyclerViewStory = view.findViewById(R.id.recycler_view_story)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager2 = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewStory.layoutManager = linearLayoutManager2





        storyList = ArrayList()
        storyAdapter = context?.let { StoryAdapter(it, storyList as ArrayList<Story>) }
        recyclerViewStory.adapter = storyAdapter

        checkFollowings()


        view.send_view.setOnClickListener {
            var intent = Intent(context, ChatActivity::class.java)
            startActivity(intent)
        }


        return view
    }

    private fun checkFollowings() {
        followingList = ArrayList()
        var followingRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (followingList as ArrayList<String>).clear()
                    for (snapshot in p0.children) {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                        retrievePosts()
                        retrieveStories()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun retrievePosts() {
        var postRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Posts")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                postList?.clear()
                for (snapshot in p0.children) {
                    val post = snapshot.getValue(Post::class.java)
                    for (id in (followingList as ArrayList<String>)) {
                        if (post!!.getPublisher() == id) {
                            postList!!.add(post)

                        }
                        postAdapter!!.notifyDataSetChanged()


                    }

                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }


    private fun retrieveStories() {
        var storyRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Story")
        storyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val timeCurrent = System.currentTimeMillis()
                (storyList as ArrayList<Story>).clear()
                (storyList as ArrayList<Story>).add(
                    Story(
                        "",
                        0,
                        0,
                        "",
                        FirebaseAuth.getInstance().currentUser!!.uid
                    )
                )

                for (id in followingList!!) {
                    var countStory = 0
                    var story: Story? = null
                    for (snapshot in dataSnapshot.child(id).children) {
                        story = snapshot.getValue(Story::class.java)
                        if (timeCurrent > story!!.getTimeStart() && timeCurrent < story!!.getTimeEnd()) {
                            countStory++
                        }
                    }
                    if (countStory > 0) {

                        (storyList as ArrayList<Story>).add(story!!)

                    }
                }
                storyAdapter!!.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }
}
