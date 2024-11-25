package com.landtanin.teacherattendancemonitoring.fragment.day;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.landtanin.teacherattendancemonitoring.R;
import com.landtanin.teacherattendancemonitoring.activity.StudentListActivity;
import com.landtanin.teacherattendancemonitoring.adapter.TimeTableListAdapter;
import com.landtanin.teacherattendancemonitoring.adapter.TimeTableListItem;
import com.landtanin.teacherattendancemonitoring.dao.LecturerModuleDao;
import com.landtanin.teacherattendancemonitoring.databinding.FragmentTuesdayBinding;

import java.util.ArrayList;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;


/**
 * Created by nuuneoi on 11/16/2014.
 */
public class TuesdayFragment extends Fragment {

    private TimeTableListAdapter mTimeTableListAdapter;
    private List<TimeTableListItem> mTimeTableListItems = new ArrayList<>();
    private FragmentTuesdayBinding b;

    LinearLayoutManager mLayoutManager;
    TimeTableListAdapter TimeTableListAdapter;
    private Realm realm;

    public TuesdayFragment() {
        super();
    }

    public static TuesdayFragment newInstance() {
        TuesdayFragment fragment = new TuesdayFragment();
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
        b = DataBindingUtil.inflate(inflater, R.layout.fragment_tuesday, container, false);
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
        realm = Realm.getDefaultInstance();
        RealmResults<LecturerModuleDao> lecturerModuleDao = realm.where(LecturerModuleDao.class).equalTo("day","Tue", Case.SENSITIVE).findAll();


        Log.w("MONDAY_module", String.valueOf(lecturerModuleDao.size()));

        if (lecturerModuleDao.size()!=0) {

            LinearLayoutManager rvLayoutManager = new LinearLayoutManager(getContext());
            b.rvTuesdayTimeTable.setLayoutManager(rvLayoutManager);

            TimeTableListAdapter.ClickListener clickListener = new TimeTableListAdapter.ClickListener() {
                @Override
                public void myOnClickListener(String moduleId, int itemNumber) {

                    RealmResults<LecturerModuleDao> forClickLecturerModuleDao
                            = realm.where(LecturerModuleDao.class).equalTo("day","Tue", Case.SENSITIVE).findAll();

                    if (forClickLecturerModuleDao.get(itemNumber).getModStatus().equals("active")) {

                        Intent intent = new Intent(getContext(), StudentListActivity.class);
                        intent.putExtra("module_id", moduleId);
                        startActivity(intent);

                    } else {

                        Toast.makeText(getContext(), "Module is inactive", Toast.LENGTH_SHORT).show();

                    }

                }
            };

            mTimeTableListAdapter = new TimeTableListAdapter(getContext(), lecturerModuleDao, true, clickListener);
            b.rvTuesdayTimeTable.setAdapter(mTimeTableListAdapter);
            b.rvTuesdayTimeTable.setHasFixedSize(true);

        } else {

            b.rvTuesdayTimeTable.setVisibility(View.GONE);
            b.tueNoModuleText.setText("You are free today");
            b.tueNoModuleText.setVisibility(View.VISIBLE);

        }

    //        connectToDataBase();

        }

    private void connectToDataBase() {

        // hardcoded item to RecyclerView
        for (int i = 0; i < 100; i++) {

//            AddModuleItem addModuleItem = new AddModuleItem("item " + i, "item2 " + i, false);
            TimeTableListItem timeTableListItem = new TimeTableListItem("Tue module " + i, "A000" + i, i % 2 == 0 ? "active" : "inactive", "9-12", "School of Engineering");
            mTimeTableListItems.add(timeTableListItem);

        }

        mTimeTableListAdapter.notifyDataSetChanged();

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
