package biggodo.com;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.UUID;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import biggodo.com.R;
import biz.trustpay.objects.Request;
import biz.trustpay.ui.Payments;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Biggodo extends Activity  {
	public static final String PREFS_NAME = "BigGodo Store";
	private Editor prefsEditor;
	private SharedPreferences settings;
	WebView myWebView = null;
	private ProgressDialog progressDialog = null;
	private static final String CONST_APP_ID = "ap.f0f5c5e1-ab77-4cde-aba5-f6dffd40b21c";
	
	String success = "";
	String fail = "";
	String msisdn = "";
	String mcc = "";
	String username = "";
	String pin = "";
	private boolean loadingFinished = true, redirect = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mcc = TelFunc.getOperator(this);
		settings = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		setContentView(R.layout.activity_biggodo);
		myWebView = (WebView) findViewById(R.id.webView);
		myWebView.getSettings().setJavaScriptEnabled(true);
		doCookieThing();
		myWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				boolean ret = false;
				if (url.contains("my.trustpay.biz")) {
					System.out.println("Got TrustPay:" + url);
					Uri uri = Uri.parse(url);
					String amount = uri.getQueryParameter("amount");
					String currency = uri.getQueryParameter("currency");
					String appuser = uri.getQueryParameter("appuser");
					try {
						if (appuser != null) {
							appuser = URLDecoder.decode(appuser, "UTF-8");
						}
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String txid = uri.getQueryParameter("txid");
					try {
						if (txid != null) {
							txid = URLDecoder.decode(txid, "UTF-8");
						}
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String message = uri.getQueryParameter("message");
					try {
						if (message != null) {
							message = URLDecoder.decode(message, "UTF-8");
						}
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					success = uri.getQueryParameter("success");
					fail = uri.getQueryParameter("fail");
					pay(amount, currency, false, appuser, message, txid);
					ret = true;
				}
				return ret;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				loadingFinished = false;
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setMessage(" Loading...");
				progressDialog.setCancelable(true);
				progressDialog.show();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (!redirect) {
					loadingFinished = true;
				}
				if (loadingFinished && !redirect) {
					if (!isFinishing()) {
						if (progressDialog != null) {
							if (progressDialog.isShowing()) {
								progressDialog.dismiss();
							}
						}
					}
					// finish();
				} else {
					redirect = false;
				}
			}
		});
		myWebView.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {

				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		progressDialog = new ProgressDialog(this);
		LoadSettings();

		if (msisdn.equals("")) {
			getMSISDN();
			Register();
		} else {
			Login();
		}
	}

	@Override
	public void onBackPressed() {
		if (myWebView.canGoBack())
			myWebView.goBack();
		else
			super.onBackPressed();
	}

	public void Register() {
		CmsRegister cmsregister = new CmsRegister(this);
		cmsregister
				.setUrl("https://my.trustpay.biz/FullTracksRegister/Register?username="
						+ username + "&msisdn=" + msisdn + "&mcc=" + mcc);
		Thread tr = new Thread(cmsregister);
		tr.start();
	}

	public void gotResponse(String response) {
		System.out.println("Got:" + response);
		try {
			JSONObject json = new JSONObject(response);
			String status = json.getString("status");
			if (status.equals("exists")) {
				getMSISDN();
				Register();
			} else {
				if (status.equals("success")) {
					pin = json.getString("pin");
					ClearSettings();
					StoreSettings();
					Login();
				} else {
					final String msg = json.getString("message");
					Biggodo.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showMessage(msg, false);
						}
					});
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void doCookieThing() {
		CookieSyncManager cookieSyncManager = CookieSyncManager
				.createInstance(myWebView.getContext());
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();
		cookieManager.setCookie("http://biggodo.m4me.mobi",
				"Domain=biggodo.m4me.mobi");
		cookieSyncManager.sync();
		String cookie = cookieManager.getCookie("http://biggodo.m4me.mobi");
		System.out.println("Cookie is:" + cookie);
	}

	public void Login() {

		Biggodo.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				/*
				System.out.println("Signing in:" + "username=" + username
						+ "&pin=" + pin + "&commit=Sign+in");
				String postData = "username=" + username + "&pin=" + pin
						+ "&commit=Sign+in";
				*/
				myWebView.postUrl("http://biggodo.m4me.mobi/q9Y/display/latest",
						EncodingUtils.getBytes("", "BASE64"));
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_biggodo, menu);
		return true;
	}
	
	public void pay(String amount, String currency, boolean isTest,
		String appuser, String txDescription, String txid) {
		Request request = new Request();
		request.setAmount(amount);
		request.setApp_id(CONST_APP_ID);
		request.setApp_ref(txid);
		request.setApp_user("BigGodo User");
		request.setCurrency(currency);
		request.setTx_description(txDescription);
		request.setSimoperator(mcc);
		if (isTest) {
			request.setIstest(true);
		} else {
			request.setIstest(false);
		}
		Intent intent = new Intent(this, Payments.class);
		Bundle mBundle = new Bundle();
		mBundle.putSerializable("request", request);
		intent.putExtras(mBundle);
		startActivityForResult(intent, 123);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 123) {
			if (resultCode == Activity.RESULT_OK) {
				
				myWebView.loadUrl(success);

			} else if (resultCode == Activity.RESULT_CANCELED) {
				if (data != null) {
					myWebView.loadUrl(fail);
				}
			}
		} 
	}

	public void showMessage(final String msg) {

		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(msg);

		alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {

			}
		});

		if (!isFinishing()) {
			alertbox.show();
		}
	}

	public void ClearSettings() {
		prefsEditor = settings.edit();
		prefsEditor.clear();
		prefsEditor.commit();
	}

	public void StoreSettings() {
		prefsEditor = settings.edit();
		prefsEditor.putString("MSISDN", msisdn);
		prefsEditor.putString("USERNAME", username);
		prefsEditor.putString("PIN", pin);

		prefsEditor.commit();
	}

	public void LoadSettings() {

		msisdn = settings.getString("MSISDN", "");
		pin = settings.getString("PIN", "");
		username = settings.getString("USERNAME", "");
	}

	public void getMSISDN() {
		msisdn = "0";
		username = UUID.randomUUID().toString();
		username = username.replaceAll("-", "");
	}

	public void showMessage(final String msg, final boolean finish) {

		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(msg);

		alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				if (finish) {
					finish();
				}
			}
		});
		if (!isFinishing()) {
			alertbox.show();
		}
	}

}
