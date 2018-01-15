package com.procatdt.appbluetooth;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

// 2017-11-03 This is the latest attempt to print to the bluetooth printer.  So far, it is able to print out a full label.
// This is now able to print out the full label perfectly including the signature image.

public class PrintActivity extends AppCompatActivity {
    String mac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);
        Timber.d("**************** Select Printer Activity *****************");
        mac = getIntent().getStringExtra("printer")==null ? "" : getIntent().getStringExtra("printer");
    }

    public void onPrintTestClick(View view) {
        // 11-14-2017:  WORKING!!! This uses RxJava to call the method within PrinterManager2 as an Observable.  Which is subscribed to from
        // this activity.  Once the observable is executed and completes, the OnNext and onComplete methods kickoff.
        // I could have done this with Lambdas, but then I'd have to use Java 8.
        PrinterManager.printTest(mac, createTestLine())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        Toast.makeText(PrintActivity.this, s, Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError: RxJava return " + e);
                    }
                    @Override
                    public void onComplete() {
                        Timber.d("onComplete: printing completed.");
                    }
                });
    }


    private String createTestLine() {
        return "^XA^LL360^POI^FO20,20^A0N,25,25^FDTest of the RxJava printing on " + Helper.getCurrDateTime() + "^FS^XZ";
    }

    public void onPrintFullLabelClick(View view) {
        PrinterManager.printFullLabel(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        Toast.makeText(PrintActivity.this, s, Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError: RxJava return " + e);
                    }
                    @Override
                    public void onComplete() {
                        Timber.d("onComplete: printing completed.");
                    }
                });
    }
}
