package jp.isaplan.nfcentrycontrol.bean;

import java.util.ArrayList;
import java.util.List;

public class Member {
	List<UserInfo> userInfo;
	
	public Member() {
		userInfo = new ArrayList<UserInfo>();
	}
}
