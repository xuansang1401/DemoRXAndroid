package com.xs.demorxkotlin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Predicate
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val disposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val footballPlayesObserver = getFootballPlayessObserver()
        val footballPlayesObservable: Observable<String> = getFootballPlayesObservable()

        // observer subscribing to observable

        // observer subscribing to observable
        footballPlayesObservable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .filter(Predicate<String> { s ->
                    Log.e("DDVH", "filter in Thread :: " + Thread.currentThread().name)
                    s.toLowerCase().startsWith("r")
                }) //                .observeOn(Schedulers.newThread())
                //                .map(new Function<String, String>() {
                //                    @Override
                //                    public String apply(String s) throws Exception {
                //                        Log.e("DDVH", "map in Thread :: "  + Thread.currentThread().getName());
                //                        return s.toUpperCase();
                //                    }
                //                })
                //                .flatMap(new Function<String, ObservableSource<String>>() {
                //                    @Override
                //                    public ObservableSource<String> apply(String s) throws Exception {
                //                        return Observable.just(s);
                //                    }
                //                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(footballPlayesObserver!!)

        /*** Note :

        subcribeOn sẽ làm cho tất cả hàm callback của Observerable và Observer chạy trong Thread nó set
        (gọi ở đâu cũng như nhau, nhiều lần thì tính theo thằng set ở trên cùng)

        observeOn sẽ làm cho tất cả các hàm phía sau khi gọi nó chạy trong Thread nó set
        observerOn có thể set nhiều lần để chuyển đổi Thread đang chạy thành Thread muốn chạy
        (chỉ tính các hàm gọi phía sau nó)


         */

        /*** Note :
         *
         * subcribeOn sẽ làm cho tất cả hàm callback của Observerable và Observer chạy trong Thread nó set
         * (gọi ở đâu cũng như nhau, nhiều lần thì tính theo thằng set ở trên cùng)
         *
         * observeOn sẽ làm cho tất cả các hàm phía sau khi gọi nó chạy trong Thread nó set
         * observerOn có thể set nhiều lần để chuyển đổi Thread đang chạy thành Thread muốn chạy
         * (chỉ tính các hàm gọi phía sau nó)
         *
         *
         */
        disposable.add(getNotesObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { note -> // Making the note to all uppercase
                    Observable.just(note)
                }
                .subscribeWith(getNotesObserver()))

        disposable.add(Observable.interval(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ aLong -> Log.e("DDVH", "số thứ $aLong") }) { throwable-> }
        )

    }

    private fun getFootballPlayessObserver(): Observer<String> {
        return object : Observer<String> {
            override fun onSubscribe(d: Disposable) {
                Log.e("DDVH", d.toString() + " === onSubscribe in Thread :: " + Thread.currentThread().name)
            }

            override fun onNext(s: String) {
                Log.e("DDVH", "Name: " + s + "in Thread :: " + Thread.currentThread().name)
            }

            override fun onError(e: Throwable) {
                Log.e("DDVH", "onError: " + e.message + "in Thread :: " + Thread.currentThread().name)
            }

            override fun onComplete() {
                Log.e("DDVH", "All items are emitted! in Thread :: " + Thread.currentThread().name)
            }
        }
    }

    private fun getFootballPlayesObservable(): Observable<String> {
        return Observable.just("Messi", "Ronaldo", "Modric", "Salah", "Mbappe")
    }

    private fun prepareNotes(): List<Note> {
        val notes: MutableList<Note> = ArrayList<Note>()
        notes.add(Note(1, "buy tooth paste!"))
        notes.add(Note(2, "call brother!"))
        notes.add(Note(3, "watch narcos tonight!"))
        notes.add(Note(4, "pay power bill!"))
        return notes
    }

    private fun getNotesObservable(): Observable<Note> {
        val notes: List<Note> = prepareNotes()
        return Observable.create {
            fun subscribe(emitter: ObservableEmitter<Note?>) {
                Log.e("DDVH", "subscribe in Thread :: " + Thread.currentThread().name)
                for (note in notes) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(note)
                    }
                }
                if (!emitter.isDisposed) {
                    emitter.onComplete()
                }
            }
        }
    }

    private fun getNotesObserver(): DisposableObserver<Note> {
        return object : DisposableObserver<Note>() {
            override fun onNext(note: Note) {
                Log.e("DDVH", "Note: " + note.note + "==== Thread : " + Thread.currentThread().name)
            }

            override fun onError(e: Throwable) {
                Log.e("DDVH", "onError: " + e.message + "==== Thread : " + Thread.currentThread().name)
            }

            override fun onComplete() {
                Log.e("DDVH", "All notes are emitted!" + "==== Thread : " + Thread.currentThread().name)
            }
        }
    }
}