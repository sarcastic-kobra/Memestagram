package com.rtech.memestagram;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class AddMemeFragment extends Fragment {
	
	private ViewGroup AddMemeGrp;
	private Button SelectbtnMulti, SelectbtnSingle, UploadBtn;
	private TextView memeCounter;
	private Integer UserPostCount, PostCounter, type, UploadCount, currentimgNum, imgCount,
			PICK_IMAGE = 1,
			PICK_IMAGES = 2;
	private Uri imagePath;
	private StorageReference imgRef;
	private DatabaseReference memeRef, usersRef;
	private String UID, MemerName;
	private Long counter, userPostNum;
	private ProgressBar addmemeBar;
	private ArrayList<Uri> ImageList = new ArrayList<> ();
	private GridView memeView;
	private ImageView memeFull;
	
	public AddMemeFragment () {
	
	}
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		
		super.onCreate (savedInstanceState);
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		//Create ViewGroup
		
		AddMemeGrp = (ViewGroup) inflater.inflate (R.layout.fragment_add_meme, container, false);
		
		//Declare Components
		
		addmemeBar = AddMemeGrp.findViewById (R.id.addmeme_pb);
		
		SelectbtnMulti = AddMemeGrp.findViewById (R.id.select_multiple);
		SelectbtnSingle = AddMemeGrp.findViewById (R.id.select_single);
		UploadBtn = AddMemeGrp.findViewById (R.id.upload_meme);
		
		memeCounter = AddMemeGrp.findViewById (R.id.meme_count);
		
		memeView = AddMemeGrp.findViewById (R.id.grid_add_meme);
		
		memeFull = AddMemeGrp.findViewById (R.id.memeView_full);
		
		//Set Visibility of Components
		
		addmemeBar.setVisibility (View.INVISIBLE);
		
		memeCounter.setVisibility (View.INVISIBLE);
		
		SelectbtnSingle.setVisibility (View.VISIBLE);
		
		SelectbtnMulti.setVisibility (View.VISIBLE);
		
		//Declare Database & Storage References
		
		imgRef = FirebaseStorage.getInstance ().getReference ("Memes");
		memeRef = FirebaseDatabase.getInstance ().getReference ("Memes");
		usersRef = FirebaseDatabase.getInstance ().getReference ("Users");
		
		//Get User ID
		
		UID = FirebaseAuth.getInstance ().getCurrentUser ().getUid ();
		
		//Check if Data exists in the User Meme Counter
		
		usersRef.child (UID).child ("Memes").child (String.valueOf (0)).addListenerForSingleValueEvent (new ValueEventListener () {
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot) {
				
				//If Data does not exist
				
				if (!snapshot.exists ()) {
					
					//Get Total Number of Memes
					
					memeRef.child ("Counter").addListenerForSingleValueEvent (new ValueEventListener () {
						@Override
						public void onDataChange (@NonNull DataSnapshot snapshot) {
							
							//Assign Total Number of Memes to counter
							
							counter = snapshot.getValue (Long.class);
							
							//Get Username
							
							usersRef.child (UID).child ("Username").addListenerForSingleValueEvent (new ValueEventListener () {
								@Override
								public void onDataChange (@NonNull DataSnapshot snapshot) {
									
									//Assign Username and initialize Variables
									
									MemerName = snapshot.getValue (String.class);
									
									PostCounter = 0;
									
									UserPostCount = 0;
									
									//Random Condition to ensure this snippet is executed after
									//Username is received
									
									if (MemerName != null) {
										
										//Execute Function to add Posts to User's Data
										
										AddPostsToUserData(PostCounter, counter, MemerName, UserPostCount);
										
									}
									
								}
								
								@Override
								public void onCancelled (@NonNull DatabaseError error) {
								
								}
							});
							
						}
						
						@Override
						public void onCancelled (@NonNull DatabaseError error) {
						
						}
					});
					
				}
				
			}
			
			@Override
			public void onCancelled (@NonNull DatabaseError error) {
			
			}
		});
		
		//Set Function for Click Event of Multiple Memes
		
		SelectbtnMulti.setOnClickListener (v -> {
			
			Intent SelectMultiIntent = new Intent(Intent.ACTION_GET_CONTENT);
			SelectMultiIntent.setType("image/*");
			
			//Allow Multiple Images to be Chosen
			
			SelectMultiIntent.putExtra (Intent.EXTRA_ALLOW_MULTIPLE, true);
			
			startActivityForResult(Intent.createChooser (SelectMultiIntent, "Select Images"),
					PICK_IMAGES);
		
		});
		
		//Set Function for Click Event of Single Meme
		
		SelectbtnSingle.setOnClickListener (v -> {
			
			Intent SelectSingleIntent = new Intent (Intent.ACTION_GET_CONTENT);
			SelectSingleIntent.setType ("image/*");
			
			//Allow only a Single Image to be Chosen
			
			SelectSingleIntent.putExtra (Intent.EXTRA_ALLOW_MULTIPLE, false);
			
			startActivityForResult (Intent.createChooser (SelectSingleIntent, "Select An " +
					                                                                  "Image"), PICK_IMAGE);
			
		});
		
		//Set Function for Uploading Images
		
		UploadBtn.setOnClickListener (v -> {
			
			//Set Component Visibility
			
			addmemeBar.setVisibility (View.VISIBLE);
			
			UploadBtn.setVisibility (View.INVISIBLE);
			
			//Get Total Number of Memes
			
			memeRef.child ("Counter").addListenerForSingleValueEvent (new ValueEventListener () {
				@Override
				public void onDataChange (@NonNull DataSnapshot snapshot) {
					
					//Assign Value to counter
					
					counter = snapshot.getValue (Long.class);
					
					//Get Username
					
					usersRef.child (UID).child ("Username").addListenerForSingleValueEvent (new ValueEventListener () {
						@Override
						public void onDataChange (@NonNull DataSnapshot snapshot) {
							
							//Assign Username to UserName
							
							String UserName = snapshot.getValue (String.class);
							
							//Check if Counter exists for User's Memes
							
							usersRef.child (UID).child ("Memes").child ("Count").addListenerForSingleValueEvent (new ValueEventListener () {
								@Override
								public void onDataChange (@NonNull DataSnapshot snapshot) {
									
									if (snapshot.exists ()) {
										
										//If counter exists, assign value to userPostNum
										
										userPostNum = snapshot.getValue (Long.class);
										
									} else {
										
										//If counter does not exist, assign userPostNum as 0
										
										userPostNum = 0L;
										
									}
									
									//Check for Single or Multiple Meme Selection
									
									if (type == 0) {
										
										//Code for Single Meme
										
										//Add the imagePath under Memes/counter in the Storage
										
										imgRef.child (String.valueOf (counter)).putFile (imagePath)
										      .addOnSuccessListener (taskSnapshot -> {
										      	
										      	  //Code to execute if meme is added successfully
											      
											      //Get the download Url of the newly added Meme
											      //Store the URI in the Memes of the User
										      	
											      imgRef.child (String.valueOf (counter)).getDownloadUrl ()
											            .addOnSuccessListener (uri -> usersRef.child (UID).child ("Memes").child (String.valueOf (userPostNum))
										                                                  .setValue (uri.toString ()));
											      
											      //Set the name of the memer in the database
											
											      memeRef.child (String.valueOf (counter)).setValue (UserName);
											      
											      //Increase the count of total Memes
											
											      counter = counter + 1;
											      
											      //Update the Total Meme counter
											
											      memeRef.child ("Counter").setValue (counter);
											      
											      //Update the count of the User's Memes
											
											      usersRef.child (UID).child ("Memes").child ("Count").setValue (userPostNum + 1);
											
											      Toast.makeText (getParentFragment ().getActivity (), "Meme " +
													                                                           "Uploaded Successfully !",
													      Toast.LENGTH_SHORT).show ();
											      
										      });
										
									} else if (type == 1) {
										
										//Code for Multiple Memes
										
										//Initialize Upload Count to 0
										
										UploadCount = 0;
										
										//Execute Function to Upload Multiple Memes
										
										UploadMultipleMemes (counter, UploadCount, userPostNum,
												UserName);
										
									}
									
								}
								
								@Override
								public void onCancelled (@NonNull DatabaseError error) {
								
								}
							});
							
						}
						
						@Override
						public void onCancelled (@NonNull DatabaseError error) {
						
						}
					});
					
					//Set Visibility of Components
					
					memeView.setVisibility (View.INVISIBLE);
					
					memeFull.setVisibility (View.INVISIBLE);
					
					SelectbtnSingle.setVisibility (View.VISIBLE);
					
					SelectbtnMulti.setVisibility (View.VISIBLE);
					
					addmemeBar.setVisibility (View.INVISIBLE);
					
					UploadBtn.setVisibility (View.VISIBLE);
					
					memeCounter.setVisibility (View.INVISIBLE);
					
					//Clear the Text in the MemeCounter TextView
					
					memeCounter.setText ("");
					
				}
				
				@Override
				public void onCancelled (@NonNull DatabaseError error) {
				
				}
			});
		
		});
		
		//Return the ViewGroup
		
		return AddMemeGrp;
		
	}
	
	//Function to Upload Multiple Memes
	
	private void UploadMultipleMemes (Long Counter, Integer uploadCount, Long UserPostNum,
	                                  String userName) {
		
		//Assign Values to Counter, uploadCount, UserPostNum and userName
		
		//Add the imagePath under Memes/counter in the Storage
		
		imgRef.child (String.valueOf (Counter)).putFile (ImageList.get (uploadCount))
		      .addOnSuccessListener (taskSnapshot ->
				
				                             //Code to exexute if meme is added successfully
				
				                             //Get the download Url of the newly added Meme
				                             //Store the URI in the Memes of the User
				                             
				                             imgRef.child (String.valueOf (Counter)).getDownloadUrl ()
		                                                   .addOnSuccessListener (uri -> { usersRef.child (UID).child ("Memes")
			                                                           .child (String.valueOf (UserPostNum))
			                                                           .setValue (uri.toString ());
		                                                   
			                                                   //Set the name of the memer in the database
			                                                   
			                                                   memeRef.child (String.valueOf (Counter))
			                                                          .setValue (userName);
			                                                   
			                                                   //Set newCounter
			                                                   
			                                                   Long newCounter = Counter + 1;
			                                                   
			                                                   //Set newUserPostNum
			                                                   
			                                                   Long newUserPostNum = UserPostNum + 1;
			                                                   
			                                                   //Update Counter of User's Memes
			                                                   
			                                                   usersRef.child (UID).child (
					                                                   "Memes")
			                                                           .child ("Count").setValue (newUserPostNum);
			                                                   
			                                                   //Update Counter of Total Memes
			                                                   
			                                                   memeRef.child ("Counter").setValue (newCounter);
			                                                   
			                                                   //Set newuploadCount
			                                                   
			                                                   int newuploadCount = uploadCount + 1;
			                                                   
			                                                   if (newuploadCount < ImageList.size ()) {
			                                                   	
			                                                   	   //If All Memes are not Uploaded
			                                                   	
				                                                   UploadMultipleMemes (newCounter, newuploadCount, newUserPostNum, userName);
				                                                   
			                                                   } else {
			                                                   	
			                                                   	   //If All Memes are Uploaded
			                                                   	
				                                                   Toast.makeText (getParentFragment ().getActivity (), "Memes " +
						                                                                                                        "Uploaded Successfully !",
						                                                   Toast.LENGTH_SHORT).show ();
				                                                   
			                                                   }
			
		                                                   }));
		
	}
	
	//Function to add Memes to User's Data
	
	private void AddPostsToUserData (final Integer postCounter, final Long counter,
	                                 final String memerName, final Integer userPostCount) {
		
		//Assign Values to postCounter, counter, memerName and userPostCount
		
		//Check the name of the Memer for the Specific Meme
		
		memeRef.child (String.valueOf (postCounter)).addListenerForSingleValueEvent (new ValueEventListener () {
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot) {
				
				if (Objects.equals (snapshot.getValue (String.class), memerName)) {
					
					//If the memer is the same as the User
					
					//Get downloadUrl of that Meme
					//Store it in the User's Memes
					
					imgRef.child (String.valueOf (postCounter)).getDownloadUrl ()
					      .addOnSuccessListener (uri -> usersRef.child (UID).child ("Memes").child (String.valueOf (userPostCount))
				                                            .setValue (uri.toString ()));
					
					//Update Values
					
					int newuserPostCount = userPostCount + 1;
					
					int newpostCounter = postCounter + 1;
					
					if (newpostCounter < counter) {
						
						//If all memes have not been checked
						
						AddPostsToUserData (newpostCounter, counter, memerName, newuserPostCount);
						
					}
					
				} else {
					
					//If memer and User are different
					
					int newpostCounter = postCounter + 1;
					
					if (newpostCounter < counter) {
						
						//If all memes have not been checked
						
						AddPostsToUserData (newpostCounter, counter, memerName, userPostCount);
						
					}
					
				}
				
			}
			
			@Override
			public void onCancelled (@NonNull DatabaseError error) {
			
			}
		});
		
	}
	
	@Override
	public void onActivityResult (int requestCode, int resultCode, @Nullable Intent data) {
		
		super.onActivityResult (requestCode, resultCode, data);
		
		if (requestCode == PICK_IMAGES) {
			
			//If User wants to upload multiple Memes
			
			if (resultCode == RESULT_OK) {
				
				if (data.getClipData () != null) {
					
					type = 1;
					
					imgCount = data.getClipData ().getItemCount ();
					
					currentimgNum = 0;
					
					while (currentimgNum < imgCount) {
						
						//Add Uris of all Images to the Array
						
						imagePath = data.getClipData ().getItemAt (currentimgNum).getUri ();
						
						ImageList.add (imagePath);
						
						currentimgNum = currentimgNum + 1;
						
					}
					
					if (imgCount > 20) {
						
						memeCounter.setText ("Select A Maximum of 20 Memes");
						
						ImageList.clear ();
						
					} else {
						
						memeCounter.setText ("Memes Selected : " + imgCount);
						
						memeCounter.setVisibility (View.VISIBLE);
						
					}
					
					//Set Visibility of Components
					
					SelectbtnSingle.setVisibility (View.INVISIBLE);
					
					SelectbtnMulti.setVisibility (View.INVISIBLE);
					
					memeView.setVisibility (View.VISIBLE);
					
					//Initialize the Adapter to Display the selected Memes from the Array
					
					MemeAdapter memeAdapter = new MemeAdapter(getActivity (), ImageList);
					
					memeView.setAdapter (memeAdapter);
					
				}
				
			}
			
		}
		
		if (requestCode == PICK_IMAGE) {
			
			//If User wants to upload a single Meme
			
			if (resultCode == RESULT_OK) {
				
				if (data.getData () != null) {
					
					type = 0;
					
					imagePath = data.getData ();
					
					//Set Visibility of Components
					
					memeFull.setVisibility (View.VISIBLE);
					
					//Load the Selected Meme on the Screen
					
					Glide.with (getContext ()).load (imagePath).into (memeFull);
					
					memeCounter.setText ("1 Meme Selected");
					
					//Set Visibility of Components
					
					memeCounter.setVisibility (View.VISIBLE);
					
					SelectbtnSingle.setVisibility (View.INVISIBLE);
					
					SelectbtnMulti.setVisibility (View.INVISIBLE);
					
				}
			
			}
			
		}
		
	}
	
}

class MemeAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater layoutInflater;
	private ArrayList<Uri> memeList;
	
	public MemeAdapter(Context context,  ArrayList<Uri> memeList) {
		
		this.mContext = context;
		this.memeList = memeList;
		
	}
	
	@Override
	public int getCount () {
		
		return memeList.size ();
		
	}
	
	@Override
	public Object getItem (int position) {
		
		return null;
		
	}
	
	@Override
	public long getItemId (int position) {
		
		return 0;
		
	}
	
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		
		if (layoutInflater == null) {
			
			layoutInflater = (LayoutInflater) mContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
			
		}
		
		if (convertView == null) {
			
			convertView = layoutInflater.inflate (R.layout.grid_item, null);
			
		}
		
		ImageView imageView = convertView.findViewById (R.id.grid_memeView);
		
		Glide.with (mContext).load (memeList.get (position)).into (imageView);
		
		return convertView;
		
	}
	
}