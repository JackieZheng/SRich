package com.jscheng.srich;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jscheng.srich.model.Note;
import com.jscheng.srich.uitl.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created By Chengjunsen on 2019/2/20
 */
public class OutLinesActivity extends BaseActivity implements OutLinePresenter.OutLineView {
    private final static String TAG = "OutLinesActivity";
    private OutLinePresenter mPresenter = null;
    private RecyclerView mRecyclerView = null;
    private LinearLayoutManager mLayoutManager = null;
    private OutLinesRecyclerViewAdapter mRecyclerAdapter = null;
    private LinearLayout mHeadDateLayout = null;
    private TextView mHeadDateText = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outline);

        this.mPresenter = new OutLinePresenter();
        this.getLifecycle().addObserver(mPresenter);

        this.mHeadDateLayout = findViewById(R.id.outline_head_date);
        this.mHeadDateText = mHeadDateLayout.findViewById(R.id.date_text);

        this.mRecyclerView = findViewById(R.id.outline_recyclerview);
        this.mLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        this.mRecyclerAdapter = new OutLinesRecyclerViewAdapter(this, mLayoutManager);
        this.mRecyclerView.setLayoutManager(mLayoutManager);
        this.mRecyclerView.setAdapter(mRecyclerAdapter);
        this.mRecyclerView.addOnScrollListener(new ScrollChangeListener());

        List<Note> notes = this.generalData();
        this.mRecyclerAdapter.setData(notes);
    }

    private List<Note> generalData() {
        List<Note> notes = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Note note = NoteBuilder.create().mtime(System.nanoTime()).title("note " + i).build();
            notes.add(note);
        }
        return notes;
    }

    private class ScrollChangeListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            long time = mRecyclerAdapter.getFirstVisibleDateTime();
            if (time > 0) {
                mHeadDateLayout.setVisibility(View.VISIBLE);
                mHeadDateText.setText(DateUtil.formatDate(time));
            } else {
                mHeadDateText.setVisibility(View.INVISIBLE);
            }
        }
    }
}
