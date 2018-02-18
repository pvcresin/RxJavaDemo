package com.pvcresin.rxjavademo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = "GitHub"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.github.com/")
                .build()

        val service = retrofit.create(GitHubService::class.java)

        service.listRepos("pvcresin")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { list ->
                    list.forEach { repo ->
                        Log.d(TAG, repo.toString())
                    }
                }
    }
}

fun doSomethingToList(list: List<Repo>?) {
    list?.forEach {
        Log.d(MainActivity.TAG, it.toString())
    }
}

interface GitHubService {
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String): Single<List<Repo>>
}

data class Repo(
        val full_name: String,
        val html_url: String,
        val description: String
)