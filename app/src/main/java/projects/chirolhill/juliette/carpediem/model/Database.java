package projects.chirolhill.juliette.carpediem.model;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Database {
    /**
     * Callback interface
     * does all the work you want to do with the object returned from the database
     */
    public interface Callback {
        void dbCallback(Object o);
    }

    private Callback cb;

    public void setCallback(Callback cb) {
        this.cb = cb;
    }

    /**
     * Database class
     * getUser fetches the user at a particular uID
     * addUser can ADD a user, UPDATE a user, and DELETE a user (pass in null)
     */
    public static final String MOMENTS = "moments";
    private static final String TAG = Database.class.getSimpleName();

    public static final String FAIL = "fail";
    public static final String SUCCESS = "success";

    private static final Database ourInstance = new Database();

    private FirebaseDatabase firebase;
    private DatabaseReference dbMomentsRef;
//    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    public static Database getInstance() {
        return ourInstance;
    }

    //  initialize all the references
    private Database() {
        firebase = FirebaseDatabase.getInstance();
        dbMomentsRef = firebase.getReference(MOMENTS);
    }

    // returns error message, null if no problems
    // if pass in null shop, will delete the item in database
    public String addMoment(String userID, DatabaseMoment m) {
        try {
            String key = dbMomentsRef.child(userID).push().getKey();
            dbMomentsRef.child(userID).child(key).setValue(m);
        } catch(DatabaseException de) {
            Log.d(TAG, de.getMessage());
            return de.getMessage();
        }
        return null;
    }

    // returns user if exists, null if does not exist
    public void getMoments(String id) {
        Log.d(TAG, id);
        dbMomentsRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Moment> moments = new ArrayList<>();
                if(dataSnapshot.getValue() != null) { // exists in DB
                    moments = new ArrayList<>();
                    Iterable<DataSnapshot> momentSnapshots = dataSnapshot.getChildren();
                    for(DataSnapshot mSnapshot : momentSnapshots) {
                        moments.add((Moment)(mSnapshot.getValue(DatabaseMoment.class).revertToOriginal()));
                    }
                }
                cb.dbCallback(moments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }

    // uploads images to firestore
    // returns null if no errors, else returns error message
    public void uploadImages(String ownerID, String date, Bitmap moment) {
        // Get the data from an ImageView as bytes
        final StorageReference documentRef = storageRef.child(ownerID + "/" + date + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        moment.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = documentRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                cb.dbCallback(FAIL);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                cb.dbCallback(SUCCESS);
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        cb.dbCallback(uri.toString());
//                    }
//                });
            }
        });
    }

    public void getImage(String ownerID, String date) {
        final String finDate = date;
        final StorageReference documentRef = storageRef.child(ownerID + "/" + date + ".jpg");
        documentRef.getBytes(50000).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                cb.dbCallback(new Pair<>(finDate, bitmap));
            }
        });
    }
}
