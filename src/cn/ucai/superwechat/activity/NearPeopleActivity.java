package cn.ucai.superwechat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatApplication;
import cn.ucai.superwechat.bean.User;
import cn.ucai.superwechat.utils.ImageLoader;
import cn.ucai.superwechat.utils.UserUtils;
import cn.ucai.superwechat.utils.Utils;

public class NearPeopleActivity extends BaseActivity {
    NearPeopleActivity mInstance;
    
    /** 百度定位*/
    LocationClient mLocationClient;
    /** 当前位置*/
    BDLocation mCurrentLocation;
    /** 位置监听器*/
    MyLocationListener mLocationListener;
    /** 网络、key错误的广播监听*/
    BaiDuSDKReceiver mReceiver;
    ListView mlvNearPeople;
    NearPeopleAdapter mAdapter;
    int mPageId;
    static final int PAGE_SIZE=20;
    
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        //使用百度sdk组件前初始化，注意必须在setContentView方法之前调用。
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_near_people);
        mInstance=this;
        mlvNearPeople= (ListView) findViewById(R.id.lvNearPeople);
        
        registerLocationReceiver();
        //创建位置监听器实例
        mLocationListener=new MyLocationListener();
        mLocationClient=new LocationClient(this);
        mLocationClient.registerLocationListener(mLocationListener);
    }

    /**
     * 注册百度sdk广播监听
     */
    private void registerLocationReceiver() {
        mReceiver=new BaiDuSDKReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        filter.addAction(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE);
        registerReceiver(mReceiver, filter);
    }
    
    /**
     * 位置监听器,获取当前手机最新位置
     * @author yao
     *
     */

    class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location == null){
                return;
            }
            if(mCurrentLocation!=null){
                if(mCurrentLocation.getLatitude()-location.getLatitude()<0.000001
                        && mCurrentLocation.getLongitude()-location.getLongitude()<0.000001){
                    return;
                }
            }
            mCurrentLocation = location;
            //将当前用户的位置信息上传至服务器，然后从服务器再下载所有联系人的位置信息
            User user = SuperWeChatApplication.getInstance().getUser();
            user.setMLocationLatitude(location.getLatitude());
            user.setMLocationLongitude(location.getLongitude());
            Log.i("main", "latitude:"+location.getLatitude()+",longitude:"+location.getLongitude());
