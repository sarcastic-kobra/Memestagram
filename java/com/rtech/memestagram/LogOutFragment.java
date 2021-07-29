package com.rtech.memestagram;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class LogOutFragment extends Fragment {
	
	public LogOutFragment () {
	
	}
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		
		super.onCreate (savedInstanceState);
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		FirebaseAuth.getInstance ().signOut ();
		
		Intent SignedOutIntent = new Intent (getActivity (), MainActivityGuest.class);
		startActivity (SignedOutIntent);
		
		return inflater.inflate (R.layout.fragment_log_out, container, false);
		
	}
	
}