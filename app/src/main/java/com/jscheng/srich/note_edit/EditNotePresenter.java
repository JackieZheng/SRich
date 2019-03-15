package com.jscheng.srich.note_edit;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.jscheng.srich.model.NoteModel;
import com.jscheng.srich.model.Note;
import com.jscheng.srich.mvp.IPresenter;
import com.jscheng.srich.mvp.IView;
import com.jscheng.srich.route.Router;
import com.jscheng.srich.route.RouterConfig;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created By Chengjunsen on 2019/2/21
 */
public class EditNotePresenter extends IPresenter {
    private EditNoteMode mMode;
    private EditNoteView mView;
    private boolean isEditorBarEnable;
    private Note mNote;
    private String mNoteid;
    private boolean isNewNote;

    public EditNotePresenter(Intent intent) {
        mNoteid = intent.getStringExtra("id");
    }

    public interface EditNoteView extends IView {
        void writingMode(boolean isEditorBarEnable);
        void readingMode();
        void loadingMode();
        void finish();
        void setEditorbar(boolean isEnable);
        void showFormatDialog();
        void showAlbumDialog();
        void showNetworkDialog();
        void setNote(Note note);
        void showLoading();
        void hideLoading();
    }

    @Override
    public void onCreate(@NotNull LifecycleOwner owner) {
        this.mView = (EditNoteView)owner;
        this.isEditorBarEnable = true;
        this.isNewNote = false;
        this.mNote = null;
        this.loadNote();
    }

    private void loadNote() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter emitter) {
                mNote = NoteModel.findNote((Context)mView, mNoteid);
                if (mNote == null) {
                    mNote = NoteModel.createNote((Context)mView);
                    emitter.onNext(true);
                } else {
                    NoteModel.openNote(mNote);
                    emitter.onNext(false);
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Observer<Boolean>() {
              @Override
              public void onSubscribe(Disposable d) {
                  loadingMode();
              }

              @Override
              public void onNext(Boolean isNew) {
                  isNewNote = isNew;
                  mView.setNote(mNote);
              }

              @Override
              public void onError(Throwable e) {
              }

              @Override
              public void onComplete() {
                  if (isNewNote) {
                      writingMode();
                  } else {
                      readingMode();
                  }
              }
          });
    }

    @Override
    public void onDestroy(@NotNull LifecycleOwner owner) {
        this.mView = null;
    }

    public void tapNetworkUrl() {
        mView.showNetworkDialog();
    }

    public void tapAlbum() {
        mView.showAlbumDialog();
    }

    public void tapEdit() {
        writingMode();
    }

    public void tapTick() {
        readingMode();
        updateNote();
    }

    public void tapBack() {
        if (mMode == EditNoteMode.Writing) {
            readingMode();
        } else {
            updateNote();
            mView.finish();
        }
    }

    public void tapMore() {

    }

    public void tapRedo() {

    }

    public void tapUndo() {

    }


    public void tapImage(List<String> urls, int index) {
        Router.with((Context) mView)
                .intent("urls", urls)
                .intent("index", index)
                .route(RouterConfig.ImagePreviewActivityUri)
                .go();
    }

    public void tapAttach() {
        mView.showFormatDialog();
    }

    public void tapEditorBar(boolean isEnable) {
        isEditorBarEnable = isEnable;
        mView.setEditorbar(isEditorBarEnable);
    }

    private void loadingMode() {
        mMode = EditNoteMode.Loading;
        mView.loadingMode();

    }

    private void readingMode() {
        mMode = EditNoteMode.Reading;
        mView.readingMode();
    }

    private void writingMode() {
        mMode = EditNoteMode.Writing;
        mView.writingMode(isEditorBarEnable);
    }

    private void updateNote() {
        if (NoteModel.isNoteNull(mNote)) {
            NoteModel.deleteNote((Context)mView, mNote);
            if (!isNewNote) {
                Toast.makeText((Context) mView, "正在删除数据", Toast.LENGTH_SHORT).show();
            }
        } else if (mNote.isDirty()) {
            NoteModel.updateNote((Context)mView, mNote);
            Toast.makeText((Context) mView, "正在保存数据", Toast.LENGTH_SHORT).show();
        }
    }
}
