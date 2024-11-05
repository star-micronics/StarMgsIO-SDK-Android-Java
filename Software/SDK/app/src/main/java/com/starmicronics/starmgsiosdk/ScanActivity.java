package com.starmicronics.starmgsiosdk;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.starmicronics.starmgsio.ConnectionInfo;
import com.starmicronics.starmgsio.StarDeviceManager;
import com.starmicronics.starmgsio.StarDeviceManagerCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanActivity extends AppCompatActivity {
    private static final String INTERFACE_TYPE_KEY = "INTERFACE_TYPE_KEY";
    private static final String DEVICE_NAME_KEY    = "DEVICE_NAME_KEY";
    private static final String IDENTIFIER_KEY     = "IDENTIFIER_KEY";
    private static final String SCALE_TYPE_KEY     = "SCALE_TYPE_KEY";

    private StarDeviceManager mStarDeviceManager;

    private List<Map<String, String>> mDataMapList;

    private SimpleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan);
        setTitle("MG series Scale Sample");

        if (SDK_INT <= 30) {
            if( (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) ){
                // If you are using Android 11 and targetSdkVersion is 30 or earlier,
                // you have to request Bluetooth permission to use the Bluetooth printer.
                // https://developer.android.com/develop/connectivity/bluetooth/bt-permissions#declare-android11-or-lower

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN},    0x00);
            }
        }
        else
        {
            if((ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) ){
                // If you are using Android 12 and targetSdkVersion is 31 or later,
                // you have to request Bluetooth permission to use the Bluetooth printer.
                // https://developer.android.com/develop/connectivity/bluetooth/bt-permissions#declare-android12-or-higher

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT},    0x00);
            }
        }

        ListView discoveredListView = findViewById(R.id.DiscoveredListView);

        mDataMapList = new ArrayList<>();

        mAdapter = new SimpleAdapter(
                this,
                mDataMapList,
                R.layout.list_discovered_row,
                new String[] { INTERFACE_TYPE_KEY, DEVICE_NAME_KEY, IDENTIFIER_KEY, SCALE_TYPE_KEY},
                new int[] { R.id.InterfaceTypeTextView, R.id.DeviceNameTextView, R.id.IdentifierTextView, R.id.ScaleInfoTextView});

        discoveredListView.setAdapter(mAdapter);

        discoveredListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDataMapList.clear();
                mAdapter.notifyDataSetChanged();

                mStarDeviceManager.stopScan();

                TextView identifierTextView    = view.findViewById(R.id.IdentifierTextView);
                String   identifier            = identifierTextView.getText().toString();
                TextView interfaceTypeTextView = view.findViewById(R.id.InterfaceTypeTextView);
                String   interfaceType         = interfaceTypeTextView.getText().toString();
                TextView scaleTypeTextView     = view.findViewById(R.id.ScaleInfoTextView);
                String   scaleType             = scaleTypeTextView.getText().toString();

                Intent intent = new Intent(ScanActivity.this, ScaleActivity.class);
                intent.putExtra(ScaleActivity.IDENTIFIER_BUNDLE_KEY,  identifier);
                intent.putExtra(ScaleActivity.INTERFACE_TYPE_BUNDLE_KEY, interfaceType);
                intent.putExtra(ScaleActivity.SCALE_TYPE_BUNDLE_KEY, scaleType);

                startActivity(intent);
            }
        });
    }

    public void setPadding(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int insetTypes = WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.systemBars();
            Insets bars = insets.getInsets(insetTypes);
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPadding(findViewById(R.id.DiscoveredListView));

        mStarDeviceManager = new StarDeviceManager(ScanActivity.this, StarDeviceManager.InterfaceType.All);

        mStarDeviceManager.scanForScales(mStarDeviceManagerCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mStarDeviceManager.stopScan();
    }

    private final StarDeviceManagerCallback mStarDeviceManagerCallback = new StarDeviceManagerCallback() {
        @Override
        public void onDiscoverScale(@NonNull ConnectionInfo connectionInfo) {
            Map<String, String> item = new HashMap<>();
            item.put(INTERFACE_TYPE_KEY, connectionInfo.getInterfaceType().name());
            item.put(DEVICE_NAME_KEY, connectionInfo.getDeviceName());
            item.put(IDENTIFIER_KEY,  connectionInfo.getIdentifier());
            item.put(SCALE_TYPE_KEY, connectionInfo.getScaleType().name());

            if(!mDataMapList.contains(item)) {
                mDataMapList.add(item);
                mAdapter.notifyDataSetChanged();
            }
        }
    };
}
