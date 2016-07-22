package com.gin.blurdemo.ui;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gin.blurdemo.BlurUtil;
import com.gin.blurdemo.R;

/**
 * Created by wanglc on 16/7/22.
 */
public class TestFragment extends Fragment {

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BlurUtil.getInstance().activity(activity).show();
    }

    private View btn_clonse;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.activity_test, container, false);
        btn_clonse = view.findViewById(R.id.close);
        btn_clonse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
        return view;
    }

    private void destory() {
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BlurUtil.getInstance().activity(getActivity()).dismiss();
    }
}
