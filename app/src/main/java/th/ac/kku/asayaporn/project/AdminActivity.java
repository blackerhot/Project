package th.ac.kku.asayaporn.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.List;

public class AdminActivity extends AppCompatActivity {

    protected ListView mListView;
    protected CustomAdapter2 mAdapter;

    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activites);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Manage Activities");
        }
        mListView = (ListView) findViewById(R.id.feedlistview);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("activities");


        myRef.addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                            mAdapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                                            mAdapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                myRef.addValueEventListener(new ValueEventListener() {
                    private String TAG = "TAGGGG";

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // This method is called once with the initial value and again
                        // whenever data at this location is updated.
                        String s0 = "";
                        Gson gson = new Gson();

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            //Here you can access the child.getKey()
                            ActivityKKU user = child.getValue(ActivityKKU.class);

                            s0 += gson.toJson(user.toMap()) + ",";
                        }
                        if (s0.length() != 0) {
                            s0 = s0.substring(0, s0.length() - 1);
                        }

                        SharedPreferences sp;
                        SharedPreferences.Editor editor;
                        sp = AdminActivity.this.getSharedPreferences("USER", Context.MODE_PRIVATE);
                        editor = sp.edit();
                        editor.putString("jsonByUSER", new String(String.valueOf("{\"activities\":[" + s0 + "]}")));
                        editor.commit();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value.", error.toException());
                    }
                });

        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = AdminActivity.this.getSharedPreferences("USER", Context.MODE_PRIVATE);
        editor = sp.edit();
        String result1 = sp.getString("jsonByUSER", "");
        editor.commit();
        String fromeall = "{\"activities\":[" + handlerData1(result1) + "]}";

        showData(fromeall);



        final Intent intent = new Intent(AdminActivity.this, ItemActivity2.class);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ActivityKKU mActivite = (ActivityKKU) parent.getItemAtPosition(position);


                intent.putExtra("img", String.valueOf(mActivite.image));
                intent.putExtra("title", String.valueOf(mActivite.title));
                intent.putExtra("address", String.valueOf(mActivite.place));
                intent.putExtra("detail", String.valueOf(mActivite.content));
                intent.putExtra("datest", String.valueOf(mActivite.dateSt));
                intent.putExtra("dateend", String.valueOf(mActivite.dateEd));
                intent.putExtra("website", String.valueOf(mActivite.url));
                intent.putExtra("sponsor", String.valueOf(mActivite.sponsor));
                intent.putExtra("timest", String.valueOf(mActivite.timeSt));
                intent.putExtra("timeed", String.valueOf(mActivite.timeEd));
                if (mActivite.phone == null) {
                    intent.putExtra("phone", String.valueOf(mActivite.getContact().get("phone")));
                } else {
                    intent.putExtra("phone", String.valueOf(mActivite.phone));
                }
                startActivity(intent);


            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

    private void showData(String json) {

        Gson gson = new Gson();
        Blog blog = gson.fromJson(json, Blog.class);
        List<ActivityKKU> activity = blog.getActivities();
        mAdapter = new CustomAdapter2(AdminActivity.this, activity);
        mListView.setAdapter(mAdapter);

    }


    private String handlerData1(String json) {

        String string = "";
        Gson gson = new Gson();

        Blog blog = gson.fromJson(json, Blog.class);
        List<ActivityKKU> activity = blog.getActivities();

        for (int i = 0; i < activity.size(); i++) {
            string += gson.toJson(activity.get(i).toMap()) + ",";

        }
        if (string.length() == 0) {
            return "";
        }
        string = string.substring(0, string.length() - 1);
        return string;

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
