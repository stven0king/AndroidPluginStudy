package com.tzx.amsdemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.tzx.ams.TrackMethod;
import com.tzx.ams_test_jar.AmsTestJar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ExecutorService mExecutor = Executors.newFixedThreadPool(10);
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (int i = 0; i < 10; i++) {
            mExecutor.execute(this::test);
        }
    }

    //@TrackMethod(tag = "TIME")
    public void test() {
        AmsTestJar.test();
        Log.d("tanzhenxing", "test");
    }
}
