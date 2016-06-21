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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;

import java.io.File;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.Message;
import cn.ucai.superwechat.data.OkHttpUtils;
import cn.ucai.superwechat.listener.OnSetAvatarListener;
import cn.ucai.superwechat.utils.ImageUtils;
import cn.ucai.superwechat.utils.Utils;

/**
 * 注册页
 * 
 */
public class RegisterActivity extends BaseActivity {
	public static final String TAG = RegisterActivity.class.getName();
	RegisterActivity mContext;
	private EditText userNameEditText;
	private EditText userNickEditText;
	private EditText passwordEditText;
	private EditText confirmPwdEditText;
	private ImageView mivAvatar;

	ProgressDialog pd;

    OnSetAvatarListener mOnSetAvatarListener;

    String username;
    String nick;
    String pwd;
	String avatarName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		mContext = this;
		initView();
        setListener();
    }

    private void setListener() {
		setLoginClickListener();
        setRegisterClickListener();
        setAvatarClickListener();
    }

    private void setAvatarClickListener() {
        findViewById(R.id.layout_user_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnSetAvatarListener = new OnSetAvatarListener(mContext,R.id.layout_register,getUserName(), I.AVATAR_TYPE_USER_PATH);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){
            return;
        }
        mOnSetAvatarListener.setAvatar(requestCode,data,mivAvatar);
    }

    private String getUserName() {
        avatarName = System.currentTimeMillis()+"";
        return avatarName;
    }

    private void setLoginClickListener() {
        findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        userNameEditText = (EditText) findViewById(R.id.etUserName);
        userNickEditText = (EditText) findViewById(R.id.etNick);
        passwordEditText = (EditText) findViewById(R.id.etPassword);
        confirmPwdEditText = (EditText) findViewById(R.id.etConfirmPassword);
        mivAvatar = (ImageView) findViewById(R.id.iv_avatar);
    }

    /**
	 * 注册
     *
     */
	private void setRegisterClickListener() {
        findViewById(R.id.btnRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = userNameEditText.getText().toString().trim();
                nick = userNickEditText.getText().toString().trim();
                pwd = passwordEditText.getText().toString().trim();
                String confirm_pwd = confirmPwdEditText.getText().toString().trim();
                if (TextUtils.isEmpty(username)) {
                    userNameEditText.requestFocus();
                    userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_empty));
                    return;
                } else if (!username.matches("[\\w][\\w\\d_]+")) {
                    userNameEditText.requestFocus();
                    userNameEditText.setError(getResources().getString(R.string.User_name_cannot_be_wd));
                    return;
                } else if (TextUtils.isEmpty(nick)) {
                    userNickEditText.requestFocus();
                    userNickEditText.setError(getResources().getString(R.string.Nick_name_cannot_be_empty));
                    return;
                } else if (TextUtils.isEmpty(pwd)) {
                    passwordEditText.requestFocus();
                    passwordEditText.setError(getResources().getString(R.string.Password_cannot_be_empty));
                    return;
                } else if (TextUtils.isEmpty(confirm_pwd)) {
                    confirmPwdEditText.requestFocus();
                    confirmPwdEditText.setError(getResources().getString(R.string.Confirm_password_cannot_be_empty));
                    return;
                } else if (!pwd.equals(confirm_pwd)) {
                    confirmPwdEditText.requestFocus();
                    confirmPwdEditText.setError(getResources().getString(R.string.Two_input_password));
                    return;
                }

                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(pwd)) {
                    pd = new ProgressDialog(mContext);
                    pd.setMessage(getResources().getString(R.string.Is_the_registered));
                    pd.show();

                    registerAppServer();

                }
            }
        });
	}

    private void registerAppServer() {
        //先注册本地的服务器并上传头像 REQUEST_REGISTER -->okhttp
        //注册环信的服务器 registerEMServer
        //如果环信的服务器注册失败，删除服务器上面的账号和头像 unRegister-->okhttp
        File file = new File(ImageUtils.getAvatarPath(mContext,I.AVATAR_TYPE_USER_PATH),
                avatarName + I.AVATAR_SUFFIX_JPG);
        OkHttpUtils<Message> utils = new OkHttpUtils<Message>();
        utils.url(SuperWeChatApplication.SERVER_ROOT)//设置服务端根地址
                .addParam(I.KEY_REQUEST, I.REQUEST_REGISTER)//添加上传的请求参数
                .addParam(I.User.USER_NAME, username)//添加用户的账号
                .addParam(I.User.NICK,nick)//添加用户的昵称
                .addParam(I.User.PASSWORD,pwd)//添加用户的密码
                .targetClass(Message.class)//设置服务端返回json数据的解析类型
                .addFile(file)//添加上传的文件
                .execute(new OkHttpUtils.OnCompleteListener<Message>() {
                    @Override
                    public void onSuccess(Message result) {
                        if(result.isResult()){
                            registerEMServer();
                        } else {
                            Utils.showToast(mContext,Utils.getResourceString(mContext,result.getMsg()),Toast.LENGTH_SHORT);
                            pd.dismiss();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        pd.dismiss();
                        Log.e(TAG,"register fail,error:" + error);
                    }
                });

    }

    private void unRegister() {
        OkHttpUtils<Message> utils = new OkHttpUtils<Message>();
        utils.url(SuperWeChatApplication.SERVER_ROOT)//设置服务端根地址
                .addParam(I.KEY_REQUEST, I.REQUEST_UNREGISTER)//添加上传的请求参数
                .addParam(I.User.USER_NAME, username)//添加用户的账号
                .targetClass(Message.class)//设置服务端返回json数据的解析类型
                .execute(new OkHttpUtils.OnCompleteListener<Message>() {//执行请求，并处理返回结果
                    @Override
                    public void onSuccess(Message result) {
                        pd.dismiss();
                        Utils.showToast(mContext,R.string.Registration_failed,Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onError(String error) {
                        pd.dismiss();
                        Log.e(TAG, error);
                    }
                });
    }

    private void registerEMServer() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // 调用sdk注册方法
                    EMChatManager.getInstance().createAccountOnServer(username, pwd);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegisterActivity.this.isFinishing())
                                pd.dismiss();
                            // 保存用户名
                            SuperWeChatApplication.getInstance().setUserName(username);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } catch (final EaseMobException e) {
                    unRegister();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (!RegisterActivity.this.isFinishing())
                                pd.dismiss();
                            int errorCode=e.getErrorCode();
                            if(errorCode==EMError.NONETWORK_ERROR){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                            }else if(errorCode == EMError.USER_ALREADY_EXISTS){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                            }else if(errorCode == EMError.UNAUTHORIZED){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                            }else if(errorCode == EMError.ILLEGAL_USER_NAME){
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name),Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        }).start();
    }

	public void back(View view) {
		finish();
	}

}
