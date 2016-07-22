package com.gin.blurdemo.ui;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.gin.blurdemo.BlurUtil;
import com.gin.blurdemo.R;


/**
 * Created by wanglc on 16/6/23.
 */
public class TestPopu {

    private PopupWindow popupWindow;
    private Activity mActivity;

    private View rootView,btn_Close;

    public TestPopu(Context context) {
        mActivity = (Activity) context;

        initView();
    }

    private void initView() {
        rootView = LayoutInflater.from(mActivity).inflate(R.layout.activity_test, null);
        btn_Close = rootView.findViewById(R.id.close);
        btn_Close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        popupWindow = new PopupWindow(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        popupWindow.setFocusable(true);
        // 设置点击窗口外边窗口消失  
        popupWindow.setOutsideTouchable(false);
        // 设置弹出窗体需要软键盘  
        popupWindow.setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
        // 再设置模式，和Activity的一样，覆盖，调整大小。  
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    public void show() {

        BlurUtil.getInstance().activity(mActivity).show();
        popupWindow.setContentView(rootView);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }
    public void dismiss() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        BlurUtil.getInstance().activity(mActivity).dismiss();
    }
}
