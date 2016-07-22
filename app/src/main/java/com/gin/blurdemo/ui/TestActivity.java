package com.gin.blurdemo.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gin.blurdemo.BlurUtil;
import com.gin.blurdemo.R;

/**
 * Created by wanglc on 16/7/22.
 */
public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BlurUtil.getInstance().activity(mActivity).show();
        setContentView(R.layout.activity_test);
    }
    public void onClick(View view){
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        BlurUtil.getInstance().dismiss();
    }
    public static Activity mActivity;
    public static Intent getIntent(Activity activity){
        mActivity = activity;
        return new Intent(activity,TestActivity.class);
    }
}
