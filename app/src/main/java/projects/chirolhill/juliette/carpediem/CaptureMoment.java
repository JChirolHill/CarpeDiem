package projects.chirolhill.juliette.carpediem;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import projects.chirolhill.juliette.carpediem.model.Database;
import projects.chirolhill.juliette.carpediem.model.DatabaseMoment;
import projects.chirolhill.juliette.carpediem.model.Moment;

public class CaptureMoment extends AppCompatActivity {
    private static String TAG = "CaptureMomentTag";

    private Button btnCapture;
//    private Button btnSubmit;
    private ImageView imgCapture;
    private ImageView imgSaveMoment;
    private EditText editCaption;
    private String userID;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap momentBitmap;
    private Moment moment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_moment);

        btnCapture = findViewById(R.id.btnCaptureMoment);
//        btnSubmit = findViewById(R.id.btnSubmit);
        editCaption = findViewById(R.id.editAddCaption);
        imgCapture = findViewById(R.id.imgCaptureMoment);
        imgSaveMoment = findViewById(R.id.imgSaveMoment);

        SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        userID = prefs.getString(LauncherActivity.PREF_USER_ID, "INVALID USER ID");

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        imgSaveMoment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                moment =  new Moment(editCaption.getText().toString(), dateFormat.format(new Date()));

                // upload pictures of verification docs to database
                Database.getInstance().setCallback(new Database.Callback() {
                    @Override
                    public void dbCallback(Object o) {
                        if(o.equals(Database.SUCCESS)) {
                            // create the moment in database
                            String addMomentResult = Database.getInstance().addMoment(userID, new DatabaseMoment(moment));
                            if(addMomentResult != null) {
//                                textError.setText(addShopResult);
//                                textError.setVisibility(View.VISIBLE);
                            }
                            startActivity(new Intent(getApplicationContext(), ViewMoments.class));
                        }
                        else if(o.equals(Database.FAIL)) {
//                            textError.setText(R.string.failedImgUpload);
//                            textError.setVisibility(View.VISIBLE);
                        }
                    }
                });
                Database.getInstance().uploadImages(userID, moment.getDate(), momentBitmap);
            }
        });

//        btnSubmit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // find current date
//                DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
//                moment =  new Moment(editCaption.getText().toString(), dateFormat.format(new Date()));
//
//                // upload pictures of verification docs to database
//                Database.getInstance().setCallback(new Database.Callback() {
//                    @Override
//                    public void dbCallback(Object o) {
//                        if(o.equals(Database.SUCCESS)) {
//                            // create the moment in database
//                            String addMomentResult = Database.getInstance().addMoment(userID, new DatabaseMoment(moment));
//                            if(addMomentResult != null) {
////                                textError.setText(addShopResult);
////                                textError.setVisibility(View.VISIBLE);
//                            }
//                            startActivity(new Intent(getApplicationContext(), ViewMoments.class));
//                        }
//                        else if(o.equals(Database.FAIL)) {
////                            textError.setText(R.string.failedImgUpload);
////                            textError.setVisibility(View.VISIBLE);
//                        }
//                    }
//                });
//                Database.getInstance().uploadImages(userID, moment.getDate(), momentBitmap);
//            }
//        });
    }

    // create intent to use another app to take picture
    private void dispatchTakePictureIntent() {
        // check user granted permission
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 4);
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            btnCapture.setVisibility(View.GONE);
            imgCapture.setVisibility(View.VISIBLE);
            imgSaveMoment.setVisibility(View.VISIBLE);
//            btnSubmit.setEnabled(true);

            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgCapture.setImageBitmap(imageBitmap);
            momentBitmap = imageBitmap;
        }
    }
}
