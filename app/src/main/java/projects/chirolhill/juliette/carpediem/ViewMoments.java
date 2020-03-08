package projects.chirolhill.juliette.carpediem;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import projects.chirolhill.juliette.carpediem.model.Database;
import projects.chirolhill.juliette.carpediem.model.Moment;

public class ViewMoments extends AppCompatActivity {
    private static String TAG = "ViewMomentsTag";
    private ListView listMoments;

    private MomentListAdapter momentAdapter;
    private List<Moment> moments;
    private String userID;
    private Iterator<Moment> itMoment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_moments);

        listMoments = findViewById(R.id.list);
        moments = new ArrayList<>();

        momentAdapter = new MomentListAdapter(this, R.layout.list_item_moment, moments); // put in the XML custom row we created
        listMoments.setAdapter(momentAdapter);

        SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        this.userID = prefs.getString(LauncherActivity.PREF_USER_ID, "Invalid ID");

        // load in moments from database for this user
        Database.getInstance().setCallback(new Database.Callback() {
            @Override
            public void dbCallback(Object o) {
                final List<Moment> momentsDB = (ArrayList<Moment>)o;

                for(Moment m : momentsDB) {
                    momentAdapter.add(m);
                    momentAdapter.notifyDataSetChanged();
//                    Database.getInstance().setCallback(new Database.Callback() {
//                        @Override
//                        public void dbCallback(Object o) {
//                            Pair<String, Bitmap> pair = (Pair<String, Bitmap>)o;
//
//                            // find the appropriate moment to add this image to
//                            for(Moment m : moments) {
//                                if(m.getDate().equals(pair.first)) {
//                                    m.setImg(pair.second);
//                                    break;
//                                }
//                            }
//                        }
//                    });
//                    Database.getInstance().getImages(userID, moments);
                }

                itMoment = moments.iterator();
                recursiveImages();
            }
        });
        Database.getInstance().getMoments(userID);
    }

    private void recursiveImages() {
        if(itMoment.hasNext()) {
            Log.d(TAG,"called recursive iamges");
            Database.getInstance().setCallback(new Database.Callback() {
                @Override
                public void dbCallback(Object o) {
                    Pair<String, Bitmap> pair = (Pair<String, Bitmap>)o;

                    // find the appropriate moment to add this image to
                    for(Moment m : moments) {
                        if(m.getDate().equals(pair.first)) {
                            m.setImg(pair.second);
                            momentAdapter.notifyDataSetChanged();
                            break;
                        }
                    }

                    // recurse
                    recursiveImages();
                }
            });
            Database.getInstance().getImage(userID, itMoment.next().getDate());
        }
    }

    private class MomentListAdapter extends ArrayAdapter<Moment> {
        public MomentListAdapter(Context context, int resource, List<Moment> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_moment, null);
            }

            TextView textCaption = convertView.findViewById(R.id.listMomentCaption);
            TextView textDate = convertView.findViewById(R.id.listMomentDate);
            ImageView image = convertView.findViewById(R.id.listMomentImage);

            Moment m = getItem(position);

            // copy/map the data from the current item (model) to the curr row (view)
            textCaption.setText(m.getTitle());
            textDate.setText(m.getDate());
            image.setImageBitmap(m.getImg());
//            Picasso.get().load(m.getImgUrl()).into(image);

            return convertView;
        }
    }
}
