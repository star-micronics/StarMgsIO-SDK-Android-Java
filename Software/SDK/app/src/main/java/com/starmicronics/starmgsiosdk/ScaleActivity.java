package com.starmicronics.starmgsiosdk;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.starmicronics.starmgsio.ConnectionInfo;
import com.starmicronics.starmgsio.Scale;
import com.starmicronics.starmgsio.ScaleCallback;
import com.starmicronics.starmgsio.ScaleData;
import com.starmicronics.starmgsio.ScaleOutputConditionSetting;
import com.starmicronics.starmgsio.ScaleSetting;
import com.starmicronics.starmgsio.ScaleType;
import com.starmicronics.starmgsio.StarDeviceManager;

import java.util.Locale;
import java.util.Objects;


public class ScaleActivity extends AppCompatActivity {
    public static final String IDENTIFIER_BUNDLE_KEY     = "IDENTIFIER_BUNDLE_KEY";
    public static final String INTERFACE_TYPE_BUNDLE_KEY = "INTERFACE_TYPE_BUNDLE_KEY";
    public static final String SCALE_TYPE_BUNDLE_KEY     = "SCALE_TYPE_BUNDLE_KEY";

    private TextView mWeightTextView;
    private TextView mStateTextView;

    private Scale mScale;
    private ScaleType mScaleType;

