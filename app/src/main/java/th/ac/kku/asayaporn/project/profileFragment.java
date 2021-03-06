package th.ac.kku.asayaporn.project;

import android.app.Activity;
import android.app.MediaRouteButton;
import android.app.assist.AssistContent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class profileFragment extends Fragment {

    String email = "";
    String uid = "";
    String disname = "";
    String url_photo = "";
    FirebaseUser currentFirebaseUser;
    private FirebaseAuth mAuth;

    TextView waiting;
    TextView already;
    TextView emailtv;
    TextView waitingmod;
    FirebaseDatabase database;
    DatabaseReference myRef;

    ImageButton waitinBut;
    ImageButton acceptBut;
    DatabaseReference myRefActi;
    TextView tv_name_user;
    SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle saveInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        getActivity().setTitle("Management");
        ((AppCompatActivity) getActivity()).getSupportActionBar().
                setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        emailtv = (TextView) view.findViewById(R.id.emailTv);

        waiting = (TextView) view.findViewById(R.id.waitingTv);
        already = (TextView) view.findViewById(R.id.alreadyAccTv);
        waitingmod = (TextView) view.findViewById(R.id.activitesTv);
        waitinBut = (ImageButton) view.findViewById(R.id.waitingBut);
        acceptBut = (ImageButton) view.findViewById(R.id.acceptedBut);
        final TextView statusTv = (TextView) view.findViewById(R.id.statusTv);
        Button btn = (Button) view.findViewById(R.id.logoutBut);
        Button adminBut = (Button) view.findViewById(R.id.adminBut);
        Button manageBut = (Button) view.findViewById(R.id.userBut);
        tv_name_user = (TextView) view.findViewById(R.id.tv_name_user);
        CircleImageView user_photo = (CircleImageView) view.findViewById(R.id.user_photo_id);
        final LinearLayout layoutmanage = (LinearLayout) view.findViewById(R.id.layoutmanage);
        final LinearLayout layoutadmon = (LinearLayout) view.findViewById(R.id.layoutadmin);
        mAuth = FirebaseAuth.getInstance();
        currentFirebaseUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user");
        myRefActi = database.getReference("activities");
        layoutadmon.setVisibility(View.GONE);
        layoutmanage.setVisibility(View.GONE);
        myRef.addValueEventListener(new ValueEventListener() {
            private String TAG = "TAGGGG";

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (currentFirebaseUser != null) {


                    for (DataSnapshot data : dataSnapshot.getChildren()) {

                        if (data.child("email").getValue() != null) {
                            if (data.child("email").getValue().toString()
                                    .equals(currentFirebaseUser.getEmail())) {

                                if (data.child("classes").getValue() != null) {
                                    if (data.child("classes").getValue().toString().equals("admin")) {
                                        statusTv.setText("Class :: Administrator");

                                        layoutmanage.setVisibility(View.VISIBLE);
                                        layoutadmon.setVisibility(View.VISIBLE);
                                    } else if (data.child("classes").getValue().toString().equals("mod")) {
                                        statusTv.setText("Class :: Moderator");
                                        layoutadmon.setVisibility(View.VISIBLE);
                                        layoutmanage.setVisibility(View.GONE);
                                    } else {

                                        statusTv.setText("Class :: User");
                                        layoutadmon.setVisibility(View.GONE);
                                        layoutmanage.setVisibility(View.GONE);
                                    }
                                } else {
                                    statusTv.setText("Class :: User");
                                    layoutadmon.setVisibility(View.GONE);
                                    layoutmanage.setVisibility(View.GONE);

                                }

                            }
                        } else {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        waitinBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MyActivities.class);

                intent.putExtra("email", currentFirebaseUser.getEmail() + "");
                v.getContext().startActivity(intent);

            }
        });
        acceptBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ShowAcceptActivites.class);

                intent.putExtra("email", currentFirebaseUser.getEmail() + "");
                v.getContext().startActivity(intent);

            }
        });

        myRefActi.addValueEventListener(new ValueEventListener() {
            private String TAG = "TAGGGG";

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                int n = 0;
                int waitingnum = 0;
                int pass = 0;
                String email = "";
                if (currentFirebaseUser != null) {
                    email = currentFirebaseUser.getEmail();
                }
                if (dataSnapshot.getValue() != null) {


                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if ((child.child("by").getValue() + "").equals(email)) {
                            if (child.child("status").getValue().equals("pending")) {
                                waitingnum++;
                            }
                            if (child.child("status").getValue().equals("true")) {
                                pass++;
                            }

                        }
                        if (child.child("status").getValue().equals("pending")) {
                            n++;
                        }

                    }
                    waiting.setText(waitingnum + "");
                    already.setText(pass + "");
                    waitingmod.setText("Awaiting to Accept : " + n + " Now!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });

        if (currentFirebaseUser == null) {
            view = inflater.inflate(R.layout.activity_request_login, container, false);
            Button btn_login_re = (Button) view.findViewById(R.id.btn_requ_login_id);
            btn_login_re.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), Login.class));
                }
            });
            return view;
        }
        try {
            if (currentFirebaseUser.getDisplayName() == null) {
                email = currentFirebaseUser.getEmail();
                uid = currentFirebaseUser.getUid();
                disname = "Normal person";
            } else {

                url_photo = currentFirebaseUser.getPhotoUrl().toString();
                disname = currentFirebaseUser.getDisplayName();
                email = currentFirebaseUser.getEmail();
                uid = currentFirebaseUser.getUid();
                if (url_photo.equals("")) {

                } else {
                    Uri photoUrl = Uri.parse(url_photo);

                    // Variable holding the original String portion of the url that will be replaced
                    String originalPieceOfUrl = "s96-c/photo.jpg";

                    // Variable holding the new String portion of the url that does the replacing, to improve image quality
                    String newPieceOfUrlToAdd = "s400-c/photo.jpg";

                    // Check if the Url path is null
                    if (photoUrl != null) {

                        // Convert the Url to a String and store into a variable
                        String photoPath = photoUrl.toString();

                        // Replace the original part of the Url with the new part
                        String newString = photoPath.replace(originalPieceOfUrl, newPieceOfUrlToAdd);

                        // Load user's profile photo from their signed-in provider into the image view (with newly edited Url for resolution improvement)

                        if(FacebookAuthProvider.PROVIDER_ID.equals(currentFirebaseUser.getProviderId())) {
                            String photoUrl1 = "http://graph.facebook.com/"+currentFirebaseUser.getProviderId()+"/picture?type=large";
                            Picasso.get().load(photoUrl1).fit().into(user_photo);

                        }else {
                            Picasso.get().load(newString).fit().into(user_photo);
                        }
                    } // End if

                }
            }
        } catch (Exception e) {
            email = currentFirebaseUser.getEmail();
            uid = currentFirebaseUser.getUid();
            disname = "Normal person";
        }


        sharedPreferences = this.getActivity().getSharedPreferences("shared preferences", MODE_PRIVATE);
        tv_name_user.setText(disname);
        emailtv.setText(email);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FacebookSdk.setApplicationId("302892413797138");
                FacebookSdk.sdkInitialize(getContext());
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                AccessToken.setCurrentAccessToken(null);//logout facebook
                // settings.edit().remove("LOGIN").commit();


                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.clear().commit();

                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);

            }
        });

        adminBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AdminActivity.class));
            }
        });
        manageBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), UserActivity.class));
            }
        });



        return view;


    }


}

