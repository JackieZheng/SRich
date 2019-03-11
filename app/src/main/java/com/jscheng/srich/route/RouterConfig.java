package com.jscheng.srich.route;

import com.jscheng.srich.edit_note.EditNoteActivity;
import com.jscheng.srich.outline.OutLinesActivity;

/**
 * Created By Chengjunsen on 2019/2/21
 */
public class RouterConfig {
    public final static String OutLineActivityUri = "OutLineActivity";
    public final static String EditNoteActivityUri = "EditNoteActivity";

    public static void config() {
        Router.addInterceptor(new LogInterceptor());
        Router.addInterceptor(new ActivityInterceptor());
        Router.addInterceptor(new UriAnalyzerInterceptor());

        Router.addUri(OutLineActivityUri, OutLinesActivity.class);
        Router.addUri(EditNoteActivityUri, EditNoteActivity.class);
    }
}
