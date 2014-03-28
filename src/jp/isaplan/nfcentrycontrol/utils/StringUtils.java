package jp.isaplan.nfcentrycontrol.utils;

import java.util.Map;

public class StringUtils {
	public static String generateUrlWithGetParams(String url, Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		sb.append(new String(url));
		int count = 0;
		for (Map.Entry<String, String> e : params.entrySet()) {
		    if (count == 0) sb.append(new String("?"));
		    else sb.append(new String("&"));
		    sb.append(new String(e.getKey() + "=" + e.getValue()));
		    count++;
		}
		return new String(sb);
	}
}
