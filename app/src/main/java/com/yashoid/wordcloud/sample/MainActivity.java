package com.yashoid.wordcloud.sample;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import com.yashoid.wordcloud.WordCloud;

public class MainActivity extends AppCompatActivity implements WordCloud.OnWordClickListener {

    private WordCloud mWordCloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWordCloud = (WordCloud) findViewById(R.id.wordcloud);

        mWordCloud.setAdapter(new SampleWordAdapter());

        mWordCloud.setOnWordClickListener(this);
    }

    @Override
    public void onWordClicked(int position, WordCloud.Word word) {
        Rect rect = new Rect();
        mWordCloud.getGlobalVisibleRect(rect);

        int x = (int) (rect.left + word.getX());
        int y = (int) (rect.top + word.getY());

        Toast toast = Toast.makeText(this, word.getText(), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.LEFT | Gravity.TOP, x, y);
        toast.show();
    }

}
