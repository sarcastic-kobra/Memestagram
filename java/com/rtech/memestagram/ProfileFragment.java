package com.rtech.memestagram;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileFragment extends Fragment {
	
	private ViewGroup ProfileViewGroup;
	private TextView userName, noMeme;
	private ImageView memeFull;
	private GridView profileMemes;
	private ArrayList<Uri> ImageList = new ArrayList<> ();
	private Integer MemeNumber;
	private ProgressBar profilePB;
	private String UID;
	private DatabaseReference userMemesRef;
	
	public ProfileFragment () {
	
	}
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		
		super.onCreate (savedInstanceState);
		
	}
	
	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		//Create a ViewGroup
		
		ProfileViewGroup = (ViewGroup) inflater.inflate (R.layout.fragment_profile, container, false);
		
		//Declare the Components
		
		userName = ProfileViewGroup.findViewById (R.id.text_username);
		noMeme = ProfileViewGroup.findViewById (R.id.no_memes);
		memeFull = ProfileViewGroup.findViewById (R.id.full_page_meme);
		
		profileMemes = ProfileViewGroup.findViewById (R.id.grid_profile);
		
		profilePB = ProfileViewGroup.findViewById (R.id.profile_bar);
		
		//Declare Database References
		
		userMemesRef = FirebaseDatabase.getInstance ().getReference ("Users");
		
		//Set the Visibility of Components
		
		userName.setVisibility (View.INVISIBLE);
		noMeme.setVisibility (View.INVISIBLE);
		
		profilePB.setVisibility (View.VISIBLE);
		
		//Get the UID
		
		UID = Objects.requireNonNull (FirebaseAuth.getInstance ().getCurrentUser ()).getUid ();
		
		//Get the Username
		
		userMemesRef.child (UID).child ("Username").addListenerForSingleValueEvent (new ValueEventListener () {
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot) {
				
				//Set the Text and Visibility of the Username
				
				userName.setText ("Hi " + snapshot.getValue (String.class));
				
				userName.setVisibility (View.VISIBLE);
				
			}
			
			@Override
			public void onCancelled (@NonNull DatabaseError error) {
			
			}
		});
		
		//Initialize MemeNumber to 0
		
		MemeNumber = 0;
		
		AddURLtoArray(MemeNumber);
		
		//Set Back Button Activity
		
		OnBackPressedCallback ShowGrid = new OnBackPressedCallback (true) {
			@Override
			public void handleOnBackPressed () {
				
				memeFull.setVisibility (View.INVISIBLE);
				
				profileMemes.setVisibility (View.VISIBLE);
				
			}
		};
		
		requireActivity ().getOnBackPressedDispatcher ().addCallback (getViewLifecycleOwner(), ShowGrid);
		
		//Return ViewGroup
		
		return ProfileViewGroup;
		
	}
	
	private void AddURLtoArray (int memeNumber) {
		
		//Get the User's Memes
		
		userMemesRef.child (UID).child ("Memes").child (String.valueOf (memeNumber))
		            .addListenerForSingleValueEvent (new ValueEventListener () {
			@Override
			public void onDataChange (@NonNull DataSnapshot snapshot) {
				
				if (snapshot.exists ()) {
					
					//Add the Uri of the Meme to the Array
					
					ImageList.add (Uri.parse (snapshot.getValue (String.class)));
					
					int newMemeNumber = memeNumber + 1;
					
					//Start again for the next Meme
					
					AddURLtoArray (newMemeNumber);
					
				} else {
					
					//Display Progress Bar
					
					profilePB.setVisibility (View.INVISIBLE);
					
					if (ImageList.size () == 0) {
						
						//If no memes are added by the user
						
						noMeme.setText ("No Memes Yet !");
						
						noMeme.setVisibility (View.VISIBLE);
						
					} else {
						
						//Set the Profile Adapter
						
						profileMemes.setAdapter (new ProfileAdapter (getActivity (), ImageList));
						
						//Click event for each Meme
						
						profileMemes.setOnItemClickListener ((parent, view, position, id) -> {
							
							//Load Meme on the screen
							
							Glide.with (getView ().getContext ()).load (ImageList.get (ImageList.size () - position - 1))
							     .into (memeFull);
							
							//Set Visibility of Components
							
							memeFull.setVisibility (View.VISIBLE);
							
							profileMemes.setVisibility (View.INVISIBLE);
							
						});
						
					}
					
				}
				
			}
			
			@Override
			public void onCancelled (@NonNull DatabaseError error) {
			
			}
		});
		
	}
	
}

class ProfileAdapter extends BaseAdapter {
	
	private Context mContext;
	private LayoutInflater layoutInflater;
	private ArrayList<Uri> imageList;
	
	public ProfileAdapter(Context context,  ArrayList<Uri> imageList) {
		
		this.mContext = context;
		this.imageList = imageList;
		
	}
	
	@Override
	public int getCount () {
		
		return imageList.size ();
		
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
		
		//Load the Memes into the GridView
		
		Glide.with (mContext).load (imageList.get (imageList.size () - position - 1)).into ((ImageView) convertView.findViewById (R.id.grid_memeView));
		
		//imageList.get (imageList.size () - position - 1) is needed to load the memes in reverse
		
		//Return the View
		
		return convertView;
		
	}
	
}