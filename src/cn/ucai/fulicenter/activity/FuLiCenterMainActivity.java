package cn.ucai.fulicenter.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import cn.ucai.fulicenter.R;

public class FuLiCenterMainActivity extends Activity {

    RadioButton mRbNewGood;
    RadioButton mRbBoutique;
    RadioButton mRbCategory;
    RadioButton mRbCart;
    RadioButton mRbPersonalCenter;
    RadioButton[] mRb = new RadioButton[5];

    TextView mTvCartHint;

    private int index;
    // 当前fragment的index
    private int currentTabIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fu_li_center_main);
        initView();
    }

    private void initView() {
        mRbNewGood = (RadioButton) findViewById(R.id.rbNewGoods);
        mRbBoutique = (RadioButton) findViewById(R.id.rbBoutique);
        mRbCategory = (RadioButton) findViewById(R.id.rbCategory);
        mRbCart = (RadioButton) findViewById(R.id.rbCart);
        mRbPersonalCenter = (RadioButton) findViewById(R.id.rbPersonalCenter);

        mTvCartHint = (TextView) findViewById(R.id.tvCartHint);

        mRb[0] = mRbNewGood;
        mRb[1] = mRbBoutique;
        mRb[2] = mRbCategory;
        mRb[3] = mRbCart;
        mRb[4] = mRbPersonalCenter;
    }

    public void onCheckedChange(View view){
        switch (view.getId()){
            case R.id.rbNewGoods:
                index=0;
                break;
            case R.id.rbBoutique:
                index=1;
                break;
            case R.id.rbCategory:
                index=2;
                break;
            case R.id.rbCart:
                index=3;
                break;
            case R.id.rbPersonalCenter:
                index=4;
                break;
        }
        if (currentTabIndex!=index){
            //mRb[index].setSelected(true);
            setRadioChecked(index);
            currentTabIndex = index;
        }
    }

    public void setRadioChecked(int index){
        for (int i=0;i<mRb.length;i++){
            if (i==index){
                mRb[index].setChecked(true);
            }else {
                mRb[index].setChecked(false);
            }
        }
    }
}
