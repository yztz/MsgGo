package top.yzzblog.messagehelper.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.transition.MaterialSharedAxis;
import com.kongzue.dialogx.dialogs.BottomMenu;
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;

import top.yzzblog.messagehelper.activities.EditActivity;
import top.yzzblog.messagehelper.data.DataLoader;
import top.yzzblog.messagehelper.R;
import top.yzzblog.messagehelper.services.SMSSender;
import top.yzzblog.messagehelper.util.ToastUtil;
import top.yzzblog.messagehelper.activities.ChooserActivity;

public class HomeFrag extends Fragment {
    private static final String TAG = "HomeFrag";
    private Context context;
//    private LinearLayout mLinearSend, mLinearEdit;
//    private CarouselPicker carouselPicker;
    private Button simBtn;
    private Button mBtnSend;
    private Button mBtnEdit;
    private TextSwitcher mTitle;
    private List<SubscriptionInfo> subs;
    private int simSubId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        MaterialSharedAxis enterTransition = new MaterialSharedAxis(MaterialSharedAxis.X, false);
        MaterialSharedAxis returnTransition = new MaterialSharedAxis(MaterialSharedAxis.X, true);

        setEnterTransition(enterTransition);
        setReturnTransition(returnTransition);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_home, null);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: ");
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        super.onViewCreated(view, savedInstanceState);
        mBtnSend = view.findViewById(R.id.send_btn);
        mBtnEdit = view.findViewById(R.id.edit_btn);
        simBtn = view.findViewById(R.id.sim_btn);
        simSubId = DataLoader.getSimSubId();
//        mTitle = view.findViewById(R.id.title);



//        AssetManager mgr = getContext().getAssets();
//        Typeface tf = Typeface.createFromAsset(mgr, "font/noto_serif.ttf");
//        mTitle.setTypeface(tf);

//        carouselPicker = view.findViewById(R.id.carousel);

        mBtnSend.setOnClickListener(v -> {
            //注意数据表的读入
            //注意返回结果
            if (DataLoader.getDataModel() == null) {
                ToastUtil.show(context, "请先点击“+”导入数据哦~");
            } else {
                if(!TextUtils.isEmpty(DataLoader.getContent())) {
                    Intent intent = new Intent(context, ChooserActivity.class);

                    startActivity(intent);
                }else {
                    ToastUtil.show(context, "短信内容不得为空哦~");
                }
            }
        });
        //编辑按钮监听
        mBtnEdit.setOnClickListener(v -> EditActivity.openEditor(context));

        loadSimInfo();

    }

    private void loadSimInfo() {
        this.subs = SMSSender.getSubs(getContext());

        List<CharSequence> des = new ArrayList<>();
        for (int i = 0; i < subs.size(); i++) {
            SubscriptionInfo sub = subs.get(i);
            String txt = String.format("卡槽 %d %s", sub.getSimSlotIndex(), sub.getCarrierName());
            des.add(txt);
        }


        simBtn.setOnClickListener(v -> {
            int selected = -1;
            for (int i = 0; i < subs.size(); i++) {
                SubscriptionInfo sub = subs.get(i);
                if (sub.getSubscriptionId() == simSubId) {
                    selected = i;
                    break;
                }
            }
            if (selected == -1) {
                selected = 0;
                // not sim found, maybe removed?
                DataLoader.setSimSubId(subs.get(0).getSubscriptionId());
            }

            BottomMenu.show(des)
                    .setTitle("SIM 卡选择")
                    .setMessage("选择一个用于发送短信的 SIM 卡").setOnMenuItemClickListener((dialog, text, index) -> {
                        //记录已选择值
                        simSubId = subs.get(index).getSubscriptionId();
                        // save sim
                        DataLoader.setSimSubId(simSubId);
                        ToastUtil.show(getContext(), "已选择: " + text);
                        return false;
                    }).setSelection(selected);
        });
    }

//    private void loadSimInfo() {
//        List<SubscriptionInfo> subs = SMSSender.getSubs(getContext());
//        List<CarouselPicker.PickerItem> textItems = new ArrayList<>();
//        for (SubscriptionInfo sub : subs) {
//            String txt = String.format("SIM %d %s", sub.getSimSlotIndex(), sub.getCarrierName());
//            CarouselPicker.TextItem item =  new CarouselPicker.TextItem(txt, 5);
//            textItems.add(item);
//        }
//        CarouselPicker.CarouselViewAdapter textAdapter = new CarouselPicker.CarouselViewAdapter(getContext(), textItems, 0);
//        textAdapter.setTextColor(Color.WHITE);
//        carouselPicker.setAdapter(textAdapter);
//    }


}
