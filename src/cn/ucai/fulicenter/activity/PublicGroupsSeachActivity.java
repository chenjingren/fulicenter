package cn.ucai.fulicenter.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.R;
import cn.ucai.fulicenter.bean.Group;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.UserUtils;

public class PublicGroupsSeachActivity extends BaseActivity{
    private RelativeLayout containerLayout;
    private EditText idET;
    private TextView nameText;
    private NetworkImageView nivAvatar;
    public static Group searchedGroup;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_public_groups_search);
        
        containerLayout = (RelativeLayout) findViewById(R.id.rl_searched_group);
        idET = (EditText) findViewById(R.id.et_search_id);
        nameText = (TextView) findViewById(R.id.name);
        nivAvatar = (NetworkImageView) findViewById(R.id.avatar);
        
        searchedGroup = null;
    }
    
    /**
     * 搜索
     * @param v
     */
    public void searchGroup(View v){
        if(TextUtils.isEmpty(idET.getText())){
            return;
        }

        pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.searching));
        pd.setCancelable(false);
        pd.show();
        try {
            String path = new ApiParams()
                    .with(I.Group.HX_ID,idET.getText().toString())
                    .getRequestUrl(I.REQUEST_FIND_PUBLIC_GROUP_BY_HXID);
            executeRequest(new GsonRequest<Group>(path,Group.class,
                    responseFindGroupListener(),errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<Group> responseFindGroupListener() {
        return new Response.Listener<Group>() {
            @Override
            public void onResponse(Group group) {
                if(group!=null){
                    searchedGroup = group;
                    pd.dismiss();
                    containerLayout.setVisibility(View.VISIBLE);
                    nameText.setText(group.getMGroupName());
                    UserUtils.setGroupBeanAvatar(group.getMGroupHxid(),nivAvatar);
                }else{
                    pd.dismiss();
                    searchedGroup = null;
                    containerLayout.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.group_not_existed), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }


    /**
     * 点击搜索到的群组进入群组信息页面
     * @param view
     */
    public void enterToDetails(View view){
        startActivity(new Intent(this, GroupSimpleDetailActivity.class));
    }
}
