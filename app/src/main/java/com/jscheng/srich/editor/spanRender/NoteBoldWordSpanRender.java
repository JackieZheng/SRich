package com.jscheng.srich.editor.spanRender;

import com.jscheng.srich.editor.spans.NoteBoldSpan;
import com.jscheng.srich.model.Style;

/**
 * Created By Chengjunsen on 2019/3/4
 */
public class NoteBoldWordSpanRender extends NoteWordSpanRender<NoteBoldSpan> {

    @Override
    protected NoteBoldSpan createSpan() {
        return NoteBoldSpan.create();
    }

    @Override
    protected int getStyle() {
        return Style.Bold;
    }

    @Override
    protected boolean isStyle(int style) {
        return Style.isWordStyle(style, getStyle());
    }
}
