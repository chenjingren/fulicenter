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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.NetworkImageView;
import com.easemob.chat.EMContactManager;

import java.util.HashMap;

import cn.ucai.superwechat.DemoHXSDKHelper;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.applib.controller.HXSDKHelper;
import cn.ucai.superwechat.bean.Contact;
import cn.ucai.superwechat.bean.User;
import cn.ucai.superwechat.data.ApiParams;
import cn.ucai.superwechat.data.GsonRequest;
import cn.ucai.superwechat.utils.UserUtils;

public class AddContactActivity extends BaseActivity{
	private EditText editText;
	private LinearLayout searchedUserLayout;
	private TextView nameText,mTextView;
	private Button searchBtn;
	private NetworkImageView avatar;
	private InputMethodManager inputMethodManager;
	private String toAddUsername;
	private ProgressDialog progressDialog;

    
    private TextView mtvNothing;
    Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_contact);
        mContext = this;
		mTextView = (TextView) findViewById(R.id.add_list_friends);
		
		editText = (EditText) findViewById(R.id.edit_note);
		String strAdd = getResources().getString(R.string.add_friend);
		mTextView.setText(strAdd);
		String strUserName = getResources().getString(R.string.user_name);
		editText.setHint(strUserName);
		searchedUserLayout = (LinearLayout) findViewById(R.id.ll_user);
		nameText = (TextView) findViewById(R.id.name);
		searchBtn = (Button) findViewById(R.id.search);
		avatar = (NetworkImageView) findViewById(R.id.avatar);
        mtvNothing = (TextView) findViewById(R.id.tv_show_nothing);
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		setListener();
	}

	private void setListener() {
		setSearchContactListener();
		setAddContactListener();
	}
	
	
	/**
	 * 查找contact
	 */
	public void setSearchContactListener() {
		findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = editText.getText().toString();

                if(TextUtils.isEmpty(name)) {
                    String st = getResources().getString(R.string.Please_enter_a_username);
                    startActivity(new Intent(mContext, AlertDialog.class).putExtra("msg", st));
                    return;
                }

                if(SuperWeChatApplication.getInstance().getUserName().equals(nameText.getText().toString())){
                    String str = getString(R.string.not_add_myself);
                    startActivity(new Intent(mContext, AlertDialog.class).putExtra("msg", str));
                    return;
                }
                toAddUsername = name;

                try {
                    String path = new ApiParams()
                            .with(I.User.USER_NAME,toAddUsername)
                            .getRequestUrl(I.REQUEST_FIND_USER);
                    executeRequest(new GsonRequest<User>(path, User.class,
                            responseFindUserListener(),errorListener()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

	}

    private Response.Listener<User> responseFindUserListener() {
        return new Response.Listener<User>() {
            @Override
            public void onResponse(User user) {
                if(user!=null){
                    mtvNothing.setVisibility(View.GONE);
                    HashMap<String, Contact> userList =
                            SuperWeChatApplication.getInstance().getUserList();
                    if (userList.containsKey(user.getMUserName())){
                        Intent intent = new Intent();
                        intent.setClass(mContext, UserProfileActivity.class);
                        intent.putExtra("username", user.getMUserName());
                        mContext.startActivity(intent);
                    } else {
                        //服务器存在此用户，显示此用户和添加按钮
                        searchedUserLayout.setVisibility(View.VISIBLE);
                        UserUtils.setUserBeanAvatar(user,avatar);
                        UserUtils.setUserBeanNick(user,nameText);
                    }
                } else {
                    mtvNothing.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    /**
	 *  添加contact
	 */
	public void setAddContactListener(){
        findViewById(R.id.indicator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(((DemoHXSDKHelper) HXSDKHelper.getInstance()).getContactList().containsKey(nameText.getText().toString())){
                    //提示已在好友列表中，无需添加
                    if(EMContactManager.getInstance().getBlackListUsernames().contains(nameText.getText().toString())){
                        startActivity(new Intent(mContext, AlertDialog.class).putExtra("msg", "此用户已是你好友(被拉黑状态)，从黑名单列表中移出即可"));
                        return;
                    }
                    String strin = getString(R.string.This_user_is_already_your_friend);
                    startActivity(new Intent(mContext, AlertDialog.class).putExtra("msg", strin));
                    return;
                }

                progressDialog = new ProgressDialog(mContext);
                String stri = getResources().getString(R.string.Is_sending_a_request);
                progressDialog.setMessage(stri);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                new Thread(new Runnable() {
                    public void run() {

                        try {
                            //demo写死了个reason，实际应该让用户手动填入
                            String s = getResources().getString(R.string.Add_a_friend);
                            EMContactManager.getInstance().addContact(toAddUsername, s);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();
                                    String s1 = getResources().getString(R.string.send_successful);
                                    Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (final Exception e) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    progressDialog.dismiss();
                                    String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                                    Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

	}
	
	public void back(View v) {
		finish();
	}
}
