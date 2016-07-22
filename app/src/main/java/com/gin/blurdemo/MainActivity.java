package com.gin.blurdemo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;

import com.gin.blurdemo.ui.TestActivity;
import com.gin.blurdemo.ui.TestFragment;
import com.gin.blurdemo.ui.TestPopu;



public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private View btn_activity, /*btn_fragment,*/ btn_dialog, btn_pop;

    private Intent intent;
    private FrameLayout frameLayout;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_activity = findViewById(R.id.activity);
//        btn_fragment = findViewById(R.id.fragment);
        btn_dialog = findViewById(R.id.dialog);
        btn_pop = findViewById(R.id.popuwindow);
        frameLayout = (FrameLayout) findViewById(R.id.fragment_container);

        btn_activity.setOnClickListener(this);
//        btn_fragment.setOnClickListener(this);
        btn_dialog.setOnClickListener(this);
        btn_pop.setOnClickListener(this);
    }
    private void setDefaultFragment()
    {
        frameLayout.setVisibility(View.VISIBLE);
        TestFragment test = new TestFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.fragment_container, test);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity:
                intent = TestActivity.getIntent(MainActivity.this);
                startActivity(intent);
                break;
//            case R.id.fragment:
//                setDefaultFragment();
//                break;
            case R.id.dialog:
                BlurUtil.getInstance().activity(MainActivity.this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View view = View.inflate(MainActivity.this,R.layout.activity_test,null);
                builder.setView(view);
                builder.setTitle("DialogTest");
                final AlertDialog dialog = builder.create();
                view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BlurUtil.getInstance().activity(MainActivity.this).dismiss();
                        dialog.dismiss();
                    }
                });
               dialog.show();
                break;
            case R.id.popuwindow:
                TestPopu popu = new TestPopu(MainActivity.this);
                popu.show();
                break;
        }
    }
}
