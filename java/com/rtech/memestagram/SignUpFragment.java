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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUpFragment extends Fragment {
	
	private ViewGroup SignUpViewGrp;
	private EditText usernameSignup, emailSignup, passwordSignup;
	private Button signupBtn;
	private String UID;
	private DatabaseReference usersRef;
	private ProgressBar signUpBar;
	
	public SignUpFragment () {
	
	}
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		
		super.onCreate (savedInstanceState);
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		SignUpViewGrp = (ViewGroup) inflater.inflate (R.layout.fragment_sign_up, container, false);
		
		usernameSignup = SignUpViewGrp.findViewById (R.id.username_signup);
		emailSignup = SignUpViewGrp.findViewById (R.id.email_signup);
		passwordSignup = SignUpViewGrp.findViewById (R.id.password_signup);
		
		signupBtn = SignUpViewGrp.findViewById (R.id.button_signup);
		
		signUpBar = SignUpViewGrp.findViewById (R.id.progressBarSignup);
		
		signUpBar.setVisibility (View.INVISIBLE);
		
		usersRef = FirebaseDatabase.getInstance ().getReference ("Users");
		
		signupBtn.setOnClickListener (v -> {
			
			signUpBar.setVisibility (View.VISIBLE);
			
			signupBtn.setVisibility (View.INVISIBLE);
			
			if (usernameSignup.getText ().toString ().isEmpty ()) {
				
				usernameSignup.setError ("Username is Needed");
				
				signUpBar.setVisibility (View.INVISIBLE);
				
				signupBtn.setVisibility (View.VISIBLE);
				
				return;
				
			}
			
			if (emailSignup.getText ().toString ().trim ().isEmpty ()) {
				
				emailSignup.setError ("Email Id Needed");
				
				signUpBar.setVisibility (View.INVISIBLE);
				
				signupBtn.setVisibility (View.VISIBLE);
				
				return;
				
			}
			
			if (passwordSignup.getText ().toString ().trim ().isEmpty ()) {
				
				passwordSignup.setError ("Password Needed");
				
				signUpBar.setVisibility (View.INVISIBLE);
				
				signupBtn.setVisibility (View.VISIBLE);
				
				return;
				
			}
			
			if (passwordSignup.getText ().toString ().trim ().length () < 6) {
				
				passwordSignup.setError ("Password Too Short");
				
				signUpBar.setVisibility (View.INVISIBLE);
				
				signupBtn.setVisibility (View.VISIBLE);
				
				return;
				
			}
			
			FirebaseAuth.getInstance ().createUserWithEmailAndPassword (emailSignup.getText ().toString ().trim (),
					passwordSignup.getText ().toString ().trim ()).addOnCompleteListener (task -> {
						
						if (task.isSuccessful ()) {
							
							UID =
									Objects.requireNonNull (FirebaseAuth.getInstance ().getCurrentUser ()).getUid ();
							
							usersRef.child (UID).child ("Username").setValue (usernameSignup.getText ().toString ().trim ());
							usersRef.child (UID).child ("Email").setValue (emailSignup.getText ().toString ().trim ());
							
							Intent LoggedInIntent = new Intent (getActivity (),
									MainActivityUser.class);
							startActivity (LoggedInIntent);
							
						} else {
							
							Toast.makeText (getActivity (), "Failed To Create User",
									Toast.LENGTH_SHORT).show ();
							
						}
						
						signUpBar.setVisibility (View.INVISIBLE);
						
						signupBtn.setVisibility (View.VISIBLE);
						
					});
			
		});
		
		return SignUpViewGrp;
		
	}
	
}