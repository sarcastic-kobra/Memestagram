package com.rtech.memestagram;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Random;

public class BrowseFragment extends Fragment {
	
	private ViewGroup BrowseViewGrp;
	private Button nextBtn, saveBtn, likeBtn, nameFull;
	private ImageView imgFull;
	private FirebaseUser firebaseUser;
	private DatabaseReference memeRef, likeRef, usersRef, savedRef;
	private StorageReference imgRefFull;
	private Random memeRandom;
	private Integer randomNum, maxNum;
	private String imgURL, UID;
	private Long likes, saves;
	private ProgressBar pBar;
	private Bitmap imgBitmap;
	
	public BrowseFragment () {
	
	}
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		
		super.onCreate (savedInstanceState);
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		//Create a ViewGroup
		
		BrowseViewGrp = (ViewGroup) inflater.inflate (R.layout.fragment_browse, container, false);
		
		//Declare Components
		
		pBar = BrowseViewGrp.findViewById (R.id.progressBar);
		
		nameFull = BrowseViewGrp.findViewById (R.id.name_full);
		
		nextBtn = BrowseViewGrp.findViewById (R.id.button_next);
		
		imgFull = BrowseViewGrp.findViewById (R.id.img_full);
		
		saveBtn = BrowseViewGrp.findViewById (R.id.saveimage);
		
		likeBtn = BrowseViewGrp.findViewById (R.id.like_meme);
		
		//Set Visibility of Components
		
		nextBtn.setVisibility (View.INVISIBLE);
		saveBtn.setVisibility (View.INVISIBLE);
		likeBtn.setVisibility (View.INVISIBLE);
		
		nameFull.setVisibility (View.INVISIBLE);
		
		//Check if anyone is logged in
		
		firebaseUser = FirebaseAuth.getInstance ().getCurrentUser ();
		
		if (firebaseUser != null) {
			
			//Get UID if anyone is logged in
			
			UID = FirebaseAuth.getInstance ().getCurrentUser ().getUid ();
			
		}
		
		//Declare Database References
		
		memeRef = FirebaseDatabase.getInstance ().getReference ("Memes");
		likeRef = FirebaseDatabase.getInstance ().getReference ("Likes");
		usersRef = FirebaseDatabase.getInstance ().getReference ("Users");
		savedRef = FirebaseDatabase.getInstance ().getReference ("Saved");
		
		//Get Count of Total Memes
		
