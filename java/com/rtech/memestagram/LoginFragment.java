package com.rtech.memestagram;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
	
	private ViewGroup LoginViewGrp;
	private EditText emailLogin, passwordLogin;
	private Button loginBtn;
	private FirebaseAuth mAuth;
	private ProgressBar LoginBar;
	
	public LoginFragment () {
	
	}
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		
		super.onCreate (savedInstanceState);
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		//Create a ViewGroup
		
		LoginViewGrp = (ViewGroup) inflater.inflate (R.layout.fragment_login, container, false);
		
		//Declare the Components
		
		emailLogin = LoginViewGrp.findViewById (R.id.email_login);
		passwordLogin = LoginViewGrp.findViewById (R.id.password_login);
		
		loginBtn = LoginViewGrp.findViewById (R.id.button_login);
		
		LoginBar = LoginViewGrp.findViewById (R.id.progressBarLogin);
		
		//Set Visibility of Components
		
		LoginBar.setVisibility (View.INVISIBLE);
		
		mAuth = FirebaseAuth.getInstance ();
		
		loginBtn.setOnClickListener (v -> {
			
			//Update the Visibility of Components
			
			LoginBar.setVisibility (View.VISIBLE);
			
			loginBtn.setVisibility (View.INVISIBLE);
			
			//Check if Email is entered
			
			if (emailLogin.getText ().toString ().trim ().isEmpty ()) {
				
				emailLogin.setError ("Email Id Needed");
				
				LoginBar.setVisibility (View.INVISIBLE);
				
				loginBtn.setVisibility (View.VISIBLE);
				
				return;
				
			}
			
			//Check if Password is entered
			
			if (passwordLogin.getText ().toString ().trim ().isEmpty ()) {
				
				passwordLogin.setError ("Password Needed");
				
				LoginBar.setVisibility (View.INVISIBLE);
				
				loginBtn.setVisibility (View.VISIBLE);
				
				return;
				
			}
			
			//Login
			
			mAuth.signInWithEmailAndPassword (emailLogin.getText ().toString ().trim (),
					passwordLogin.getText ().toString ().trim ()).addOnCompleteListener (task -> {
						
						if (task.isSuccessful ()) {
							
							//Go to User's Home
							
							Intent LoggedInIntent = new Intent (getActivity (),
									MainActivityUser.class);
							startActivity (LoggedInIntent);
							
						} else {
							
							Toast.makeText (getActivity (), "Failed to Log In", Toast.LENGTH_SHORT).show ();
							
						}
						
						//Update the Visibility of components
						
						LoginBar.setVisibility (View.INVISIBLE);
						
						loginBtn.setVisibility (View.VISIBLE);
						
					});
			
		});
		
		//Return the ViewGroup
		
		return LoginViewGrp;
		
	}
	
}