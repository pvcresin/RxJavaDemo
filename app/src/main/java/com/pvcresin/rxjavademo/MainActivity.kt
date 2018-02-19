package com.pvcresin.rxjavademo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.*


class MainActivity : AppCompatActivity() {

    val TAG = "GitHub"
    // task manager
    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // operate list
        val listTask = Observable
                .just(
                        Arrays.asList("Alice", "Bob", "Charlie"),
                        Arrays.asList("Dave", "Ellen", "Frank")
                )
                .subscribeOn(Schedulers.io())
                .flatMap { list -> Observable.fromIterable<String>(list) }  // flatten
                .filter { name -> name.length > 4 }                         // filter by length
                .map { name -> "$name(length ${name.length})" }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { text -> Log.d(TAG, "text: $text") },
                        { t: Throwable? -> t?.printStackTrace() },
                        { Log.d(TAG, "complete") }
                )

        compositeDisposable.add(listTask)

        // operate api
        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.github.com/")
                .build()

        val service = retrofit.create(GitHubService::class.java)

        val singleTask = service.listRepos("pvcresin")
                .subscribeOn(Schedulers.io())               // async process (api)
                .observeOn(AndroidSchedulers.mainThread())  // get result at UI thread
                .subscribe { list ->
                    list.filterIndexed { index, repo -> index < 5 }
                            .forEach { repo -> Log.d(TAG, repo.toString()) }
                }

        compositeDisposable.add(singleTask)
    }

    override fun onDestroy() {
        compositeDisposable.clear()     // dispose all task
        super.onDestroy()
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