package com.jscheng.srich.editor;
import android.util.Log;

import com.jscheng.srich.model.Note;
import com.jscheng.srich.model.Options;
import com.jscheng.srich.model.Paragraph;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created By Chengjunsen on 2019/2/27
 * SPAN_EXCLUSIVE_EXCLUSIVE // 在Span前后输入的字符都不应用Span效果
 * SPAN_EXCLUSIVE_INCLUSIVE // 在Span前面输入的字符不应用Span效果，后面输入的字符应用Span效果
 * SPAN_INCLUSIVE_EXCLUSIVE // 在Span前面输入的字符应用Span效果，后面输入的字符不应用Span效果
 * SPAN_INCLUSIVE_INCLUSIVE // 在Span前后输入的字符都应用Span效果
 */
public class NoteEditorManager {
    private final static String TAG = "NoteEditorManager";
    private Note mNote;
    private Options mOptions;
    private List<OnSelectionChangeListener> mSelectionListeners;
    private NoteEditorText mEditorText;
    private NoteEditorRender mRender;
    private int mSelectionStart;
    private int mSelectionEnd;

    public NoteEditorManager(NoteEditorText editorText) {
        mNote = new Note();
        mRender = new NoteEditorRender(editorText);
        mOptions = new Options();
        mSelectionListeners = new ArrayList<>();
        mEditorText = editorText;
    }

    public void addSelectionChangeListener(OnSelectionChangeListener listener) {
        mSelectionListeners.add(listener);
    }

    public void notifySelectionChangeListener(int start, int end, Options options) {
        if (mSelectionListeners != null) {
            for (OnSelectionChangeListener listener : mSelectionListeners) {
                listener.onStyleChange(start, end, options);
            }
        }
    }

    public void commandColor(boolean isSelected, boolean draw) {
        mOptions.setColor(isSelected);
    }

    public void commandUnderline(boolean isSelected, boolean draw) {
        mOptions.setUnderline(isSelected);
    }

    public void commandItalic(boolean isSelected, boolean draw) {
        mOptions.setItalic(isSelected);
    }

    public void commandBold(boolean isSelected, boolean draw) {
        mOptions.setBold(isSelected);
    }

    public void commandSuperscript(boolean isSelected, boolean draw) {
        mOptions.setSuperScript(isSelected);
    }

    public void commandSubscript(boolean isSelected, boolean draw) {
        mOptions.setSubScript(isSelected);
    }

    public void commandStrikeThrough(boolean isSelected, boolean draw) {
        mOptions.setStrikethrough(isSelected);
    }

    public void commandDividingLine(boolean draw) {
        inputDividingLine();
        if (draw) { requestDraw(); }
    }

    public void commandBulletList(boolean isSelected, boolean draw) {

    }

    public void commandNumList(boolean isSelected, boolean draw) {

    }

    public void commandDeleteSelection(boolean draw) {
        deleteSelectionParagraphs(mSelectionStart, mSelectionEnd);
        if (draw) { requestDraw(); }
    }

    public void commandDelete(boolean draw) {
        if (mSelectionStart == mSelectionEnd) {
            deleteSelectionParagraphs(mSelectionStart - 1, mSelectionStart);
        } else {
            deleteSelectionParagraphs(mSelectionStart, mSelectionEnd);
        }
        deleteSelectionParagraphs();
        if (draw) { requestDraw(); }
    }

    public void commandDelete(int num, boolean draw) {
        deleteSelectionParagraphs(mSelectionStart - num, mSelectionStart);
        if (draw) { requestDraw(); }
    }

    public void commandPaste(String content, boolean draw) {
        commandInput(content, draw);
    }

    public void commandEnter(boolean draw) {
        inputEnter();
        if (draw) { requestDraw(); }
    }

