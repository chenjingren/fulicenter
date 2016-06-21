/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.exceptions.EaseMobException;

import java.io.File;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.bean.Group;
import cn.ucai.superwechat.bean.Message;
import cn.ucai.superwechat.bean.User;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.listener.OnSetAvatarListener;
import cn.ucai.superwechat.utils.ImageUtils;
import cn.ucai.superwechat.utils.Utils;

public class NewGroupActivity extends BaseActivity {
    public static final String TAG = NewGroupActivity.class.getName();
	private EditText groupNameEditText;
	private ProgressDialog progressDialog;
	private EditText introductionEditText;
	private CheckBox checkBox;
	private CheckBox memberCheckbox;
	private LinearLayout openInviteContainer;
    static final int ACTION_CREATE_GROUP = 100;
    NewGroupActivity mContext;
    OnSetAvatarListener mOnSetAvatarListener;
    ImageView mivAvatar;
    String avatarName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_group);
        mContext = this;
		

        initView();
        setListener();
    }

    private void setListener() {
        setOnCheckchangedListener();
        setSaveGroupClickListener();
        setGroupIconClickListener();
    }

    private void setGroupIconClickListener() {
        findViewById(R.id.layout_group_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnSetAvatarListener = new OnSetAvatarListener(mContext,R.id.layout_new_group,getGroupAvatarName(), I.AVATAR_TYPE_GROUP_PATH);
            }
        });
    }

    private String getGroupAvatarName() {
        avatarName = System.currentTimeMillis()+"";
        return avatarName;
    }

    private void setOnCheckchangedListener() {
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    openInviteContainer.setVisibility(View.INVISIBLE);
                }else{
                    openInviteContainer.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initView() {
        groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
        introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
        checkBox = (CheckBox) findViewById(R.id.cb_public);
        memberCheckbox = (CheckBox) findViewById(R.id.cb_member_inviter);
        openInviteContainer = (LinearLayout) findViewById(R.id.ll_open_invite);
        mivAvatar = (ImageView) findViewById(R.id.iv_avatar);
    }

    public void setSaveGroupClickListener() {
		findViewById(R.id.btnSaveGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str6 = getResources().getString(R.string.Group_name_cannot_be_empty);
                String name = groupNameEditText.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Intent intent = new Intent(mContext, AlertDialog.class);
                    intent.putExtra("msg", str6);
                    startActivity(intent);
                } else {
                    // 进通讯录选人
                    startActivityForResult(new Intent(mContext,
                            GroupPickContactsActivity.class).putExtra("groupName", name),
                            ACTION_CREATE_GROUP);
                }
            }
        });
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK){
            return;
        }
        if(requestCode == ACTION_CREATE_GROUP){
            createNewGroup(data);
        }else{
            mOnSetAvatarListener.setAvatar(requestCode,data,mivAvatar);
        }

	}

    private void createNewGroup(final Intent data) {
        setProgressDialog();
        final String st2 = getResources().getString(R.string.Failed_to_create_groups);
        //新建群组
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 调用sdk创建群组方法
                String groupName = groupNameEditText.getText().toString().trim();
                String desc = introductionEditText.getText().toString();
                Contact[] contacts = (Contact[]) data.getSerializableExtra("newmembers");
                String[] members = null;
                if (contacts != null && contacts.length > 0) {
                    members = new String[contacts.length];
                    for (int i = 0; i < contacts.length; i++) {
                        members[i] = contacts[i].getMContactCname();
                    }
                }
                EMGroup emGroup;
                try {
                    if (checkBox.isChecked()) {
                        //创建公开群，此种方式创建的群，可以自由加入
                        //创建公开群，此种方式创建的群，用户需要申请，等群主同意后才能加入此群
                        emGroup = EMGroupManager.getInstance().createPublicGroup(groupName, desc, members, true, 200);
                    } else {
                        //创建不公开群
                        emGroup = EMGroupManager.getInstance().createPrivateGroup(groupName, desc, members, memberCheckbox.isChecked(), 200);
                    }
                    createGroupAppServer(emGroup.getGroupId(), groupName, desc, contacts);
                } catch (final EaseMobException e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(NewGroupActivity.this, st2 + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void createGroupAppServer(String hxid, String groupName, String desc, final Contact[] contacts) {
        //注册环信的服务器 registerEMServer
        //先注册本地的服务器并上传头像 REQUEST_CREATE_GROUP -->okhttp
        //添加群成员
        boolean isPublic = checkBox.isChecked();
        boolean isExam = !memberCheckbox.isChecked();
        File file = new File(ImageUtils.getAvatarPath(activity, I.AVATAR_TYPE_GROUP_PATH),
                avatarName + I.AVATAR_SUFFIX_JPG);
        User user = SuperWeChatApplication.getInstance().getUser();
        OkHttpUtils<Group> utils = new OkHttpUtils<Group>();
        utils.url(SuperWeChatApplication.SERVER_ROOT)//设置服务端根地址
                .addParam(I.KEY_REQUEST, I.REQUEST_CREATE_GROUP)//添加上传的请求参数
                .addParam(I.Group.HX_ID,hxid)
                .addParam(I.Group.NAME,groupName)
                .addParam(I.Group.DESCRIPTION,desc)
                .addParam(I.Group.OWNER,user.getMUserName())
                .addParam(I.Group.IS_PUBLIC,isPublic+"")
                .addParam(I.Group.ALLOW_INVITES,isExam+"")
                .addParam(I.User.USER_ID,user.getMUserId()+"")
                .targetClass(Group.class)//设置服务端返回json数据的解析类型
                .addFile(file)//添加上传的文件
                .execute(new OkHttpUtils.OnCompleteListener<Group>() {//执行请求，并处理返回结果
                    @Override
                    public void onSuccess(Group group) {
                        if(group.isResult()){
                            if(contacts!=null) {
                                addGroupMembers(group, contacts);
                            }else{
                                SuperWeChatApplication.getInstance().getGroupList().add(group);
                                Intent intent = new Intent("update_group_list").putExtra("group",group);
                                setResult(RESULT_OK,intent);
                                progressDialog.dismiss();
                                Utils.showToast(mContext,R.string.Create_groups_Success,Toast.LENGTH_SHORT);
                                finish();
                            }
                        }else{
                            progressDialog.dismiss();
                            Utils.showToast(mContext,Utils.getResourceString(mContext,group.getMsg()),Toast.LENGTH_SHORT);
                            Log.e(TAG, Utils.getResourceString(mContext,group.getMsg()));
                        }
                    }

                    @Override
                    public void onError(String error) {
                        progressDialog.dismiss();
                        Utils.showToast(mContext,R.string.Failed_to_create_groups,Toast.LENGTH_SHORT);
                        Log.e(TAG, error);
                    }
                });
    }

    private void addGroupMembers(Group group,Contact[] members) {
        try {
            String userIds="";
            String userNames="";
            for(int i=0;i<members.length;i++){
                userIds+=members[i].getMContactCid()+",";
                userNames+=members[i].getMContactCname()+",";
            }
            String path = new ApiParams()
                    .with(I.Member.GROUP_HX_ID,group.getMGroupHxid())
                    .with(I.Member.USER_ID,userIds)
                    .with(I.Member.USER_NAME,userNames)
                    .getRequestUrl(I.REQUEST_ADD_GROUP_MEMBERS);
            Log.e(TAG,"path = "+ path);
            executeRequest(new GsonRequest<Message>(path, Message.class,
                    responseListener(group), errorListener()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<Message> responseListener(final Group group) {
        return new Response.Listener<Message>() {
            @Override
            public void onResponse(Message message) {
                if(message.isResult()){
                    progressDialog.dismiss();
                    Utils.showToast(mContext,Utils.getResourceString(mContext,I.MSG_GROUP_CREATE_SCUUESS),Toast.LENGTH_LONG);
                    SuperWeChatApplication.getInstance().getGroupList().add(group);
                    Intent intent = new Intent("update_group_list").putExtra("group",group);
                    Utils.showToast(mContext,Utils.getResourceString(mContext,group.getMsg()),Toast.LENGTH_SHORT);
                    setResult(RESULT_OK,intent);
                } else {
                    progressDialog.dismiss();
                    Utils.showToast(mContext,R.string.Failed_to_create_groups,Toast.LENGTH_SHORT);
                }
                finish();
            }
        };
    }

    private void setProgressDialog() {
        String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(st1);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void back(View view) {
		finish();
	}
}
