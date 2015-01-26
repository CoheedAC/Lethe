package cs48.com.snapyak;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;


public class CameraActivity extends ActionBarActivity {

    private Bundle mExtras;
    private Bitmap mImageBitmap;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Intent intent = getIntent();
        mExtras = intent.getBundleExtra("imageBundle");
        mImageBitmap = (Bitmap) mExtras.get("data");
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setImageBitmap(mImageBitmap);
    }

}
