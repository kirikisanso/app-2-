package com.landtanin.teacherattendancemonitoring.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.landtanin.teacherattendancemonitoring.R;
import com.landtanin.teacherattendancemonitoring.dao.LecturerModuleCollectionDao;
import com.landtanin.teacherattendancemonitoring.dao.LecturerModuleDao;
import com.landtanin.teacherattendancemonitoring.databinding.ActivityMainBinding;
import com.landtanin.teacherattendancemonitoring.fragment.MainFragment;
import com.landtanin.teacherattendancemonitoring.manager.HttpManager;
import com.landtanin.teacherattendancemonitoring.manager.http.ApiService;
import com.landtanin.teacherattendancemonitoring.util.Utils;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding b;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initInstance();

        if (savedInstanceState==null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.contentContainer,
                            MainFragment.newInstance(),
                            "MainFragment")
                    .commit();
        }

    }

    private void initInstance() {

        SharedPreferences prefs = this.getSharedPreferences("login_state", Context.MODE_PRIVATE);
        String userName = prefs.getString("name", null);
        setSupportActionBar(b.mainActivityToolbar);
        b.txtToolbarMainActivity.setText("Hi, " + userName);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_logout) {

//            realm = Realm.getDefaultInstance();
//            RealmConfiguration realmConfig = realm.getConfiguration();
//            Realm.deleteRealm(realmConfig);

            SharedPreferences prefs = this.getSharedPreferences("login_state", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            // Add/Edit/Delete
            editor.remove("login_state_var");
            editor.remove("name");
            editor.remove("lecturer_id");
            editor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();


        }

        else if (item.getItemId() == R.id.action_refresh) {

            dialog = new ProgressDialog(this);
            dialog.setMessage("Refreshing...");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            SharedPreferences prefs = this.getSharedPreferences("login_state", Context.MODE_PRIVATE);
            int lecturer_id = prefs.getInt("lecturer_id", 0);
            ApiService apiService = HttpManager.getInstance().create(ApiService.class);
//        apiService.loadStudentModule(Authorization,Content_Type,developer.getMemberID(),TopicId)
            apiService.loadLecturerModule(lecturer_id)
                    .asObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Utils.getInstance().defaultSubscribeScheduler())
                    .unsubscribeOn(Utils.getInstance().defaultSubscribeScheduler())
                    .subscribe(new Action1<LecturerModuleCollectionDao>() {
                        @Override
                        public void call(LecturerModuleCollectionDao response) {

                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
//                        realm.deleteAll(); // clear the current data before load new data
                            realm.delete(LecturerModuleDao.class); // delete only data of a specific class
                            realm.copyToRealmOrUpdate(response.getData());
                            realm.commitTransaction();
                            dialog.dismiss();
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                            Log.d("MainActivity refresh getLecturer", "call success");

                        }

                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            dialog.dismiss();
                            Utils.getInstance().onHoneyToast("MainActivity refresh Lecturer"+throwable.getLocalizedMessage());

                        }
                    });

        }

        return super.onOptionsItemSelected(item);
    }
}
