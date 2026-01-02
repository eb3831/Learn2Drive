package com.example.learn2drive.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.learn2drive.Adapters.LessonAdapter;
import com.example.learn2drive.Classes.Lesson;
import com.example.learn2drive.R;

import java.util.ArrayList;

public class StudentHomeFragment extends Fragment
{

    private ListView lvNextLessons;
    private Button btnHistory;
    private Button btnNewLesson;

    private ArrayList<Lesson> lessonsList;
    private LessonAdapter adapter;

    public StudentHomeFragment()
    {
        // בנאי ריק (חובה)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_student_home, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view)
    {
        lvNextLessons = view.findViewById(R.id.lvStNextLessons);
        btnHistory = view.findViewById(R.id.moveToStHistory);
        btnNewLesson = view.findViewById(R.id.moveToStNewLesson);

        setupListView();
        setupListeners();
    }

    private void setupListView()
    {
        lessonsList = new ArrayList<>();

        // --- נתונים לדוגמה (בהמשך נחליף בשליפה מ-Firebase) ---
        lessonsList.add(new Lesson("12/10/2025", "09:00", "60 min"));
        lessonsList.add(new Lesson("15/10/2025", "13:00", "45 min"));
        lessonsList.add(new Lesson("18/10/2025", "10:00", "60 min"));
        lessonsList.add(new Lesson("20/10/2025", "16:00", "60 min"));

        if (getContext() != null)
        {
            adapter = new LessonAdapter(getContext(), lessonsList);
            lvNextLessons.setAdapter(adapter);
        }
    }

    private void setupListeners()
    {
        btnHistory.setOnClickListener(new View.OnClickListener(

        ) {
            @Override
            public void onClick(View v)
            {
                // כרגע רק נציג הודעה. בהמשך נעשה כאן מעבר מסך.
                Toast.makeText(getContext(), "Clicked: History", Toast.LENGTH_SHORT).show();
            }
        });

        btnNewLesson.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // כרגע רק נציג הודעה.
                Toast.makeText(getContext(), "Clicked: New Lesson", Toast.LENGTH_SHORT).show();
            }
        });
    }
}