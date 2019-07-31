package com.example.m.recipebook.Controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StarController {
    private Boolean likeChecker;

   public void updateStarCount(final Context context, final DatabaseReference PostsRef, final DatabaseReference LikesRef, final String CurrentUserID, final String PostKey){

       Calendar calendar = Calendar.getInstance();
       SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
       String dateStamp = dateFormat.format(calendar.getTime());

       Calendar time = Calendar.getInstance();
       SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
       String timeStamptemp = timeFormat.format(time.getTime());

       final String timeStamp = dateStamp + timeStamptemp;


       likeChecker = true;

       LikesRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               if (likeChecker.equals(true)) {
                   if (dataSnapshot.child(PostKey).hasChild(CurrentUserID)) {
                       LikesRef.child(PostKey).child(CurrentUserID).removeValue();
                       likeChecker = false;

                       PostsRef.child(PostKey).child("likesnumber").addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot) {
                               if (dataSnapshot.exists()){
                                   int numberOfLikes = Integer.valueOf(dataSnapshot.getValue().toString());
                                   numberOfLikes = numberOfLikes - 1;
                                   final String numberLikes = String.valueOf(numberOfLikes);
                                   PostsRef.child(PostKey).child("likesnumber").setValue(numberLikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()){
                                               Toast.makeText(context, "Star removed", Toast.LENGTH_SHORT).show();
                                               if (numberLikes.equals("0")){                                                            //Todo check this new part
                                                  PostsRef.child(PostKey).child("sortByLikes").removeValue();                           //
                                               }                                                                                        //
                                               else{                                                                                    //
                                                   PostsRef.child(PostKey).child("sortByLikes").setValue(numberLikes + " " + timeStamp);      //
                                               }                                                                                        //
                                           }
                                           else{
                                               Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                           }

                                       }
                                   });
                               }
                               else{
                                   //Toast.makeText(context, "Does not exist", Toast.LENGTH_SHORT).show();
                               }

                           }

                           @Override
                           public void onCancelled(DatabaseError databaseError) {

                           }
                       });
                   }
                   else {
                       LikesRef.child(PostKey).child(CurrentUserID).setValue(true);
                       likeChecker = false;
                       PostsRef.child(PostKey).child("likesnumber").addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot) {
                               if (dataSnapshot.exists()){
                                   int numberOfLikes = Integer.valueOf(dataSnapshot.getValue().toString());
                                   numberOfLikes = numberOfLikes + 1;
                                   final String numberLikes = String.valueOf(numberOfLikes);

                                   PostsRef.child(PostKey).child("likesnumber").setValue(numberLikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()){
                                               Toast.makeText(context, "You gave this recipe a star", Toast.LENGTH_SHORT).show();
                                               if (numberLikes.equals("0")){                                                                //Todo check this new part
                                                   PostsRef.child(PostKey).child("sortByLikes").setValue(numberLikes + " " + timeStamp);    //
                                               }                                                                                            //
                                               else{                                                                                        //
                                                   PostsRef.child(PostKey).child("sortByLikes").setValue(numberLikes + " " + timeStamp);    //
                                               }
                                           }
                                           else{
                                               Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                                           }
                                       }
                                   });
                               }
                               else {
                                   PostsRef.child(PostKey).child("likesnumber").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()){
                                               Toast.makeText(context, "You gave this recipe a star", Toast.LENGTH_SHORT).show();
                                               PostsRef.child(PostKey).child("sortByLikes").setValue("1" + " " + timeStamp);
                                               likeChecker = false;
                                               return;
                                           }

                                       }
                                   });
                               }
                           }

                           @Override
                           public void onCancelled(DatabaseError databaseError) {

                           }
                       });
                   }

               }
           }
           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });

    }

}
