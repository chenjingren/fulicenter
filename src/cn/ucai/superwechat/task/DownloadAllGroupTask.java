package cn.ucai.superwechat.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.activity.BaseActivity;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.Utils;

public class DownloadAllGroupTask extends BaseActivity {
    private static final String TAG = DownloadAllGroupTask.class.getName();
    Context mContext;
    String username;
    String path;

    public DownloadAllGroupTask(Context mContext, String username) {
        this.mContext = mContext;
        this.username = username;
        initPath();
    }

    private void initPath() {
        try {
            path = new ApiParams()
                    .with(I.User.USER_NAME,username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUPS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        executeRequest(new GsonRequest<Group[]>(path,Group[].class,
                responseDownloadAllGroupTaskListener(),errorListener()));
    }

    private Response.Listener<Group[]> responseDownloadAllGroupTaskListener() {
        return new Response.Listener<Group[]>() {
            @Override
            public void onResponse(Group[] groups) {
                Log.e(TAG,"DownloadAllGroup");
                if(groups!=null){
                    Log.e(TAG,"DownloadAllGroup,groups size="+groups.length);
                    ArrayList<Group> list = Utils.array2List(groups);
                    ArrayList<Group> groupList =
                            SuperWeChatApplication.getInstance().getGroupList();
                    groupList.clear();
                    groupList.addAll(list);
//                    for (Group group:groupList){
//                        String groupName = group.getMGroupName();
//                        String header = "";
//
//                        for(int i=0;i<groupName.length();i++){
//                            String s = groupName.substring(i,i+1);
//                            header = header + HanziToPinyin.getInstance()
//                                    .get(s).get(0).target.toLowerCase();
//                        }
//                        group.setHeader(header);
//                    }
                    mContext.sendStickyBroadcast(new Intent("update_group_list"));
                }
            }
        };
    }
}