    public void commandInput(CharSequence content, boolean draw) {
        if (content.length() > 0) {
            StringBuilder contentNoEnter = new StringBuilder();
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == NoteEditorRender.EndCodeChar) {
                    inputParagraph(contentNoEnter.toString());
                    inputEnter();
                    contentNoEnter.delete(0, contentNoEnter.length());
                } else {
                    contentNoEnter.append(c);
                }
            }
            if (contentNoEnter.length() > 0) {
                inputParagraph(contentNoEnter.toString());
            }
        }
        if (draw) { requestDraw(); }
    }

    private void inputEnter() {
        // 删除区间
        deleteSelectionParagraphs();

        int pos = mSelectionStart;
        // 获取段落
        Paragraph lastParagraph = getParagraph(pos);
        if (lastParagraph == null) {
            lastParagraph = createParagraph(0);
        }

        int index = getParagraphIndex(lastParagraph);

        // 分割成两部分
        int cutPos = pos - getParagraphBegin(lastParagraph);
        int newLineStyle = lastParagraph.getLineStyle();
        int newIndentation = lastParagraph.getIndentation();
        String newWords = lastParagraph.getWords(cutPos, lastParagraph.getLength());
        List<Integer> newWordStyles = new LinkedList<>(
                lastParagraph.getWordStyles(cutPos, lastParagraph.getLength()));

        // 删除旧段落后半部分
        lastParagraph.remove(cutPos, lastParagraph.getLength());

        // 加入新段落
        Paragraph newParagraph = createParagraph(index + 1, newIndentation, newLineStyle);
        newParagraph.setWords(newWords, newWordStyles);

        // 计算新的选择区
        int newPos = getParagraphBegin(newParagraph);
        setSeletion(newPos);
    }

    private void inputParagraph(String content) {
        Log.e(TAG, "inputParagraph: " + content );
        if (content.isEmpty()) { return; }
        // 删除区间
        deleteSelectionParagraphs();

        int pos = mSelectionStart;
        // 获取段落
        Paragraph paragraph = getParagraph(pos);
        if (paragraph == null) {
            paragraph = createParagraph(0);
        }

        // 计算插入位置
        int begin = getParagraphBegin(paragraph);
        int insertPos = pos - begin;

        // 插入位置
        paragraph.insert(insertPos, content, mOptions);

        // 调整选择区
        setSeletion(pos + content.length());
    }

    private void inputDividingLine() {
        // 删除区间
        deleteSelectionParagraphs();

        int pos = mSelectionStart;

        Paragraph paragraph = getParagraph(mSelectionStart);
        if (paragraph == null) {
            paragraph = createDividingParagraph(0);
        }

        setSeletion(pos);
    }

    private void deleteSelectionParagraphs() {
        deleteSelectionParagraphs(mSelectionStart, mSelectionEnd);
    }

    private void deleteSelectionParagraphs(int start, int end) {
        if (start < 0 || start >= end) {
            return;
        }
        Paragraph firstParagraph = null;
        Iterator<Paragraph> iter = mNote.getParagraphs().iterator();

        int startPos = 0;
        int endPos = 0;
        while (iter.hasNext()) {
            Paragraph item = iter.next();
            endPos = startPos + item.getLength();
            if (start >= startPos && start <= endPos) { // first paragraph
                firstParagraph = item;
                int delParaStartPos = start - startPos;
                int delParaEndPos = end - startPos;
                if (delParaEndPos > item.getLength()) {
                    delParaEndPos = item.getLength();
                }
                firstParagraph.remove(delParaStartPos,delParaEndPos);

            } else if (start <= startPos && end >= endPos) { // middle paragraph
                iter.remove();

            } else if (end >= startPos && end <= endPos) { // last paragraph
                int demainParaStartPos = end - startPos;
                int demainParaEndPos = item.getLength();
                String demainContent = item.getWords(demainParaStartPos, demainParaEndPos);
                List<Integer> demainStyles = item.getWordStyles(demainParaStartPos, demainParaEndPos);
                firstParagraph.addWords(demainContent, demainStyles);
                iter.remove();
                break;

            }
            startPos = endPos + 1;
        }
        setSeletion(start);
    }

    private Paragraph getParagraph(int globalPos) {
        int startPos = 0;
        int endPos;
        for (Paragraph paragraph : mNote.getParagraphs()) {
            endPos = startPos + paragraph.getLength();
            if (globalPos >= startPos && globalPos <= endPos) {
                return paragraph;
            }
            startPos = endPos + 1;
        }
        return null;
    }

    private int getParagraphIndex(Paragraph paragraph) {
        return mNote.getParagraphs().indexOf(paragraph);
    }

    private int getParagraphBegin(Paragraph aim) {
        int startPos = 0;
        int endPos;
        for (Paragraph paragraph : mNote.getParagraphs()) {
            endPos = startPos + paragraph.getLength();
            if (aim == paragraph) {
                return startPos;
            }
            startPos = endPos + 1;
        }
        return startPos;
    }

    private int getParagraphEnd(Paragraph aim) {
        int begin = getParagraphBegin(aim);
        return begin + aim.getLength();
    }

    private Paragraph createParagraph(int index) {
        return createParagraph(index, mOptions.getIndentation(), mOptions.getLineStyle());
    }

    private Paragraph createParagraph(int index, int indenteation, int lineStyle) {
        Paragraph paragraph = new Paragraph();
        paragraph.setDirty(true);
        paragraph.setIndentation(indenteation);
        paragraph.setLineStyle(lineStyle);
        paragraph.setDividingLine(false);

        mNote.getParagraphs().add(index, paragraph);
        return paragraph;
    }

    private Paragraph createDividingParagraph(int index) {
        Paragraph paragraph = new Paragraph();
        paragraph.setDirty(true);
        paragraph.setIndentation(0);
        paragraph.setLineStyle(0);
        paragraph.setDividingLine(true);

        mNote.getParagraphs().add(index, paragraph);
        return paragraph;
    }

    public void setSeletion(int globalPos) {
        mSelectionStart = globalPos;
        mSelectionEnd = globalPos;
    }

    public void setSeletion(int globalStart, int globalEnd) {
        mSelectionStart = globalStart;
        mSelectionEnd = globalEnd;
    }

    public void requestDraw() {
        mRender.draw(mEditorText, mNote.getParagraphs(), mSelectionStart, mSelectionEnd);
        print();
    }

    public void print() {
        List<Paragraph> paragraphs = mNote.getParagraphs();
        Log.e(TAG, " count: " + paragraphs.size() + "selection: ( " + mSelectionStart + " , " + mSelectionEnd + " )");
        for (Paragraph paragraph: paragraphs) {
            int startPos = getParagraphBegin(paragraph);
            int endPos = getParagraphEnd(paragraph);
            Log.e(TAG, "[ " + startPos + "->" + endPos + " ] " + paragraph.toString());
        }
    }

    public String getSelectionText() {
        return "test \n test";
    }
}
