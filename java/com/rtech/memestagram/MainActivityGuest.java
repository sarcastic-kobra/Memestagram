package com.rtech.memestagram;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivityGuest extends AppCompatActivity {
	
	private DrawerLayout drawerLayout;
	private ImageView imgMenu;
	private NavigationView navViewGuest;
	private View navHeader;
	private NavController navController;
	private TextView textTitle, headermemeCounter;
	private FirebaseUser firebaseUser;
	private DatabaseReference memeRef;
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_main_guest);
		
		drawerLayout = findViewById (R.id.DrawerLayout);
		
		imgMenu = findViewById (R.id.ImageMenu);
		
		imgMenu.setOnClickListener (v -> drawerLayout.openDrawer (GravityCompat.START));
		
		navViewGuest = findViewById (R.id.NavigationViewGuest);
		navViewGuest.setItemIconTintList (null);
		
		navHeader = navViewGuest.getHeaderView (0);
		
		headermemeCounter = navHeader.findViewById (R.id.total_memes);
		
		navController = Navigation.findNavController (this, R.id.NavHostFragmentGuest);
		NavigationUI.setupWithNavController (navViewGuest, navController);
		
		textTitle = findViewById (R.id.TextTitle);
		
		navController.addOnDestinationChangedListener ((controller, destination, arguments) -> textTitle.setText (destination.getLabel ()));
		
		firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
		
		if (firebaseUser != null) {
			
			Intent LoggedInIntent = new Intent (MainActivityGuest.this, MainActivityUser.class);
			startActivity (LoggedInIntent);
			finish ();
		
		}
		
		memeRef = FirebaseDatabase.getInstance ().getReference ("Memes");
		
		memeRef.child ("Counter").addListenerForSingleValueEvent (new ValueEventListener () {
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot) {
				
				headermemeCounter.setText ("Total Memes : " + snapshot.getValue (Long.class));
				
			}
			
			@Override
			public void onCancelled (@NonNull DatabaseError error) {
			
			}
		});
		
	}
	
}