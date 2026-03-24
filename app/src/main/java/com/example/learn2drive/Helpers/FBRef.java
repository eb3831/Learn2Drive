package com.example.learn2drive.Helpers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBRef
{
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();
    public static FirebaseDatabase FB_DB = FirebaseDatabase.getInstance();

    public static DatabaseReference refUsers = FB_DB.getReference("Users");
    public static DatabaseReference refStudents = FB_DB.getReference("Users").child("Students");
    public static DatabaseReference refTeachers = FB_DB.getReference("Users").child("Teachers");
    public static DatabaseReference refAdmins = FB_DB.getReference("Users").child("Admins");

    public static DatabaseReference refClasses = FB_DB.getReference("Classes");

    public static DatabaseReference refScheduledLessons = FB_DB.getReference("Lessons").child("Scheduled");
    public static DatabaseReference refDoneLessons = FB_DB.getReference("Lessons").child("Done");


    public static DatabaseReference refTeachersTimeTable = FB_DB.getReference("Teachers Timetable");


    // --- Firebase Storage ---
    public static FirebaseStorage FB_STORAGE = FirebaseStorage.getInstance();
    public static StorageReference refProfilePics = FB_STORAGE.getReference("Profile Pictures");
    public static StorageReference refLessonsDetails = FB_STORAGE.getReference("Lessons Details");


    public static String uid;


    /**
     * This function gets the current fb user, and updates to references according to its uid.
     * @param fbUser the current connected fb user.
     */
    public static void saveCurrentUser(FirebaseUser fbUser)
    {
        uid = fbUser.getUid();
    }
}
