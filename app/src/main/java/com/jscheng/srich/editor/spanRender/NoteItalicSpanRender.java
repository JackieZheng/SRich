package com.jscheng.srich.editor.spanRender;

import com.jscheng.srich.editor.spans.NoteItalicSpan;
import com.jscheng.srich.model.Style;

/**
 * Created By Chengjunsen on 2019/3/4
 */
public class NoteItalicSpanRender extends NoteWordSpanRender<NoteItalicSpan> {

    @Override
    protected NoteItalicSpan createSpan() {
        return NoteItalicSpan.create();
    }

    @Override
    protected int getStyle() {
        return Style.Italic;
    }

    @Override
    protected boolean isStyle(int style) {
        return Style.isStyle(style, getStyle());
    }
}
