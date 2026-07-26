package com.fishr.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fishr.app.utils.CatchStorage;
import com.fishr.app.utils.CatchEntry;
import org.json.JSONObject;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_LOCATION = 101;

    LinearLayout tabCatch, tabLog, tabLicense;
    FrameLayout contentCatch, contentLog, contentLicense;

    // Catch tab
    ImageView catchPhoto;
    TextView catchLocation, catchDateTime;
    EditText catchWeight, catchLength, catchNotes;
    Spinner speciesSpinner;
    Button btnCamera, btnSaveCatch;
    View catchView;

    // Log tab
    RecyclerView catchList;
    CatchAdapter adapter;
    View logView;

    // License tab
    ImageView licensePhoto;
    TextView licenseName, licenseNumber, licenseClass, licenseExpiry;
    EditText editName, editNumber, editClass, editExpiry;
    Button btnSaveLicense;
    View licenseView;

    String currentPhotoPath;
    double currentLat = 0, currentLng = 0;
    Bitmap currentBitmap = null;

    // Species database - common Atlantic/Mediterranean fish
    String[] commonSpecies = {
        "Atlantic Mackerel", "European Sea Bass", "Dusky Grouper", "Red Mullet",
        "Common Cutlassfish", "Blue Whiting", "Atlantic Horse Mackerel", 
        "European Hake", "Anglerfish", "Thornback Ray", "Spiny Dogfish",
        "Blue Shark", "Shortfin Mako", "Thresher Shark", "Common Stingray",
        "Gilt Head Bream", "Red Bream", "Black Bream", "Common Dentex",
        "White Sea Bream", "Annular Sea Bream", "Salema", "Ocean Sunfish",
        "Atlantic Bonito", "Skipjack Tuna", "Little Tunny", "Swordfish",
        "European Eel", "Greater Weever", "Streaked Gurnard", "Tub Gurnard",
        "Longspined Scorpionfish", "Red Scorpionfish", "Comber", "Ballan Wrasse",
        "Corkwing Wrasse", "Peacock Wrasse", "Rainbow Wrasse", "Mediterranean Moray",
        "Sea Urchin", "Octopus", "Common Cuttlefish", "European Lobster",
        "Spider Crab", "Edible Crab", "Striped Red Mullet", "Atlantic Croaker"
    };

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && currentPhotoPath != null) {
                File f = new File(currentPhotoPath);
                if (f.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    if (bitmap != null) {
                        currentBitmap = bitmap;
                        catchPhoto.setImageBitmap(bitmap);
                        catchPhoto.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTabs();
        initCatchTab();
        initLogTab();
        initLicenseTab();

        showTab("catch");
        loadCatches();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    void initTabs() {
        tabCatch = findViewById(R.id.tab_catch);
        tabLog = findViewById(R.id.tab_log);
        tabLicense = findViewById(R.id.tab_license);
        contentCatch = findViewById(R.id.content_catch);
        contentLog = findViewById(R.id.content_log);
        contentLicense = findViewById(R.id.content_license);

        tabCatch.setOnClickListener(v -> showTab("catch"));
        tabLog.setOnClickListener(v -> showTab("log"));
        tabLicense.setOnClickListener(v -> showTab("license"));
    }

    void showTab(String tab) {
        contentCatch.setVisibility(tab.equals("catch") ? View.VISIBLE : View.GONE);
        contentLog.setVisibility(tab.equals("log") ? View.VISIBLE : View.GONE);
        contentLicense.setVisibility(tab.equals("license") ? View.VISIBLE : View.GONE);

        tabCatch.setAlpha(tab.equals("catch") ? 1.0f : 0.5f);
        tabLog.setAlpha(tab.equals("log") ? 1.0f : 0.5f);
        tabLicense.setAlpha(tab.equals("license") ? 1.0f : 0.5f);

        if (tab.equals("catch")) {
            tabCatch.findViewById(R.id.tab_catch);
        }
        if (tab.equals("log")) loadCatches();
        if (tab.equals("license")) loadLicense();
    }

    void initCatchTab() {
        catchView = getLayoutInflater().inflate(R.layout.content_catch, contentCatch, false);
        contentCatch.addView(catchView);
        
        catchPhoto = catchView.findViewById(R.id.catch_photo);
        catchLocation = catchView.findViewById(R.id.catch_location);
        catchDateTime = catchView.findViewById(R.id.catch_datetime);
        catchWeight = catchView.findViewById(R.id.catch_weight);
        catchLength = catchView.findViewById(R.id.catch_length);
        catchNotes = catchView.findViewById(R.id.catch_notes);
        speciesSpinner = catchView.findViewById(R.id.species_spinner);
        btnCamera = catchView.findViewById(R.id.btn_camera);
        btnSaveCatch = catchView.findViewById(R.id.btn_save_catch);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_dropdown_item, commonSpecies);
        speciesSpinner.setAdapter(adapter);

        catchDateTime.setText(new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(new Date()));

        btnCamera.setOnClickListener(v -> openCamera());
        btnSaveCatch.setOnClickListener(v -> saveCatch());

        catchPhoto.setVisibility(View.GONE);
    }

    void initLogTab() {
        logView = getLayoutInflater().inflate(R.layout.content_log, contentLog, false);
        contentLog.addView(logView);
        catchList = logView.findViewById(R.id.catch_list);
        catchList.setLayoutManager(new LinearLayoutManager(this));
    }

    void initLicenseTab() {
        licenseView = getLayoutInflater().inflate(R.layout.content_license, contentLicense, false);
        contentLicense.addView(licenseView);
        
        licensePhoto = licenseView.findViewById(R.id.license_photo);
        licenseName = licenseView.findViewById(R.id.license_name);
        licenseNumber = licenseView.findViewById(R.id.license_number);
        licenseClass = licenseView.findViewById(R.id.license_class);
        licenseExpiry = licenseView.findViewById(R.id.license_expiry);
        editName = licenseView.findViewById(R.id.edit_license_name);
        editNumber = licenseView.findViewById(R.id.edit_license_number);
        editClass = licenseView.findViewById(R.id.edit_license_class);
        editExpiry = licenseView.findViewById(R.id.edit_license_expiry);
        btnSaveLicense = licenseView.findViewById(R.id.btn_save_license);

        btnSaveLicense.setOnClickListener(v -> saveLicense());
    }

    void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "FISH_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    void saveCatch() {
        String species = speciesSpinner.getSelectedItem().toString();
        
        String weightStr = catchWeight.getText().toString();
        String lengthStr = catchLength.getText().toString();
        String notes = catchNotes.getText().toString();

        CatchEntry entry = new CatchEntry();
        entry.id = UUID.randomUUID().toString().substring(0, 8);
        entry.species = species;
        entry.weight = weightStr.isEmpty() ? 0 : Double.parseDouble(weightStr);
        entry.length = lengthStr.isEmpty() ? 0 : Double.parseDouble(lengthStr);
        entry.notes = notes;
        entry.latitude = currentLat;
        entry.longitude = currentLng;
        entry.timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
        entry.photoPath = currentPhotoPath;

        CatchStorage.save(this, entry);
        
        Toast.makeText(this, "Catch saved! 🐟", Toast.LENGTH_SHORT).show();
        
        // Reset form
        if (currentBitmap != null) currentBitmap.recycle();
        currentBitmap = null;
        catchPhoto.setImageBitmap(null);
        catchPhoto.setVisibility(View.GONE);
        catchWeight.setText("");
        catchLength.setText("");
        catchNotes.setText("");
        currentPhotoPath = null;
        
        showTab("log");
    }

    void loadCatches() {
        if (catchList == null) return;
        List<CatchEntry> catches = CatchStorage.loadAll(this);
        adapter = new CatchAdapter(catches, this);
        catchList.setAdapter(adapter);
    }

    void saveLicense() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
            .putString("license_name", editName.getText().toString())
            .putString("license_number", editNumber.getText().toString())
            .putString("license_class", editClass.getText().toString())
            .putString("license_expiry", editExpiry.getText().toString())
            .apply();
        Toast.makeText(this, "License saved!", Toast.LENGTH_SHORT).show();
        loadLicense();
    }

    void loadLicense() {
        if (licenseName == null) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String name = prefs.getString("license_name", "Your Name");
        String number = prefs.getString("license_number", "---");
        String licClass = prefs.getString("license_class", "---");
        String expiry = prefs.getString("license_expiry", "---");

        licenseName.setText(name);
        licenseNumber.setText("No: " + number);
        licenseClass.setText("Class: " + licClass);
        licenseExpiry.setText("Expires: " + expiry);

        editName.setText(name);
        editNumber.setText(number);
        editClass.setText(licClass);
        editExpiry.setText(expiry);
    }
}
