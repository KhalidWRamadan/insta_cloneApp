package com.example.insta.Adapter

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.insta.Model.User
import com.example.insta.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersActivity : AppCompatActivity() {
    var id: String = ""
    var title: String = ""
    var userAdapter: UserAdapter? = null
    var userList: List<User>? = null
    var idList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_users)

        val intent = intent
        id = intent.getStringExtra("id")!!
        title = intent.getStringExtra("title")!!
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        var recyclerView: RecyclerView
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<User>, false)
        recyclerView.adapter = userAdapter
        idList = ArrayList()
        when (title) {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()

        }

    }

    private fun getViews() {

        val ref = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference
            .child("Story").child(id!!).child(intent.getStringExtra("storyid").toString()).child("views")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for (snapshot in p0.children) {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()

            }
        })

    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference
            .child("Follow").child(id!!)
            .child("Followers")


        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for (snapshot in p0.children) {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()

            }
        })

    }

    private fun getFollowing() {
        val followungsRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference
            .child("Follow").child(id!!)
            .child("Following")


        followungsRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (idList as ArrayList<String>).clear()
                for (snapshot in p0.children) {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUsers()
            }
        })
    }

    private fun getLikes() {

        var LikeRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Likes").child(id!!)

        LikeRef.addValueEventListener(object : ValueEventListener {


            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {

                    (idList as ArrayList<String>).clear()
                    for (snapshot in p0.children) {
                        (idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUsers()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun showUsers() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
        usersRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(User::class.java)

                    for (id in idList!!) {
                        if (user!!.getUID() == id) {
                            (userList as ArrayList<User>).add(user!!)
                        }
                    }


                }
                userAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}