package com.landtanin.teacherattendancemonitoring.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.landtanin.teacherattendancemonitoring.R;
import com.landtanin.teacherattendancemonitoring.activity.MainActivity;
import com.landtanin.teacherattendancemonitoring.dao.LecturerModuleCollectionDao;
import com.landtanin.teacherattendancemonitoring.dao.LecturerModuleDao;
import com.landtanin.teacherattendancemonitoring.dao.User;
import com.landtanin.teacherattendancemonitoring.databinding.FragmentLoginBinding;
import com.landtanin.teacherattendancemonitoring.manager.HttpManager;
import com.landtanin.teacherattendancemonitoring.manager.http.ApiService;
import com.landtanin.teacherattendancemonitoring.util.Utils;

import io.realm.Realm;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nuuneoi on 11/16/2014.
 */
public class FragmentLogin extends Fragment {

    FragmentLoginBinding b;
    ProgressDialog dialog;
    String email, password;
    private static final String tag = "login";

    public FragmentLogin() {
        super();
    }

    public static FragmentLogin newInstance() {
        FragmentLogin fragment = new FragmentLogin();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init(savedInstanceState);

        if (savedInstanceState != null)
            onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        View rootView = b.getRoot();
        initInstances(rootView, savedInstanceState);
        return rootView;
    }

    @SuppressWarnings("UnusedParameters")
    private void init(Bundle savedInstanceState) {
        // Init Fragment level's variable(s) here
    }

    @SuppressWarnings("UnusedParameters")
    private void initInstances(View rootView, Bundle savedInstanceState) {
        // Init 'View' instance(s) with rootView.findViewById here
        // Note: State of variable initialized here could not be saved
        //       in onSavedInstanceState

        SharedPreferences prefs = getContext().getSharedPreferences("login_state", Context.MODE_PRIVATE);
        Log.w("prefs", String.valueOf(prefs.getAll()));

        if (prefs.getAll().toString().equals("{}")) {

            b.btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog = new ProgressDialog(getActivity());
                    dialog.setMessage("Please Wait...");
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();

                    email = b.edtLoginEmail.getText().toString();
                    password = b.edtLoginPassword.getText().toString();
                    getLogin(); // MOVE TO BACKGROUND

                    Log.w("BACKGROUND", "OFF from onPostExecute()");

                }

            });

        } else if (prefs.getString("login_state_var", null) != null) {

            if (prefs.getString("login_state_var", null).equals("success")) {

                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);

            } else {

                Log.e("FragmentLogin", "SharePref, login_state_var != success");

            }

        } else {

            Log.e("FragmentLogin", "SharePref, login_state_var == null");

        }


    }

    private void getLogin() {

        ApiService apiService = HttpManager.getInstance().create(ApiService.class);
//        apiService.loadStudentModule(Authorization,Content_Type,developer.getMemberID(),TopicId)
        apiService.lecturerLoginCheck(tag , email, password)
                .asObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Utils.getInstance().defaultSubscribeScheduler())
                .unsubscribeOn(Utils.getInstance().defaultSubscribeScheduler())
                .subscribe(new Action1<LecturerModuleCollectionDao>() {
                    @Override
                    public void call(LecturerModuleCollectionDao response) {

                        SharedPreferences prefs = getContext().getSharedPreferences("login_state", Context.MODE_PRIVATE);

                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
//                        realm.deleteAll(); // clear the current data before load new data
                        realm.delete(User.class); // delete only data of a specific class

                        if (response.getResult().equals("success")) {

                            SharedPreferences.Editor editor = prefs.edit();
                            // Add/Edit/Delete
                            editor.putString("login_state_var", response.getResult());
                            editor.putString("name", response.getUser().getName());
                            editor.putInt("lecturer_id", response.getUser().getLecturerId());
                            Log.d("FragmentLogin", String.valueOf(response.getUser().getLecturerId()));
                            editor.apply();

                            realm.copyToRealmOrUpdate(response.getUser());
                            realm.commitTransaction();

                            getLecturer(response.getUser().getLecturerId());

                        } else {
                            realm.commitTransaction();
                            dialog.dismiss();
                            Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();

                        }

                        String sharePrefVar = prefs.getString("login_state_var", null);

                        Log.w("getLogin", response.getResult());
                        Log.w("SharePrefs", sharePrefVar);

                    }

                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                        dialog.dismiss();
                        Utils.getInstance().onHoneyToast("LOGIN "+throwable.getLocalizedMessage());
                        Log.w("LOGIN CONNECTION PROBLEM", throwable.getLocalizedMessage());

                    }
                });

    }

    // dump data into Realm
    private void getLecturer(int lecturer_id){

//        Log.e("todayModule","Test="+studentId);
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
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);



                        Log.d("getLecturer", "call success");

                    }

                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        dialog.dismiss();
                        Utils.getInstance().onHoneyToast("LECTURER "+throwable.getLocalizedMessage());

                    }
                });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save Instance (Fragment level's variables) State here
    }

    @SuppressWarnings("UnusedParameters")
    private void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore Instance (Fragment level's variables) State here
    }


}
