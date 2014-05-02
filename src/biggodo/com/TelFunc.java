package biggodo.com;


import android.content.Context;
import android.telephony.TelephonyManager;

public class TelFunc {

	public static String getOperator(Context context) {
		String operator = "";
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		operator = tm.getSimOperator();
		if (operator != null) {
			if (operator.length() >= 3) {
				operator = operator.substring(0, 3);
			}
		} else {
			operator = "";
		}
		return operator;

	}

	public static String getMsisdn(Context context) {
		String ret = "";
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		ret = telephonyManager.getLine1Number();
		if(ret==null){
			ret="";
		}
		return ret;
	}

}