    ConnectionInfo connectionInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scale);
        setTitle("MG series Scale Sample");

        mWeightTextView = findViewById(R.id.WeightTextView);
        mStateTextView  = findViewById(R.id.StateTextView);

        Button zeroPointAdjustmentButton = findViewById(R.id.ZeroPointAdjustmentButton);
        zeroPointAdjustmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScale == null) {
                    return;
                }

                mScale.updateSetting(ScaleSetting.ZeroPointAdjustment);
            }
        });

        mScaleType = ScaleType.getEnum(getIntent().getStringExtra(SCALE_TYPE_BUNDLE_KEY));

        Spinner outputConditionSpinner = (Spinner)findViewById(R.id.OutputConditionSettingSpinner);
        String text ="Status:";
        mStateTextView.setText(text);
        if (mScaleType == ScaleType.MGS) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.output_condition_settings_list, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            outputConditionSpinner.setAdapter(adapter);
        }
        else if (mScaleType == ScaleType.MGTS) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.output_condition_settings_list_MGTS, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            outputConditionSpinner.setAdapter(adapter);
        }

        Button outputConditionSettingButton = findViewById(R.id.OutputConditionSettingButton);
        outputConditionSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScale == null || mScaleType == null || mScaleType == ScaleType.INVALID) {
                    return;
                }

                Spinner outputConditionSpinner = (Spinner)findViewById(R.id.OutputConditionSettingSpinner);
                int index = outputConditionSpinner.getSelectedItemPosition();

                ScaleOutputConditionSetting scaleOutputCondition = null;
                if (mScaleType == ScaleType.MGS) {
                    switch (index) {
                        case 0:
                            scaleOutputCondition = ScaleOutputConditionSetting.StopOutput;
                            break; //Output Condition 0
                        case 1:
                            scaleOutputCondition = ScaleOutputConditionSetting.ContinuousOutputAtAllTimes;
                            break; //Output Condition 1
                        case 2:
                            scaleOutputCondition = ScaleOutputConditionSetting.ContinuousOutputAtStableTimes;
                            break; //Output Condition 2
                        case 3:
                            scaleOutputCondition = ScaleOutputConditionSetting.PushDownKeyForOneTimeInstantOutput;
                            break; //Output Condition 3
                        case 4:
                            scaleOutputCondition = ScaleOutputConditionSetting.AutoOutput;
                            break; //Output Condition 4
                        case 5:
                            scaleOutputCondition = ScaleOutputConditionSetting.OneTimeOutputAtStableTimes;
                            break; //Output Condition 5
                        case 6:
                            scaleOutputCondition = ScaleOutputConditionSetting.OneTimeOutputAtStableTimesAndContinuousOutputAtUnstableTimes;
                            break; //Output Condition 6
                        case 7:
                            scaleOutputCondition = ScaleOutputConditionSetting.PushDownKeyForOneTimeOutputAtStableTimes;
                            break; //Output Condition 7
                    }
                } else if (mScaleType == ScaleType.MGTS) {
                    switch (index) {
                        case 0:
                            scaleOutputCondition = ScaleOutputConditionSetting.ContinuousOutputAtAllTimes;
                            break; //Output Condition 1
                        case 1:
                            scaleOutputCondition = ScaleOutputConditionSetting.ContinuousOutputAtStableTimes;
                            break; //Output Condition 2
                    }
                }
                Toast.makeText(ScaleActivity.this, Objects.requireNonNull(scaleOutputCondition).toString(), Toast.LENGTH_SHORT).show();
                mScale.updateOutputConditionSetting(scaleOutputCondition);
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
        setPadding(findViewById(R.id.BottomLinerLayout));

        if(mScale == null) {


            String identifier = getIntent().getStringExtra(IDENTIFIER_BUNDLE_KEY);
            ConnectionInfo.InterfaceType interfaceType = ConnectionInfo.InterfaceType.valueOf(getIntent().getStringExtra(INTERFACE_TYPE_BUNDLE_KEY));

            StarDeviceManager starDeviceManager = new StarDeviceManager(ScaleActivity.this);


            switch (interfaceType) {
                default:
                case BLE:
                    connectionInfo = new ConnectionInfo.Builder()
                            .setBleInfo(identifier)
                            .build();
                    break;
                case USB:
                    connectionInfo = new ConnectionInfo.Builder()
                            .setUsbInfo(identifier)
                            .setBaudRate(1200)
                            .build();
                    break;
            }

            mScale = starDeviceManager.createScale(connectionInfo);
            mScale.connect(mScaleCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mScale != null) {
            mScale.disconnect();
        }
    }

    private final ScaleCallback mScaleCallback = new ScaleCallback() {
        @Override
        public void onConnect(Scale scale, int status) {
            boolean connectSuccess = false;

            switch (status) {
                case Scale.CONNECT_SUCCESS:
                    connectSuccess = true;
                    Toast.makeText(ScaleActivity.this, "Connect success.", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.CONNECT_NOT_AVAILABLE:
                    Toast.makeText(ScaleActivity.this, "Failed to connect. (Not available)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.CONNECT_ALREADY_CONNECTED:
                    Toast.makeText(ScaleActivity.this, "Failed to connect. (Already connected)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.CONNECT_TIMEOUT:
                    Toast.makeText(ScaleActivity.this, "Failed to connect. (Timeout)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.CONNECT_READ_WRITE_ERROR:
                    Toast.makeText(ScaleActivity.this, "Failed to connect. (Read Write error)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.CONNECT_NOT_SUPPORTED:
                    Toast.makeText(ScaleActivity.this, "Failed to connect. (Not supported device)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.CONNECT_NOT_GRANTED_PERMISSION:
                    Toast.makeText(ScaleActivity.this, "Failed to connect. (Not granted permission)", Toast.LENGTH_SHORT).show();
                    break;

                default:
                case Scale.CONNECT_UNEXPECTED_ERROR:
                    Toast.makeText(ScaleActivity.this, "Failed to connect. (Unexpected error)", Toast.LENGTH_SHORT).show();
                    break;
            }

            if(!connectSuccess) {
                mScale = null;
                finish();
            }
        }

        @Override
        public void onDisconnect(Scale scale, int status) {
            mScale = null;

            switch(status) {
                case Scale.DISCONNECT_SUCCESS:
                    Toast.makeText(ScaleActivity.this, "Disconnect success.", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.DISCONNECT_NOT_CONNECTED:
                    Toast.makeText(ScaleActivity.this, "Failed to disconnect. (Not connected)", Toast.LENGTH_SHORT).show();
                    finish();
                    break;

                case Scale.DISCONNECT_TIMEOUT:
                    Toast.makeText(ScaleActivity.this, "Failed to disconnect. (Timeout)", Toast.LENGTH_SHORT).show();
                    finish();
                    break;

                case Scale.DISCONNECT_READ_WRITE_ERROR:
                    Toast.makeText(ScaleActivity.this, "Failed to disconnect. (Read Write error)", Toast.LENGTH_SHORT).show();
                    finish();
                    break;

                case Scale.DISCONNECT_UNEXPECTED_ERROR:
                    Toast.makeText(ScaleActivity.this, "Failed to disconnect. (Unexpected error)", Toast.LENGTH_SHORT).show();
                    finish();
                    break;

                default:
                case Scale.DISCONNECT_UNEXPECTED_DISCONNECTION:
                    Toast.makeText(ScaleActivity.this, "Unexpected disconnection.", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }

        @Override
        public void onReadScaleData(Scale scale, ScaleData scaleData) {
            if(scaleData.getStatus() == ScaleData.Status.ERROR) { // Error
                String text1 = "0 [INVALID]";
                mWeightTextView.setText(text1);
                String  text2 = "Status: ERROR\nData Type: INVALID\n";
                if(mScaleType == ScaleType.MGS) {
                    text2 += "Comparator Result: INVALID\n";
                }
                mStateTextView.setText(text2);
            } else {
                String weight = String.format(Locale.US,"%."+ scaleData.getNumberOfDecimalPlaces() +"f", scaleData.getWeight());
                String unit = scaleData.getUnit().toString();

                String weightStr = weight + " [" + unit + "]";

                mWeightTextView.setText(weightStr);

                String statusStr = "";
                statusStr += "Status: " + scaleData.getStatus() + "\n";
                statusStr += "Data Type: " + scaleData.getDataType() + "\n";
                if(mScaleType == ScaleType.MGS) {
                    statusStr += "Comparator Result: " + scaleData.getComparatorResult() + "\n";
                }

                mStateTextView.setText(statusStr);
            }
        }

        @Override
        public void onUpdateSetting(Scale scale, ScaleSetting scaleSetting, int status) {
            if (scaleSetting == ScaleSetting.ZeroPointAdjustment) {
                switch(status) {
                    case Scale.UPDATE_SETTING_SUCCESS:
                        Toast.makeText(ScaleActivity.this, "Succeeded.", Toast.LENGTH_SHORT).show();
                        break;

                    case Scale.UPDATE_SETTING_NOT_CONNECTED:
                        Toast.makeText(ScaleActivity.this, "Failed. (Not connected)", Toast.LENGTH_SHORT).show();
                        break;

                    case Scale.UPDATE_SETTING_REQUEST_REJECTED:
                        Toast.makeText(ScaleActivity.this, "Failed. (Request rejected)", Toast.LENGTH_SHORT).show();
                        break;

                    case Scale.UPDATE_SETTING_TIMEOUT:
                        Toast.makeText(ScaleActivity.this, "Failed. (Timeout)", Toast.LENGTH_SHORT).show();
                        break;

                    case Scale.UPDATE_SETTING_ALREADY_EXECUTING:
                        Toast.makeText(ScaleActivity.this, "Failed. (Already executing)", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                    case Scale.UPDATE_SETTING_UNEXPECTED_ERROR:
                        Toast.makeText(ScaleActivity.this, "Failed. (Unexpected error)", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }

        @Override
        public void onUpdateOutputConditionSetting(Scale scale, ScaleOutputConditionSetting scaleOutputConditionSetting, int status) {
            switch(status) {
                case Scale.UPDATE_SETTING_SUCCESS:
                    Toast.makeText(ScaleActivity.this, "Succeeded.", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.UPDATE_SETTING_NOT_CONNECTED:
                    Toast.makeText(ScaleActivity.this, "Failed. (Not connected)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.UPDATE_SETTING_REQUEST_REJECTED:
                    Toast.makeText(ScaleActivity.this, "Failed. (Request rejected)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.UPDATE_SETTING_TIMEOUT:
                    Toast.makeText(ScaleActivity.this, "Failed. (Timeout)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.UPDATE_SETTING_NOT_SUPPORTED:
                    Toast.makeText(ScaleActivity.this, "Failed. (Not supported)", Toast.LENGTH_SHORT).show();
                    break;

                case Scale.UPDATE_SETTING_ALREADY_EXECUTING:
                    Toast.makeText(ScaleActivity.this, "Failed. (Already executing)", Toast.LENGTH_SHORT).show();
                    break;

                default:
                case Scale.UPDATE_SETTING_UNEXPECTED_ERROR:
                    Toast.makeText(ScaleActivity.this, "Failed. (Unexpected error)", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
