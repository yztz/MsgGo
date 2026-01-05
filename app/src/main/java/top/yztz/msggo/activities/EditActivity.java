package top.yztz.msggo.activities;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.yztz.msggo.R;
import top.yztz.msggo.data.DataModel;
import top.yztz.msggo.util.ToastUtil;

public class EditActivity extends AppCompatActivity {
    private EditText mEt;
//    private DrawerLayout mDrawerLayout;
    private BottomAppBar mBottomAppBar;
    private FloatingActionButton mBtnSave;
    private boolean edited;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        mEt = findViewById(R.id.et_editor);
//        mEt.setLineSpacing(0, 1.4f); // Fixed line height for stability
        mBtnSave = findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(v->{
            DataModel.setTemplate(mEt.getText().toString().trim());
            DataModel.saveAsHistory(EditActivity.this);
            ToastUtil.show(EditActivity.this, getString(R.string.save_success));
            finish();
        });

        mBottomAppBar = findViewById(R.id.bottomAppBar);
        mBottomAppBar.setOnMenuItemClickListener(v->{
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(EditActivity.this);
            int itemId = v.getItemId();
            if (itemId == R.id.btn_var) {
                dialogBuilder.setTitle(getString(R.string.variable_selection_title))
                        .setItems(DataModel.getTitles(), (dialog, which) -> {
                            int loc = mEt.getSelectionStart();
                            String pat = "${" + DataModel.getTitles()[which] + "}";
                            if (loc == -1) mEt.getText().append(pat);
                            else mEt.getText().insert(loc, pat);
                            dialog.dismiss();
                        }).setCancelable(true).show();
                return true;
            } else if (itemId == R.id.btn_clear) {
                dialogBuilder.setTitle(getString(R.string.confirm_clear_title))
                        .setCancelable(true).setMessage(getString(R.string.confirm_clear_msg))
                        .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                            mEt.getText().clear();
                            dialog.dismiss();
                        }).setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                            dialog.dismiss();
                        }).show();
                // Respond to positive button press
                return true;
            }
                return false;
        });


        //获取已经保存的内容并显示
        mEt.setText(DataModel.getTemplate());
        highlight(mEt.getText());


        //自动高亮变量
        mEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //高亮显示
                mEt.removeTextChangedListener(this); // 暂时移除监听
                highlight(s);
                mEt.addTextChangedListener(this);
                edited = true;
            }
        });
        //线性布局
