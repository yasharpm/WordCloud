package com.yashoid.wordcloud.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.yashoid.wordcloud.WordCloud;

public class MainActivity extends AppCompatActivity {

    private WordCloud mWordCloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWordCloud = (WordCloud) findViewById(R.id.wordcloud);

        mWordCloud.setAdapter(new SampleWordAdapter());
    }

}
