package jp.isaplan.nfcentrycontrol;

import java.util.HashMap;
import java.util.Map;

import jp.isaplan.nfcentrycontrol.bean.UserInfo;
import jp.isaplan.nfcentrycontrol.gsonrequest.GsonRequest;
import jp.isaplan.nfcentrycontrol.R;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

public class MainActivity extends Activity {

	private String TAG = "MainActivity";

    private NfcAdapter mNfcAdapter;
    private TextView mTextViewCardId;
    private TextView mTextViewUserName;
    private RequestQueue mQueue;
    private String mCardId;
    //private String mUserId;
    //private String mUserName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTextViewCardId = ((TextView)findViewById(R.id.textView1));
		mTextViewCardId.setText("");

		mTextViewUserName = ((TextView)findViewById(R.id.textView2));
		mTextViewUserName.setText("");

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		Intent intent = this.getIntent();
		if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
			StringBuilder sb = new StringBuilder();
			getCardId(intent, sb);
			mCardId = new String(sb);

			mTextViewCardId.setText(mCardId);

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
			StringBuilder sb = new StringBuilder();
			getCardId(intent, sb);
			mCardId = new String(sb);
			mTextViewCardId.setText(mCardId);
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

	private void requestUserInfo(String userId) {
        Log.d(TAG, "requestUserInfo");
        Log.d(TAG, "userId:" + userId);
        mQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://192.168.1.66:8080/entrycontrol/getuserinfo";
        // 送信したいパラメーター
        Map<String, String> params = new HashMap<String, String>();
        params.put("id", userId);
        // リクエストの初期設定
        GsonRequest<UserInfo> myRequest = new GsonRequest<UserInfo>(url, params,
        		UserInfo.class, myListener, myErrorListener);
        // リクエストキューにリクエスト追加
        mQueue.add(myRequest);
	}

    /**
     * レスポンス受信のリスナー
     */
    private Listener<UserInfo> myListener = new Listener<UserInfo>() {
		@Override
		public void onResponse(UserInfo response) {
            Log.d(TAG, "onResponse");
            if (response != null) {
            	mTextViewUserName.setText(response.getName());
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

	private void getCardId(Intent intent, StringBuilder sb) {
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
	}
}