//        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
//        mRv.setLayoutManager(manager);
//        //分割线
//        mRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
//        mRv.setAdapter(adapter);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (edited) {
                    new MaterialAlertDialogBuilder(EditActivity.this)
                            .setTitle(getString(R.string.edit_unsaved_title))
                            .setMessage(getString(R.string.edit_unsaved_msg))
                            .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                                setEnabled(false); // 禁用此回调
                                getOnBackPressedDispatcher().onBackPressed(); // 触发系统默认退出
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Ensure cursor doesn't land inside a chip
        mEt.setAccessibilityDelegate(null); // Optional: some systems might need this for better control
        mEt.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                checkSelection(mEt.getSelectionStart(), mEt.getSelectionEnd());
            }
        });

        mEt.setOnClickListener(v -> checkSelection(mEt.getSelectionStart(), mEt.getSelectionEnd()));

        mEt.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                int selectionStart = mEt.getSelectionStart();
                int selectionEnd = mEt.getSelectionEnd();
                if (selectionStart == selectionEnd && selectionStart > 0) {
                    Editable text = mEt.getText();
                    if (text != null) {
                        VariableChipSpan[] spans = text.getSpans(selectionStart - 1, selectionStart, VariableChipSpan.class);
                        for (VariableChipSpan span : spans) {
                            int spanEnd = text.getSpanEnd(span);
                            if (selectionStart == spanEnd) {
                                int spanStart = text.getSpanStart(span);
                                text.delete(spanStart, spanEnd);
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        });
    }

    private void checkSelection(int start, int end) {
        Editable text = mEt.getText();
        if (text == null) return;
        VariableChipSpan[] spans = text.getSpans(Math.max(0, start - 1), Math.min(text.length(), end + 1), VariableChipSpan.class);
        for (VariableChipSpan span : spans) {
            int spanStart = text.getSpanStart(span);
            int spanEnd = text.getSpanEnd(span);
            if (start > spanStart && start < spanEnd) {
                // Inside! Move to nearest edge
                if (start - spanStart < spanEnd - start) {
                    mEt.setSelection(spanStart);
                } else {
                    mEt.setSelection(spanEnd);
                }
                break;
            }
        }
    }

    private void highlight(Editable s) {
        String temp = s.toString();
        // Remove old spans
        VariableChipSpan[] oldSpans = s.getSpans(0, s.length(), VariableChipSpan.class);
        for (VariableChipSpan oldSpan : oldSpans) {
            s.removeSpan(oldSpan);
        }

        Matcher m = VARIABLE_PATTERN.matcher(temp);
        int bgColor = ContextCompat.getColor(this, R.color.md_theme_primaryContainer);
        int textColor = ContextCompat.getColor(this, R.color.md_theme_onPrimaryContainer);
        int strokeColor = ContextCompat.getColor(this, R.color.md_theme_primary);
        int padding = (int) (10 * getResources().getDisplayMetrics().density);

        while (m.find()) {
            String varName = m.group(1); // Get variable name without ${}
            VariableChipSpan span = new VariableChipSpan(varName, bgColor, textColor, strokeColor, padding);
            s.setSpan(span, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Variable Chip Span
     */
    private static class VariableChipSpan extends ReplacementSpan {
        private final String displayText;
        private final int backgroundColor;
        private final int textColor;
        private final int strokeColor;
        private final int horizontalPadding;

        public VariableChipSpan(String displayText, int backgroundColor, int textColor, int strokeColor, int horizontalPadding) {
            this.displayText = displayText;
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
            this.strokeColor = strokeColor;
            this.horizontalPadding = horizontalPadding;
        }

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
            float originalSize = paint.getTextSize();
            paint.setTextSize(originalSize * 0.85f); // Shrink font size
            paint.setFakeBoldText(true);
            
            float textWidth = paint.measureText(this.displayText);
            int margin = (int) (4 * horizontalPadding / 8f);
            
            // Restore size to not affect other spans
            paint.setTextSize(originalSize);
            return (int) (textWidth + 2 * horizontalPadding + 2 * margin);
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
            float originalSize = paint.getTextSize();
            paint.setTextSize(originalSize * 0.85f); // Shrink font size
            paint.setFakeBoldText(true);
            
            float textWidth = paint.measureText(this.displayText);
            Paint.FontMetrics fm = paint.getFontMetrics();
            float margin = 4 * horizontalPadding / 8f;
            
            // Calculate chip height using the shrunk font metrics
            float chipHeight = (fm.descent - fm.ascent) * 1.2f;
            float verticalCenter = y + (fm.ascent + fm.descent) / 2;
            float chipTop = verticalCenter - chipHeight / 2;
            float chipBottom = verticalCenter + chipHeight / 2;
            
            RectF rect = new RectF(x + margin, chipTop, x + textWidth + 2 * horizontalPadding + margin, chipBottom);
            float radius = rect.height() / 4;
            
            // 1. Draw Background
            paint.setColor(backgroundColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rect, radius, radius, paint);
            
            // 2. Draw Stroke
            paint.setColor(strokeColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            canvas.drawRoundRect(rect, radius, radius, paint);
            
            // 3. Draw Text (centered vertically in the rect)
            paint.setColor(textColor);
            paint.setStyle(Paint.Style.FILL);
            // Re-calculate baseline for the shrunk text
            float textBaseline = verticalCenter - (fm.ascent + fm.descent) / 2;
            canvas.drawText(this.displayText, x + horizontalPadding + margin, textBaseline, paint);
            
            // Restore paint state
            paint.setTextSize(originalSize);
            paint.setFakeBoldText(false);
        }
    }


    /**
     * 打开编辑器
     */
    public static void openEditor(Context context) {
        if (!DataModel.loaded()) {
            ToastUtil.show(context, context.getString(R.string.error_import_data_first));
        } else {
            Intent intent = new Intent(context, EditActivity.class);
            context.startActivity(intent);
        }
    }
}
