package cn.ucai.fulicenter.task;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.HashMap;

import cn.ucai.fulicenter.I;
import cn.ucai.fulicenter.SuperWeChatApplication;
import cn.ucai.fulicenter.activity.BaseActivity;
import cn.ucai.fulicenter.bean.Member;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

/**
 * Created by sks on 2016/4/5.
 */
public class DownloadAllGroupMembersTask extends BaseActivity {
    public static final String TAG = DownloadAllGroupMembersTask.class.getName();
    Context mContext;
    String groupId;
    String path;

    public DownloadAllGroupMembersTask(Context context, String groupId) {
        this.mContext = context;
        this.groupId = groupId;
        initPath();

    }

    private void initPath(){
        try {
            path = new ApiParams()
                    .with(I.Member.GROUP_HX_ID, groupId)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_GROUP_MEMBERS_BY_HXID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        executeRequest(new GsonRequest<Member[]>(path,Member[].class,
                responseDownloadGroupMembersListener(), errorListener()));
    }

    private Response.Listener<Member[]> responseDownloadGroupMembersListener() {
        return new Response.Listener<Member[]>(){
            @Override
            public void onResponse(Member[] userList) {
                Log.e(TAG,"responseDownloadGroupMembersListener,userList="+userList);
                if(userList==null){
                    return;
                }
                Log.e(TAG,"responseDownloadGroupMembersListener,userList.length="+userList.length);
                HashMap<String, ArrayList<Member>> groupMembers =
                        SuperWeChatApplication.getInstance().getGroupMembers();
                ArrayList<Member> users = Utils.array2List(userList);
                groupMembers.put(groupId,users);
                Intent intent = new Intent("update_group_member");
                mContext.sendStickyBroadcast(intent);
            }
        };
    }
}
