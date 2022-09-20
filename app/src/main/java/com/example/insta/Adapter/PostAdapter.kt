package com.example.insta.Adapter


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.insta.CommentsActivity
import com.example.insta.Model.Post
import com.example.insta.Model.User
import com.example.insta.R
import com.example.insta.fragments.PostDetailsFragment
import com.example.insta.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter
    (
    private val mContext: Context,
    private val mPost: List<Post>
) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {


        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post = mPost[position]
        Picasso.get().load(post.getPostimage()).into(holder.postimage)
        if (post.getDescription().equals("")) {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.setText(post.getDescription())

        }
        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())

        isLikes(post.getPostid(), holder.likeButton)



        numperOfLikes(holder.likes, post.getPostid())
        getTotalComments(holder.comments, post.getPostid())
        checkSavedStatus(post.getPostid(),holder.saveButton)


        holder.postimage.setOnClickListener {

            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("postId",post.getPostid())
            editor.apply()
            (mContext as FragmentActivity).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                PostDetailsFragment()
            ).commit()

        }
        holder.publisher.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("profileId",post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                ProfileFragment()
            ).commit()
        }
        holder.profileImage.setOnClickListener {

            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("profileId",post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                ProfileFragment()
            ).commit()



        }
        holder.postimage.setOnClickListener {

            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("postId",post.getPostid())
            editor.apply()
            (mContext as FragmentActivity).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                PostDetailsFragment()
            ).commit()

        }


        holder.likeButton.setOnClickListener {
            if (holder.likeButton.tag == "Like") {
                FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Likes").child(post.getPostid())
                    .child(firebaseUser!!.uid).setValue(true)

                addNotification(post.getPublisher(),post.getPostid())

            } else {
                FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Likes").child(post.getPostid())
                    .child(firebaseUser!!.uid).removeValue()
//                val intent = Intent(mContext, MainActivity::class.java)
//                mContext.startActivity(intent)
            }

        }


        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id",post.getPostid())
            intent.putExtra("title","likes")
            mContext.startActivity(intent)
        }

        holder.commentButton.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId", post.getPostid())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)

        }
        holder.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId", post.getPostid())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)

        }
        holder.saveButton.setOnClickListener {
            if (holder.saveButton.tag == "Save"){
                FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Saves").child(firebaseUser!!.uid)
                    .child(post.getPostid()).setValue(true)
            }else{
                FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Saves").child(firebaseUser!!.uid)
                    .child(post.getPostid()).removeValue()
            }

        }
    }

    private fun numperOfLikes(likes: TextView, postid: String) {
        var LikeRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Likes").child(postid)

        LikeRef.addValueEventListener(object : ValueEventListener {


            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    likes.text = p0.childrenCount.toString() + " likes"
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun getTotalComments(comments: TextView, postid: String) {
        var commentsRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Comments").child(postid)

        commentsRef.addValueEventListener(object : ValueEventListener {


            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    comments.text = "view all" + p0.childrenCount.toString() + " Comments"
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun isLikes(postid: String, likeButton: ImageView) {

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        var LikeRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Likes").child(postid)
        LikeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(firebaseUser!!.uid).exists()) {
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                } else {
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    inner class ViewHolder(@NonNull itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var profileImage: CircleImageView
        var postimage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var userName: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postimage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButton = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            userName = itemView.findViewById(R.id.user_name_post)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)
        }
    }


    private fun publisherInfo(
        profileImage: CircleImageView,
        userName: TextView,
        publisher: TextView,
        publisherID: String
    ) {
        val userRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Users").child(publisherID)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profileImage)
                    userName.text = user!!.getUsername()
                    publisher.text = user!!.getFullname()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
    private fun checkSavedStatus(postid: String,imageView: ImageView){
        val savesRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Saves").child(firebaseUser!!.uid)
        savesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(postid).exists()){
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                }else{
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun addNotification(userId:String,postId:String){
        val notificationRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Notification").child(userId)
        val notificationMap = HashMap<String,Any>()
        notificationMap["userid"] =firebaseUser!!.uid
        notificationMap["text"] = "liked your post"
        notificationMap["postid"] = postId
        notificationMap["ispost"] = true
        notificationRef.push().setValue(notificationMap)
    }



}





