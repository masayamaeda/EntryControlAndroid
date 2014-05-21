package jp.isaplan.nfcentrycontrol;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.isaplan.nfcentrycontrol.bean.ResultMessage;
import jp.isaplan.nfcentrycontrol.bean.UserInfo;
import jp.isaplan.nfcentrycontrol.gsonrequest.GsonRequest;

import org.json.JSONArray;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectCardUserActivity extends Activity {

	private String TAG = "SelectCardUserActivity";
	
    private RequestQueue mQueue;
    
    private String mCardId;
    private String mRegistUserName;
    
    private ListView mUserListView;
    private List<UserInfo> mUserInfoList = null;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_card_user);
		
        mCardId = "";
        
        Intent intent = getIntent();
        if (intent != null) {
        	mCardId = intent.getStringExtra("CardID");
        }
        requestAllUser();
        
        mUserListView = (ListView) findViewById(R.id.listView1);
        
        //リスト項目がクリックされた時の処理
        mUserListView.setOnItemClickListener(selectUserListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void requestAllUser() {
        Log.d(TAG, "requestAllUser");
        // ユーザー一覧取得APIを呼び出す
        mQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.1.66:8080/entrycontrol/getalluser";
        // リクエストの初期設定
        JsonArrayRequest myRequest = new JsonArrayRequest(url, getAllUserResponseListener, getAllUserErrorListener);
        // リクエストキューにリクエスト追加
        mQueue.add(myRequest);
	}

    /**
     * ユーザー一覧取得APIレスポンス受信のリスナー
     */
    private Listener<JSONArray> getAllUserResponseListener = new Listener<JSONArray>() {
		@Override
		public void onResponse(JSONArray response) {
            Log.d(TAG, "onResponse");
            if (response != null) {
            	
            	// レスポンスのJSONArrayをListに変換する
            	Gson gson = new Gson();
            	Type collectionType = new TypeToken<Collection<UserInfo>>(){}.getType();
            	List<UserInfo> members = gson.fromJson(response.toString(),
            	        collectionType);
            	mUserInfoList = new ArrayList<UserInfo>();
            	List<String> userNameList = new ArrayList<String>();
            	for (UserInfo member : members) {
            		mUserInfoList.add(member);
            		userNameList.add(member.getName());
            	}
            	
            	// すべてのユーザーをListViewに表示
            	ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectCardUserActivity.this,
                        R.layout.layout_card_user_row, userNameList);
            	mUserListView.setAdapter(adapter);
            }
       	}
    };

    /**
     * ユーザー一覧取得APIエラーのリスナー
     */
    private ErrorListener getAllUserErrorListener = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.getMessage());
        }
    };
    
    private AdapterView.OnItemClickListener selectUserListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			final int selectPosition = position;
			mRegistUserName = mUserInfoList.get(position).getName();
			new AlertDialog.Builder(SelectCardUserActivity.this)
			.setTitle(String.format(getString(R.string.dialog_confirm_label_regist_user), mRegistUserName))
			.setPositiveButton(
			getString(R.string.button_label_ok),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// OK時の処理
					// カードユーザー登録APIを呼び出す
					mQueue = Volley.newRequestQueue(getApplicationContext());
			        String url = "http://192.168.1.66:8080/entrycontrol/addcard";
			        // 送信したいパラメーター
			        Map<String, String> params = new HashMap<String, String>();
			        params.put("user_id", String.valueOf(mUserInfoList.get(selectPosition).getId()));
			        params.put("card_id", mCardId);
			        // リクエストの初期設定
			        GsonRequest<ResultMessage> myRequest = new GsonRequest<ResultMessage>(url, params,
			        		ResultMessage.class, registUserResponseListener, registUserErrorListener);
			        // リクエストキューにリクエスト追加
			        mQueue.add(myRequest);
				} 
			})
			.setNegativeButton( 
			getString(R.string.button_label_cancel),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) { 
					// NO時の処理
				}
			}) 
			.show(); 
        }
    };
    
    /**
     * カードユーザー登録APIリクエストのリスナー
     */
    private Listener<ResultMessage> registUserResponseListener = new Listener<ResultMessage>() {
		@Override
		public void onResponse(ResultMessage response) {
            Log.d(TAG, "onResponse");
            String message = response.getMessage();
            if (message.isEmpty()) {
				new AlertDialog.Builder(SelectCardUserActivity.this)
				.setTitle(String.format(getString(R.string.dialog_label_regist_user), mRegistUserName))
				.setPositiveButton(
				getString(R.string.button_label_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						moveTaskToBack(true); 
					} 
				})
				.show();
            }
            else {
				new AlertDialog.Builder(SelectCardUserActivity.this)
				.setTitle(response.getMessage())
				.setPositiveButton(
				getString(R.string.button_label_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						moveTaskToBack(true); 
					} 
				})
				.show();
			}
       	}
    };

    /**
     * カードユーザー登録APIエラーのリスナー
     */
    private ErrorListener registUserErrorListener = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.getMessage());
        }
    };
}