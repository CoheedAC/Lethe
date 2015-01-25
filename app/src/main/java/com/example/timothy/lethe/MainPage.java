package com.example.timothy.lethe;

import android.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.provider.MediaStore;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class MainPage extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void takePic(View view) {
        final int IMAGE_CAPTURE = 102;

        android.content.Intent intent = new android.content.Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 102);
    }
    public void goToPhotos(View view){
        Intent i = new Intent(getApplicationContext(), PhotoViewer.class);
        startActivity(i);
    }
    public void enlargePhoto(View view){
        ImageView image = (ImageView) findViewById(R.id.imageView2);
        image.requestLayout();
        image.getLayoutParams().height = 500;
        image.getLayoutParams().width = 500;
        image.requestLayout();
        //image.getLayoutParams().height += 40;
        //image.getLayoutParams().width += 70;
    }
}
