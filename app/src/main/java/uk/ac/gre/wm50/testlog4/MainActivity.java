package uk.ac.gre.wm50.testlog4;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERM_CODE = 101;
    private static final int REQUEST_CODE_CAMERA = 102;

    ImageView imageView;
    EditText addLink_txt;
    Button backward_button, forward_button, add_link_button, clear_link_button;
    ImageButton cameraButton;
    String ImagePath;
    int index;
    ImageDatabase db;
    String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        backward_button = findViewById(R.id.backward_button);
        forward_button = findViewById(R.id.forward_button);
        add_link_button = findViewById(R.id.add_link_button);
        addLink_txt = findViewById(R.id.addLink_txt);
        cameraButton = findViewById(R.id.cameraButton);

        add_link_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ValidUrl(addLink_txt.getText().toString().trim())){
                    ImageDatabase db = new ImageDatabase(MainActivity.this);
                    db.addLink(addLink_txt.getText().toString().trim());


                    Glide.with(getApplicationContext())
                            .load(addLink_txt.getText().toString().trim())
                            .placeholder(R.drawable.ic_baseline_image_24).into(imageView);
                    Toast.makeText(MainActivity.this, "Add Successfully!!!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "URL not Valid", Toast.LENGTH_SHORT).show();
                }

            }
        });
        clear_link_button = findViewById(R.id.clear_link_button);

        clear_link_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addLink_txt.setText("");

            }
        });

        db = new ImageDatabase(MainActivity.this);

        db.addLink("https://img.thuthuatphanmem.vn/uploads/2018/09/28/anh-rong-cuc-dep-2_024751600.jpg");
        db.addLink("https://img.thuthuatphanmem.vn/uploads/2018/09/28/anh-rong-cuc-dep_024751615.jpg");
        db.addLink("https://img.thuthuatphanmem.vn/uploads/2018/09/28/dragon-image_024751694.jpg");

        Glide.with(getApplicationContext())
                .load(loadLastImg())
                .placeholder(R.drawable.ic_baseline_image_24).into(imageView);


        backward_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(getApplicationContext())
                        .load(backward_button())
                        .placeholder(R.drawable.ic_baseline_image_24).into(imageView);
            }
        });

        forward_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(getApplicationContext())
                        .load(forward_button())
                        .placeholder(R.drawable.ic_baseline_image_24).into(imageView);
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPermission();
            }
        });
    }

    private void CameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            dispatchPicture ();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA) {
            if(resultCode == Activity.RESULT_OK) {
                File file = new File(ImagePath);
                imageView.setImageURI(Uri.fromFile(file));
                Log.d("tag", "Absolute URL is " + Uri.fromFile(file));
            }
        }
    }


    private File createFileImage() throws IOException{
        //create name of file's image
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageNameFile = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageNameFile,
                ".jpg",
                storageDir
        );
        ImagePath = image.getAbsolutePath();
        db.addLink(ImagePath);
        return image;
    }

    private void dispatchPicture (){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createFileImage();
            } catch (IOException exc){

            }
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchPicture();
            } else {
                Toast.makeText(this, "Camera Permission is Required to Use camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    String loadLastImg(){
        ImageDatabase db = new ImageDatabase(MainActivity.this);
        Cursor cursor = db.getAllLink();

        cursor.moveToLast();
        url = cursor.getString(1);
        index = cursor.getPosition();
        return url;
    }

    String forward_button(){
        ImageDatabase db = new ImageDatabase(MainActivity.this);
        Cursor cursor = db.getAllLink();
        cursor.moveToLast();
        int last = cursor.getPosition();

        if(index == last){
            cursor.moveToFirst();
            index = cursor.getPosition();

        } else {
            index++;
            cursor.moveToPosition(index);

        }
        url = cursor.getString(1);
        return url;
    }

    String backward_button(){
        ImageDatabase db = new ImageDatabase(MainActivity.this);
        Cursor cursor = db.getAllLink();


        if(index == 0){
            cursor.moveToLast();
            index = cursor.getPosition();
        } else {
            index--;
            cursor.moveToPosition(index);
        }
        url = cursor.getString(1);
        return url;
    }

    public static boolean ValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
        } catch (MalformedURLException ignored) {
        }
        return false;
    }

}