		memeRef.child ("Counter").addListenerForSingleValueEvent (new ValueEventListener () {
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot) {
				
				maxNum = Integer.valueOf (String.valueOf (snapshot.getValue (Long.class)));
				
				//Get a random meme
				
				GetRandomMemes();
				
			}
			
			@Override
			public void onCancelled (@NonNull DatabaseError error) {
			
			}
		});
		
		//Button to show Next Meme
		
		nextBtn.setOnClickListener (v -> memeRef.child ("Counter").addListenerForSingleValueEvent (new ValueEventListener () {
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot) {
				
				maxNum = Integer.valueOf (String.valueOf (snapshot.getValue (Long.class)));
				
				//Get a random meme
				
				GetRandomMemes();
				
			}
			
			@Override
			public void onCancelled (@NonNull DatabaseError error) {
			
			}
		}));
		
		//Button to save the Meme on Screen
		
		saveBtn.setOnClickListener (v -> {
			
			imgFull.setDrawingCacheEnabled (true);
			
			nameFull.setDrawingCacheEnabled (true);
			
			if (firebaseUser != null) {
				
				//User is Logged In
				
				//Check if Meme is Saved by any User
				
				savedRef.child (String.valueOf (randomNum)).child ("Saved")
				        .addListenerForSingleValueEvent (new ValueEventListener () {
					@Override
					public void onDataChange (@NonNull DataSnapshot snapshot) {
						
						if (saveBtn.getText ().toString ().trim ().equals ("Save")) {
							
							//If meme is not saved by the user
							
							if (snapshot.exists ()) {
								
								//Increment the number of saves by 1
								
								saves = snapshot.getValue (Long.class);
								
								savedRef.child (String.valueOf (randomNum)).child ("Saved").setValue (saves+1);
								
							} else {
								
								//Set the number of saves to 1
								
								savedRef.child (String.valueOf (randomNum)).child ("Saved").setValue (1);
								
							}
							
							//Get Bitmap of the Meme
							
							imgBitmap = imgFull.getDrawingCache ();
							
							//Store the Meme to Media
							
							MediaStore.Images.Media.insertImage (getContext ().getContentResolver (),
									imgBitmap, nameFull.getText ().toString ().trim (), "");
							
							//Add the Meme to the User's Saved Memes
							
							usersRef.child (UID).child ("Saved Memes").child (String.valueOf (randomNum)).setValue (1);
							
							//Disable the Save Button and Change the Text
							
							saveBtn.setEnabled (false);
							
							saveBtn.setText ("Saved !");
							
						}
						
					}
					
					@Override
					public void onCancelled (@NonNull DatabaseError error) {
					
					}
				});
				
			} else {
				
				//User has not logged in
				
				Toast.makeText (getParentFragment ().getActivity (), "Login to Save Memes !",
						Toast.LENGTH_SHORT).show ();
				
			}
			
		});
		
		//Button to Like the Meme on Screen
		
		likeBtn.setOnClickListener (v -> {
			
			if (firebaseUser != null) {
				
				//User is Logged In
				
				//Check if Meme is Liked by any User
				
				likeRef.child (String.valueOf (randomNum)).child ("Likes")
				       .addListenerForSingleValueEvent (new ValueEventListener () {
					@Override
					public void onDataChange (@NonNull DataSnapshot snapshot) {
						
						if (likeBtn.getText ().toString ().trim ().equals ("Like")) {
							
							//If meme is not liked by the user
							
							if (snapshot.exists ()) {
								
								//Increment the number of likes by 1
								
								likes = snapshot.getValue (Long.class);
								
								likeRef.child (String.valueOf (randomNum)).child ("Likes").setValue (likes+1);
								
							} else {
								
								//Set the number of likes to 1
								
								likeRef.child (String.valueOf (randomNum)).child ("Likes").setValue (1);
								
							}
							
							//Add the Meme to the User's Liked Memes
							
							usersRef.child (UID).child ("Liked Memes").child (String.valueOf (randomNum)).setValue (1);
							
							//Disable the Like Button and Change the Text
							
							likeBtn.setEnabled (false);
							
							likeBtn.setText ("Liked !");
							
						}
						
					}
					
					@Override
					public void onCancelled (@NonNull DatabaseError error) {
					
					}
				});
				
			} else {
				
				//User has not logged in
				
				Toast.makeText (getParentFragment ().getActivity (), "Login to Like Memes !",
						Toast.LENGTH_SHORT).show ();
				
			}
			
		});
		
		//Return the ViewGroup
		
		return BrowseViewGrp;
		
	}
	
	private void GetRandomMemes () {
		
		//Set the Visibility of Components
		
		pBar.setVisibility (View.VISIBLE);
		
		nextBtn.setVisibility (View.INVISIBLE);
		saveBtn.setVisibility (View.INVISIBLE);
		likeBtn.setVisibility (View.INVISIBLE);
		
		nameFull.setVisibility (View.INVISIBLE);
		
		//Generate a Random Number
		
		memeRandom = new Random ();
		
		randomNum = memeRandom.nextInt (maxNum);
		
		//Declare Storage Reference
		
		imgRefFull = FirebaseStorage.getInstance ().getReference ("Memes");
		
		//Get the downloadUrl of the meme at the random number's location
		
		imgRefFull.child (String.valueOf (randomNum)).getDownloadUrl ().addOnSuccessListener (uri -> {
			
			//Convert the Uri to String
			
			imgURL = uri.toString ();
			
			//Get the name of the Memer
			
			memeRef.child (String.valueOf (randomNum)).addListenerForSingleValueEvent (new ValueEventListener () {
				@Override
				public void onDataChange (@NonNull DataSnapshot snapshot) {
					
					//Load the Meme on the Screen
					
					Glide.with (getContext ()).load (imgURL).into (imgFull);
					
					//Set the Name of the Memer
					
					nameFull.setText ("Memer : " + snapshot.getValue (String.class));
					
					if (firebaseUser != null) {
						
						//User has logged in
						
						usersRef.child (UID).child ("Liked Memes").child (String.valueOf (randomNum))
						        .addListenerForSingleValueEvent (new ValueEventListener () {
							@Override
							public void onDataChange (@NonNull DataSnapshot snapshot) {
								
								if (snapshot.exists ()) {
									
									//User has already liked the meme
									
									likeBtn.setText ("Liked !");
									
									likeBtn.setEnabled (false);
									
								} else {
									
									//User has not liked the meme
									
									likeBtn.setText ("Like");
									
									likeBtn.setEnabled (true);
									
								}
								
								//Set the Visibility of the Components
								
								pBar.setVisibility (View.INVISIBLE);
								
								nextBtn.setVisibility (View.VISIBLE);
								saveBtn.setVisibility (View.VISIBLE);
								likeBtn.setVisibility (View.VISIBLE);
								
								nameFull.setVisibility (View.VISIBLE);
								
							}
							
							@Override
							public void onCancelled (@NonNull DatabaseError error) {
							
							}
						});
						
						usersRef.child (UID).child ("Saved Memes").child (String.valueOf (randomNum))
						        .addListenerForSingleValueEvent (new ValueEventListener () {
							        @Override
							        public void onDataChange (@NonNull DataSnapshot snapshot) {
								
								        if (snapshot.exists ()) {
								        	
								        	//User has already saved the meme
									
									        saveBtn.setText ("Saved !");
									
									        saveBtn.setEnabled (false);
									
								        } else {
								        	
								        	//User has not saved the meme
									
									        saveBtn.setText ("Save");
									        
									        saveBtn.setEnabled (true);
									
								        }
								
								        //Set the Visibility of the Components
								
								        pBar.setVisibility (View.INVISIBLE);
								
								        nextBtn.setVisibility (View.VISIBLE);
								        saveBtn.setVisibility (View.VISIBLE);
								        likeBtn.setVisibility (View.VISIBLE);
								
								        nameFull.setVisibility (View.VISIBLE);
								
							        }
							
							        @Override
							        public void onCancelled (@NonNull DatabaseError error) {
								
							        }
						        });
						
					} else {
						
						//User not logged in
						
						//Set the Visibility and Text of the components
						
						likeBtn.setText ("Like");
						
						saveBtn.setText ("Save");
						
						pBar.setVisibility (View.INVISIBLE);
						
						nextBtn.setVisibility (View.VISIBLE);
						saveBtn.setVisibility (View.VISIBLE);
						likeBtn.setVisibility (View.VISIBLE);
						
						nameFull.setVisibility (View.VISIBLE);
						
					}
					
				}
				
				@Override
				public void onCancelled (@NonNull DatabaseError error) {
				
				}
			});
			
		});
	
	}
	
}