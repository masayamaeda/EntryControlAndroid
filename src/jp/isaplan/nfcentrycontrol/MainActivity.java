package jp.isaplan.nfcentrycontrol;

import java.util.HashMap;
import java.util.Map;

import jp.isaplan.nfcentrycontrol.bean.ResultMessage;
import jp.isaplan.nfcentrycontrol.bean.UserInfo;
import jp.isaplan.nfcentrycontrol.gsonrequest.GsonRequest;
import jp.isaplan.nfcentrycontrol.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

public class MainActivity extends Activity {

	private final int MODE_ATTENDANCE = 0;
	private final int MODE_LEAVING = 1;
	
	private String TAG = "MainActivity";

    private NfcAdapter mNfcAdapter;
    private TextView mTextViewUserName;
    private RequestQueue mQueue;
    private String mCardId;
    private int mUserId;
    private String mUserName;
    private int mMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTextViewUserName = ((TextView)findViewById(R.id.textView1));
		mTextViewUserName.setText("");
        
        // 「出社」ボタン
		Button buttonAttendance = (Button) findViewById(R.id.button1);
        buttonAttendance.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickAttendLeaving(MODE_ATTENDANCE);
			}
		});

        // 「退社」ボタン
		Button buttonLeaving = (Button) findViewById(R.id.button2);
		buttonLeaving.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickAttendLeaving(MODE_LEAVING);
			}
		});

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		Intent intent = this.getIntent();
		if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
			mCardId = getCardId(intent);
			requestUserInfo(mCardId);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mNfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
			mCardId = getCardId(intent);
			requestUserInfo(mCardId);
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		try {
			intentFilter.addDataType("text/plain");
		}
		catch (MalformedMimeTypeException e) {
			e.printStackTrace();
		}
		IntentFilter[] mIntentFilter = new IntentFilter[] { intentFilter };
		mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilter, null);
	}

	/**
	 * ユーザー情報取得APIを呼び出す
	 * @param userId
	 */
	private void requestUserInfo(String userId) {
        Log.d(TAG, "requestUserInfo");
        Log.d(TAG, "userId:" + userId);
        mQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.1.66:8080/entrycontrol/api/getuserinfo";
        // 送信したいパラメーター
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", userId);
        // リクエストの初期設定
        GsonRequest<UserInfo> myRequest = new GsonRequest<UserInfo>(url, params,
        		UserInfo.class, getUserInfoListener, myErrorListener);
        // リクエストキューにリクエスト追加
        mQueue.add(myRequest);
	}

    /**
     * レスポンス受信のリスナー
     */
    private Listener<UserInfo> getUserInfoListener = new Listener<UserInfo>() {
		@Override
		public void onResponse(UserInfo response) {
            Log.d(TAG, "onResponse");
            if (response != null) {
            	if (response.getId() == 0) {
	            	Intent intent = new Intent();
	            	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	                intent.setClassName("jp.isaplan.nfcentrycontrol","jp.isaplan.nfcentrycontrol.ConfirmRegistActivity");
	                intent.putExtra("CardID", mCardId);
	                finish();
	                startActivity(intent);
            	}
            	else {
            		mUserId = response.getId();
            		mUserName = response.getName();
            		mTextViewUserName.setText(mUserName);
            	}
            }
       	}
    };

    /**
     * リクエストエラーのリスナー
     */
    private ErrorListener myErrorListener = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.getMessage());
        }
    };

    /**
     * 「出社」「退社」ボタン押下時処理
     * @param mode
     */
    private void onClickAttendLeaving(int mode) {
    	mMode = mode;
        Log.d(TAG, "requestUserInfo");
        mQueue = Volley.newRequestQueue(getApplicationContext());
        // WebAPIのURL
        String url = "http://192.168.1.66:8080/entrycontrol/api/recordtime";
        // 送信パラメーター
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", String.valueOf(mUserId));	// ユーザーID
        params.put("card_id", mCardId);					// カードID
        params.put("kind", String.valueOf(mode));		// 出社/退社
        // リクエストの初期設定
        GsonRequest<ResultMessage> myRequest = new GsonRequest<ResultMessage>(url, params,
        		ResultMessage.class, recordTimeListener, myErrorListener);
        // リクエストキューにリクエスト追加
        mQueue.add(myRequest);
    }

    /**
     * 時間登録APIのレスポンス受信のリスナー
     */
    private Listener<ResultMessage> recordTimeListener = new Listener<ResultMessage>() {
		@Override
		public void onResponse(ResultMessage response) {
			Log.d(TAG, "onResponse");
			String dispMessage = "";
			// レスポンスメッセージが存在するか確認する
			if (!response.getMessage().isEmpty()) {
				// エラーメッセージを取得
				dispMessage = response.getMessage();
			}
			else {
				if (mMode == MODE_ATTENDANCE) {
					dispMessage = String.format(getString(R.string.dialog_label_attendance_hello), mUserName);
				}
				else {
					dispMessage = String.format(getString(R.string.dialog_rleaving_office_no_attendance), mUserName);
				}
			}
			new AlertDialog.Builder(MainActivity.this)
			.setTitle(dispMessage)
			.setPositiveButton(
			getString(R.string.button_label_ok),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
		            finish();
				} 
			})
			.show();
       	}
    };

    /**
     * カードID文字列取得
     * @param intent NFCダグ読み取りのインテント
     * @return カードID文字列
     */
    private String getCardId(Intent intent) {
		StringBuilder sb = new StringBuilder();
		byte[] cardId = new byte[] { 0 };
	    Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    if (tag != null) {
	        cardId = tag.getId();
			for (int i = 0; i < cardId.length; i++) {
				if (i > 0) {
					sb.append(new String(":"));
				}
				int value = cardId[i];
				sb.append(new String(String.format("%02X", value & 0xFF)));
			}
		}
	    return new String(sb);
	}
}
