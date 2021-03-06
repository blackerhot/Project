package th.ac.kku.asayaporn.project;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.graphics.Color.parseColor;

public class ItemActivity extends AppCompatActivity {
    public boolean create;
    Button butAddEvent;
    TextView address;
    TextView datest;
    TextView dateend;
    TextView detail;
    ImageView pic;
    TextView title;
    ImageButton phone;
    ImageButton website;
    TextView sponser;
    DatabaseReference myRef2;
    FloatingActionButton floatingActionButton;
    com.google.api.services.calendar.Calendar mService;
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_AUTHORIZATION = 1001;
    GoogleAccountCredential credentialCaledndar;
    GoogleSignInAccount googleSignInAccount;
    Bundle para;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    Intent intentother = null;
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    ArrayList<ExampleItem> mExampleList = new ArrayList<ExampleItem>();
    ;
    String timestr, timeEndstr;

    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activites_kkudialog);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatAlert);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pic = (ImageView) findViewById(R.id.post_pic);
        title = (TextView) findViewById(R.id.post_title);
        detail = (TextView) findViewById(R.id.post_detail);
        datest = (TextView) findViewById(R.id.post_date);
        dateend = (TextView) findViewById(R.id.post_dateEnd);
        phone = (ImageButton) findViewById(R.id.post_phone);
        website = (ImageButton) findViewById(R.id.post_website);
        sponser = (TextView) findViewById(R.id.post_sponser);
        address = (TextView) findViewById(R.id.post_address);
        butAddEvent = (Button) findViewById(R.id.butAddEvent);
        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        para = getIntent().getExtras();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user");
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collap);
        getSupportActionBar().setTitle(para.getString("title"));

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        Picasso.get().load(para.getString("img")).resize(width, 0).
                error(R.drawable.imgnotfound).into(pic);


        title.setText(para.getString("title"));
        getSupportActionBar().setTitle("");

        //website.setText(para.getString("website"));
        sponser.setText(para.getString("sponsor"));
        detail.setText(para.getString("detail"));
        datest.setText("วันที่เริ่มงาน : " + para.getString("datest") + "\nเวลา : (" + para.getString("timest") + ")");
        dateend.setText("วันสุดท้าย : " + para.getString("dateend") + "\nเวลา : (" + para.getString("timeed") + ")");
        //phone.setText(para.getString("phone"));
        address.setText(para.getString("address"));


        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentFirebaseUser != null) {
            myRef2 = database.getReference().child("user").child(currentFirebaseUser.getUid()).child("Activities_me");
        }

        if (currentFirebaseUser != null) {

            for (UserInfo userInfo : currentFirebaseUser.getProviderData()) {

                if (userInfo.getProviderId().equals("google.com")) {
                    credentialCaledndar = GoogleAccountCredential.usingOAuth2(
                            getApplicationContext(), Arrays.asList(SCOPES))
                            .setBackOff(new ExponentialBackOff())
                            .setSelectedAccount(googleSignInAccount.getAccount());
                    mService = new com.google.api.services.calendar.Calendar.Builder(
                            transport, jsonFactory, credentialCaledndar)
                            .setApplicationName("Google Calendar API Android Quickstart")
                            .build();
                    create = false;
                    new ApiAsyncTask(ItemActivity.this).execute();
                }
            }
        }

        butAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (currentFirebaseUser != null) {

                    for (UserInfo userInfo : currentFirebaseUser.getProviderData()) {

                        if (userInfo.getProviderId().equals("google.com")) {
                            create = true;
                            new ApiAsyncTask(ItemActivity.this).execute();
                        } else {

                        }
                    }
                } else {
                    Toast.makeText(ItemActivity.this, "Please using google account", Toast.LENGTH_SHORT).show();
                }
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(currentFirebaseUser!=null){


                timestr = para.getString("timest");
                timestr = timestr.substring(4);
                timeEndstr = para.getString("timeed");
                timeEndstr = timeEndstr.substring(4);
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/" + currentFirebaseUser.getUid() + "/Activities_me/" + EncodeString(para.getString("title")) + "/title", EncodeString(para.getString("title")));
                childUpdates.put("/" + currentFirebaseUser.getUid() + "/Activities_me/" + EncodeString(para.getString("title")) + "/timestart", timestr);
                childUpdates.put("/" + currentFirebaseUser.getUid() + "/Activities_me/" + EncodeString(para.getString("title")) + "/timeend", timeEndstr);
                childUpdates.put("/" + currentFirebaseUser.getUid() + "/Activities_me/" + EncodeString(para.getString("title")) + "/datest", para.getString("datest"));
                childUpdates.put("/" + currentFirebaseUser.getUid() + "/Activities_me/" + EncodeString(para.getString("title")) + "/dateend", para.getString("dateend"));
                childUpdates.put("/" + currentFirebaseUser.getUid() + "/Activities_me/" + EncodeString(para.getString("title")) + "/detail", EncodeString(para.getString("detail")));
                childUpdates.put("/" + currentFirebaseUser.getUid() + "/Activities_me/" + EncodeString(para.getString("title")) + "/address", para.getString("address"));
                myRef.updateChildren(childUpdates);
                Toast.makeText(ItemActivity.this,"Sending to my Activities!!",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ItemActivity.this, "Please using google account", Toast.LENGTH_SHORT).show();
                }
            }
        });
        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentother = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(para.getString("website")));
                startActivity(intentother);
            }
        });
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentother = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("tel:" + para.getString("phone")));
                startActivity(intentother);
            }
        });


    }

    public static String EncodeString(String string) {
        return string.replace(".", " ");

    }




    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode,
                        ItemActivity.this,
                        REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
