package com.amitsinha.mrbolat_app.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.amitsinha.mrbolat_app.R
import com.amitsinha.mrbolat_app.activity.OrderActivity
import com.amitsinha.mrbolat_app.database.FoodDatabase
import com.amitsinha.mrbolat_app.database.FoodEntity
import com.amitsinha.mrbolat_app.model.Restaurant
import com.squareup.picasso.Picasso


class HomeRecyclerAdapter(val context: Context, private val itemList: ArrayList<Restaurant>) :
    RecyclerView.Adapter<HomeRecyclerAdapter.HomeViewHolder>() {

    class HomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        val textRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val textRestaurantPrice: TextView = view.findViewById(R.id.txtRestaurantPrice)
        val textRestaurantRating: TextView = view.findViewById(R.id.txtRestaurantRating)
        val imgRestaurantImage: ImageView = view.findViewById(R.id.imgRestaurantImage)
        val imgFavorite: ImageView = view.findViewById(R.id.imgFavorite)
        val llcontent:LinearLayout = view.findViewById(R.id.llContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_home_single_row, parent, false)

        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val restaurant = itemList[position]
        holder.textRestaurantName.text = restaurant.restaurantName
        val costForTwo = "${restaurant.restaurantPrice}/person"
        holder.textRestaurantPrice.text = costForTwo
        holder.textRestaurantRating.text = restaurant.restaurantRating
        Picasso.get().load(restaurant.restaurantImage).error(R.drawable.logo)
            .into(holder.imgRestaurantImage)

        val resEntity = FoodEntity(
            restaurant.restaurantId.toInt(),
            restaurant.restaurantName,
            restaurant.restaurantRating,
            restaurant.restaurantPrice,
            restaurant.restaurantImage
        )

        val checkFav = DBAsyncTask(
            context,
            resEntity,
            1
        ).execute()
        val isFav = checkFav.get()

        if (isFav) {
            holder.imgFavorite.setImageResource(R.drawable.ic_rating2)
        } else {
            holder.imgFavorite.setImageResource(R.drawable.ic_rating1)
        }

        holder.imgFavorite.setOnClickListener {

            if (!DBAsyncTask(
                    context,
                    resEntity,
                    1
                ).execute().get()
            ) {

                val async =
                    DBAsyncTask(
                        context,
                        resEntity,
                        2
                    ).execute()
                val result = async.get()

                if (result) {
                    holder.imgFavorite.setImageResource(R.drawable.ic_rating2)
                } else {
                    Toast.makeText(
                        context,
                        "Some Error Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {

                val async =
                    DBAsyncTask(
                        context,
                        resEntity,
                        3
                    ).execute()
                val result = async.get()

                if (result) {
                    holder.imgFavorite.setImageResource(R.drawable.ic_rating1)
                } else {
                    Toast.makeText(
                        context,
                        "Some Error Occurred",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }

        holder.llcontent.setOnClickListener {
            val intent = Intent(context, OrderActivity::class.java)
            intent.putExtra("id", restaurant.restaurantId)
            intent.putExtra("name", restaurant.restaurantName)
            intent.putExtra("rating", restaurant.restaurantRating)
            intent.putExtra("price", restaurant.restaurantPrice)
            intent.putExtra("image", restaurant.restaurantImage)
            context.startActivity(intent)
            (context as Activity).finish()
        }
        holder.imgRestaurantImage.setOnClickListener {
            val intent = Intent(context, OrderActivity::class.java)
            intent.putExtra("id", restaurant.restaurantId)
            intent.putExtra("name", restaurant.restaurantName)
            intent.putExtra("rating", restaurant.restaurantRating)
            intent.putExtra("price", restaurant.restaurantPrice)
            intent.putExtra("image", restaurant.restaurantImage)
            context.startActivity(intent)
            (context as Activity).finish()
        }
    }

    class DBAsyncTask(
        val context: Context,
        private val foodEntity: FoodEntity,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {

        private val db =
            Room.databaseBuilder(context, FoodDatabase::class.java, "restaurants-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {

            when (mode) {

                1 -> {
                    //Check DB if the restaurant is favorite or not
                    val res: FoodEntity? =
                        db.foodDao().getRestaurantById(foodEntity.restaurant_id.toString())
                    db.close()
                    return res != null
                }

                2 -> {
                    //Save the restaurant into DB as favorite
                    db.foodDao().insertRestaurant(foodEntity)
                    db.close()
                    return true
                }

                3 -> {
                    //Remove the favorite Restaurant
                    db.foodDao().deleteRestaurant(foodEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }

}
