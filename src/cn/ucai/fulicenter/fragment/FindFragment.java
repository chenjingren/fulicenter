package cn.ucai.fulicenter.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.activity.NearPeopleActivity;

public class FindFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View layout=inflater.inflate(R.layout.fragment_find, container, false);
        setListener(layout);
        return layout;
    }

    private void setListener(View layout) {
        setNearPeopleClickListener(layout);
        setScanQRCodeClickListener(layout);
    }

    /** 扫描二维码*/
    private void setScanQRCodeClickListener(View layout) {
        layout.findViewById(R.id.layoutScanQRCode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                
            }
        });
    }

    /** 附近人*/
    private void setNearPeopleClickListener(View layout) {
        layout.findViewById(R.id.layoutNearPeople).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String hint=getResources().getString(R.string.near_people_hint);
                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                builder.setTitle("附近人")
                    .setMessage(hint)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent=new Intent(getActivity(), NearPeopleActivity.class);
                            startActivity(intent);
                        }
                    }).setNegativeButton("取消", null)
                    .create().show();
                    
                
            }
        });
    }
}