//            new UploadLocationTask();
        }

        @Override
        public void onReceivePoi(BDLocation arg0) {
        }
        
    }
    
    /**
     * 百度sdk、key和网络异常监听器
     */
    class BaiDuSDKReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String errorHint = getResources().getString(R.string.Network_error);
            String keyHint = getResources().getString(R.string.please_check);
            String action =intent.getAction();
            if(action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)){
                Utils.showToast(mInstance, errorHint, 1);
            }else if(action.equals(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE)){
                Utils.showToast(mInstance, keyHint, 1);
            }
        }
        
    }
    
    /**
     * 显示附近人的适配器
     */
    class NearPeopleAdapter extends BaseAdapter{
        Context context;
        ArrayList<NearUserBean> nearUsers;
        ImageLoader imageLoader;
        User myUser;
        
        public NearPeopleAdapter(Context context, ArrayList<User> users) {
            super();
            this.context = context;
            imageLoader=ImageLoader.getInstance(context);
            myUser=SuperWeChatApplication.getInstance().getUser();
            ArrayList<NearUserBean> list = createNearUsers(users,myUser);
            this.nearUsers = new ArrayList<NearUserBean>();
            this.nearUsers.addAll(list);
        }

        /** 将UserBean集合转换为NearUserBean集合*/
        private ArrayList<NearUserBean> createNearUsers(ArrayList<User> users, User myUser) {
            ArrayList<NearUserBean> nearUsers=new ArrayList<NearUserBean>();
            for(User user:users){
                LatLng myLatLng=new LatLng(myUser.getMLocationLatitude(), myUser.getMLocationLongitude());
                LatLng contactLatLng=new LatLng(user.getMLocationLatitude(),user.getMLocationLongitude());
                int distance=(int) DistanceUtil.getDistance(myLatLng,contactLatLng);
                NearUserBean nearUser=new NearUserBean(user,distance);
                nearUsers.add(nearUser);
            }
            return nearUsers;
        }

        @Override
        public int getCount() {
           return nearUsers==null?0:nearUsers.size();
        }

        @Override
        public NearUserBean getItem(int position) {
            return nearUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, final ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null){
                holder=new ViewHolder();
                convertView=View.inflate(context, R.layout.item_near_people, null);
                holder.ivAvatar=(NetworkImageView) convertView.findViewById(R.id.ivAvatar);
                holder.tvDistance=(TextView) convertView.findViewById(R.id.tvDistance);
                holder.tvNick=(TextView) convertView.findViewById(R.id.tvNick);
                convertView.setTag(holder);
            }else{
                holder=(ViewHolder) convertView.getTag();
            }
            NearUserBean user=getItem(position);
            holder.tvNick.setText(user.getUser().getMUserNick());
            holder.tvDistance.setText(user.getDistance()+"米");
            UserUtils.setUserAvatar(UserUtils.getAvatarPath(user.getUser().getMUserName()),holder.ivAvatar);
            return convertView;
        }
        class ViewHolder{
            NetworkImageView ivAvatar;
            TextView tvNick;
            TextView tvDistance;
        }
        /** 包含与当前用户距离的Bean*/
        class NearUserBean{
            User user;
            int distance;
            public User getUser() {
                return user;
            }
            public void setUser(User user) {
                this.user = user;
            }
            public int getDistance() {
                return distance;
            }
            public void setDistance(int distance) {
                this.distance = distance;
            }
            public NearUserBean(User user, int distance) {
                super();
                this.user = user;
                this.distance = distance;
            }
            public NearUserBean() {
                super();
            }
            
        }
        
        /**
         * 将新下载的用户集合添加至原有集合
         * @param users
         */
        public void addUsers(ArrayList<User> users) {
//            ArrayList<NearUser> list = createNearUsers(users, myUser);
//            this.nearUsers.addAll(list);
//            notifyDataSetChanged();
        }
        
    }
    
    /**
     * 上传当前用户的位置
     * @author yao
     *
     */
    class UploadLocationTask extends AsyncTask<Void, Void, Boolean>{
        User user;
        
        public UploadLocationTask(User user) {
            super();
            this.user = user;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
           boolean isSuccess = false;//NetUtil.uploadLocation(user);
           if(isSuccess){
               //new DownloadLocationTask().execute();
           }
            return isSuccess;
        }
        
    }
    
    /**
     * 下载除当前用户外，所有公开位置信息的用户信息
     * @author yao
     *
     */
    class DownloadLocationTask extends AsyncTask<Void, Void, ArrayList<User>>{
        @Override
        protected ArrayList<User> doInBackground(Void... params) {
            String userName=SuperWeChatApplication.getInstance().getUserName();
            ArrayList<User> users=null;//NetUtil.downloadLocation(userName, mPageId, PAGE_SIZE);
            return users;
        }
        @Override
        protected void onPostExecute(ArrayList<User> users) {
            if(users!=null){
                if(mAdapter==null){
                    mAdapter=new NearPeopleAdapter(mInstance, users);
                    mlvNearPeople.setAdapter(mAdapter);
                }else{
                    mAdapter.addUsers(users);
                }
            }
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationClient!=null){
            mLocationClient.stop();
        }
        mCurrentLocation=null;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if(mLocationClient!=null){
            mLocationClient.start();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLocationClient!=null){
            mLocationClient.stop();
            mLocationClient=null;
            unregisterReceiver(mReceiver);
        }
    }
}
