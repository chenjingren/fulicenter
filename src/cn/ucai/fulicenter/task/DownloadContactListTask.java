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
import cn.ucai.fulicenter.bean.Contact;
import cn.ucai.fulicenter.data.ApiParams;
import cn.ucai.fulicenter.data.GsonRequest;
import cn.ucai.fulicenter.utils.Utils;

public class DownloadContactListTask extends BaseActivity {
    private static final String TAG = DownloadContactListTask.class.getName();
    Context mContext;
    String username;
    String path;

    public DownloadContactListTask(Context mContext, String username) {
        this.mContext = mContext;
        this.username = username;
        initPath();
    }

    private void initPath(){
        try {
            path = new ApiParams()
                    .with(I.Contact.USER_NAME,username)
                    .getRequestUrl(I.REQUEST_DOWNLOAD_CONTACT_ALL_LIST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute(){
        executeRequest(new GsonRequest<Contact[]>(path,Contact[].class,
                responseDownloadContactListListener(),errorListener()));
    }

    private Response.Listener<Contact[]> responseDownloadContactListListener() {
        return new Response.Listener<Contact[]>() {
            @Override
            public void onResponse(Contact[] contacts) {
                Log.e(TAG,"DownloadContactList");
                if(contacts!=null){
                    Log.e(TAG,"DownloadContactList,contacts size="+contacts.length);
                    ArrayList<Contact> list = Utils.array2List(contacts);
                    ArrayList<Contact> contactList =
                            SuperWeChatApplication.getInstance().getContactList();
                    contactList.clear();
                    contactList.addAll(list);
                    HashMap<String, Contact> userList =
                            SuperWeChatApplication.getInstance().getUserList();
                    userList.clear();
                    for(Contact c:list){
                        userList.put(c.getMContactCname(),c);
                    }
                    mContext.sendStickyBroadcast(new Intent("update_contact_list"));
                }
            }
        };
    }
}
