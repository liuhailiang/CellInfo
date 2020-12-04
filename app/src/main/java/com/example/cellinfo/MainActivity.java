package com.example.cellinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;

import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoWcdma;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.CellInfo;
import android.content.Context;
import android.telephony.CellInfoLte;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;
import android.graphics.Color;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    /**
     * permissions request code
     */
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    protected SignalStrengthListener signalStrengthListener;
    protected TelephonyManager tm;
    protected List<CellInfo> cellInfoList;
    protected Context context;

    protected String[] parts;
    protected String datastr;
    protected String srsrp, srsrq, srssnr, scqi, scellPci, scellCi, scellBands, scellBaW, scellEarfcn, scellRssi, scellTi;
    protected String sgsmCid, sgsmdBid, sgsmArfcn, sgsmLac, sgsmdBit, sgsmRssi, sgsmTi;
    protected String sumtsCid, sumtsLac, sumtsUarfnc, sumtsRscp, sumtsEcno;

    protected int cellPci = 0;
    protected int cellCi = 0;
    protected int cellEarfcn = 0;
    protected int cellBaW  = 0;
    protected int[] cellBands;
    protected int cellCqi = 0;
    protected int cellRsrp  = 0;
    protected int cellRsrq  = 0;
    protected int cellRssi  = 0;
    protected int cellRssnr  = 0;
    protected int cellTi  = 0;

    protected int cellgsmCid = 0;
    protected int cellgsmBid= 0;
    protected int cellgsmRssi = 0;
    protected int cellgsmArfcn = 0;
    protected int cellgsmBit= 0;
    protected int cellgsmLac = 0;
    protected int cellgsmTi = 0;

    protected int cellumtsCid = 0;
    protected int cellumtsLac = 0;
    protected int cellumtsUarfcn = 0;
    protected int cellumtsRscp = 0;
    protected int cellumtsEcno = 0;

    protected TextView tvSignalStrength, tvlabel1, tvdata1, tvlabel2, tvdata2, tvlabel3, tvdata3, tvlabel4, tvdata4, tvlabel5, tvdata5;
    protected TextView tvlabel6, tvdata6, tvlabel7, tvdata7, tvlabel8, tvdata8, tvlabel9, tvdata9, tvlabel10, tvdata10, tvlabel11, tvdata11;
    protected TextView tv, tvt, tv1, tv2, API;

    /**
     * matyt reikia zinoti globaliai koks telefono API
     */
    int sdk_version_number;

    private static final String FILE_NAME = "datalog.txt";
    FileOutputStream fos = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Give permissions
         * problema su funkcija onSignalStrength -> getAllCellInfo kadangi nemato, kad jau suteiktas leidimas naudotis galimyb4mis
         */
        checkPermissions();

        /**
         * Retreive Phone API level to accurate support of function for diffrent devices
         * Minimum support API level 17. Lower is not suitable for this app. 2012 year.
         */
        sdk_version_number = Build.VERSION.SDK_INT;
        API = (TextView)findViewById(R.id.API);
        API.setText(Integer.toString(sdk_version_number));

        tvSignalStrength = (TextView) findViewById(R.id.signalValue);

        tvlabel1 = (TextView) findViewById(R.id.Label1);
        tvdata1 = (TextView) findViewById(R.id.data1);
        tvlabel2  = (TextView) findViewById(R.id.Label2);
        tvdata2  = (TextView) findViewById(R.id.data2);
        tvlabel3  = (TextView) findViewById(R.id.Label3);
        tvdata3  = (TextView) findViewById(R.id.data3);
        tvlabel4  = (TextView) findViewById(R.id.Label4);
        tvdata4  = (TextView) findViewById(R.id.data4);
        tvlabel5  = (TextView) findViewById(R.id.Label5);
        tvdata5  = (TextView) findViewById(R.id.data5);
        tvlabel6 = (TextView) findViewById(R.id.Label6);
        tvdata6 = (TextView) findViewById(R.id.data6);
        tvlabel7  = (TextView) findViewById(R.id.Label7);
        tvdata7  = (TextView) findViewById(R.id.data7);
        tvlabel8  = (TextView) findViewById(R.id.Label8);
        tvdata8  = (TextView) findViewById(R.id.data8);
        tvlabel9  = (TextView) findViewById(R.id.Label9);
        tvdata9  = (TextView) findViewById(R.id.data9);
        tvlabel10  = (TextView) findViewById(R.id.Label10);
        tvdata10  = (TextView) findViewById(R.id.data10);
        tvlabel11  = (TextView) findViewById(R.id.Label11);
        tvdata11  = (TextView) findViewById(R.id.data11);

        updateSignalStrengthTextLTE(-141); // initialize with 'Poor'
        updateSignalStrengthTextGSM(-110); // initialize with 'Poor'
        updateSignalStrengthTextUMTS(-124); // initialize with 'Poor'

        // Start the signal strength listener

        signalStrengthListener = new SignalStrengthListener();
        ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).listen(signalStrengthListener, SignalStrengthListener.LISTEN_CALL_STATE |
                SignalStrengthListener.LISTEN_DATA_ACTIVITY |
                SignalStrengthListener.LISTEN_DATA_CONNECTION_STATE |
                SignalStrengthListener.LISTEN_SERVICE_STATE |
                SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS |
                SignalStrengthListener.LISTEN_CELL_INFO |
                SignalStrengthListener.LISTEN_CELL_LOCATION);


    }

    protected class SignalStrengthListener extends PhoneStateListener {

        @SuppressLint("MissingPermission")
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            int res;

            tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            try {
                cellInfoList = tm.getAllCellInfo();
                for (CellInfo cellInfo : cellInfoList) {

                    if (cellInfo instanceof CellInfoLte) {
                        // cast to CellInfoLte and call all the CellInfoLte methods you need
                        // Gets the LTE PCI: (returns Physical Cell Id 0..503, Integer.MAX_VALUE if unknown)
                        cellPci = ((CellInfoLte) cellInfo).getCellIdentity().getPci();
                        cellCi = ((CellInfoLte) cellInfo).getCellIdentity().getCi();
                        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            //cellBands = ((CellInfoLte) cellInfo).getCellIdentity().getBands();
                            //Log.d("this is my array", "arr: " + Arrays.toString(cellBands));
                            //System.out.println("arr: " + Arrays.toString(cellBands));
                        //}
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            cellBaW = ((CellInfoLte) cellInfo).getCellIdentity().getBandwidth();
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            cellEarfcn = ((CellInfoLte) cellInfo).getCellIdentity().getEarfcn();
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cellCqi = ((CellInfoLte) cellInfo).getCellSignalStrength().getCqi();
                            cellRsrp = ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrp();
                            cellRsrq = ((CellInfoLte) cellInfo).getCellSignalStrength().getRsrq();
                            cellRssnr = ((CellInfoLte) cellInfo).getCellSignalStrength().getRssnr();

                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            cellRssi = ((CellInfoLte) cellInfo).getCellSignalStrength().getRssi();
                        }
                        cellTi = ((CellInfoLte) cellInfo).getCellSignalStrength().getTimingAdvance();
                    }
                    if (cellInfo instanceof CellInfoWcdma) {
                        // cast to CellInfoLte and call all the CellInfoLte methods you need
                        // Gets the LTE PCI: (returns Physical Cell Id 0..503, Integer.MAX_VALUE if unknown)
                        cellumtsCid = ((CellInfoWcdma) cellInfo).getCellIdentity().getCid();
                        cellumtsLac = ((CellInfoWcdma) cellInfo).getCellIdentity().getLac();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            cellumtsUarfcn = ((CellInfoWcdma) cellInfo).getCellIdentity().getUarfcn();
                        }
                        cellumtsRscp = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getDbm();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            cellumtsEcno = ((CellInfoWcdma) cellInfo).getCellSignalStrength().getEcNo();
                        }
                    }

                    if (cellInfo instanceof CellInfoGsm) {
                        // cast to CellInfoLte and call all the CellInfoLte methods you need
                        // Gets the LTE PCI: (returns Physical Cell Id 0..503, Integer.MAX_VALUE if unknown)
                        cellgsmCid = ((CellInfoGsm) cellInfo).getCellIdentity().getCid();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            cellgsmBid = ((CellInfoGsm) cellInfo).getCellIdentity().getBsic();
                            cellgsmArfcn = ((CellInfoGsm) cellInfo).getCellIdentity().getArfcn();
                        }
                        cellgsmLac = ((CellInfoGsm) cellInfo).getCellIdentity().getLac();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            cellgsmBit = ((CellInfoGsm) cellInfo).getCellSignalStrength().getBitErrorRate();
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            cellgsmRssi = ((CellInfoGsm) cellInfo).getCellSignalStrength().getRssi();
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            cellgsmTi = ((CellInfoGsm) cellInfo).getCellSignalStrength().getTimingAdvance();
                        }
                    }
                }
            } catch (Exception e) {
                Log.d("SignalStrength", "Exception: " + e.getMessage());
            }

            //part[0] = "Signalstrength:"  _ignore this, it's just the title_
            //parts[1] = GsmSignalStrength
            //parts[2] = GsmBitErrorRate
            //parts[3] = CdmaDbm
            //parts[4] = CdmaEcio
            //parts[5] = EvdoDbm
            //parts[6] = EvdoEcio
            //parts[7] = EvdoSnr
            //parts[8] = LteSignalStrength
            //parts[9] = LteRsrp
            //parts[10] = LteRsrq
            //parts[11] = LteRssnr
            //parts[12] = LteCqi
            //parts[13] = gsm|lte|cdma
            //parts[14] = _not really sure what this number is_
            datastr = signalStrength.toString();
            parts = datastr.split(" ");

            if (parts == null || parts.length < 13) {
                return;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                cellRsrp = Integer.parseInt(parts[9]);
                if (cellRsrp == 2147483647) {
                    cellRsrp = -141;    //poor
                }

                cellRsrq = Integer.parseInt(parts[10]);
                if (cellRsrq == 2147483647) {
                    cellRsrq = -30; //poor
                }

                cellRssnr = Integer.parseInt(parts[11]);
                cellCqi = Integer.parseInt(parts[12]);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                cellgsmRssi = Integer.parseInt(parts[1]);
                cellgsmRssi = 2*cellgsmRssi-113; //keiciam is ASU i dBm
                if (cellgsmRssi == 2147483647) {
                    cellgsmRssi = -110; //poor
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                cellgsmBit = Integer.parseInt(parts[2]);
                if (cellgsmBit == 2147483647) {
                    cellgsmBit = -20; //poor
                }
            }

            if (cellCi == 2147483647)
                cellCi = 0;
            if (cellBaW == 2147483647)
                cellBaW = 0;
            if (cellCqi == 2147483647)
                cellCqi = 0;
            if (cellRssnr == 2147483647)
                cellRssnr = 0;
            if (cellTi == 2147483647)
                cellTi = 0;
            if (cellgsmBit == 2147483647)
                cellgsmBit = 0;
            if (cellgsmTi == 2147483647)
                cellgsmTi = 0;

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strDate = sdf.format(c.getTime());
            System.out.println("arr: " + strDate);

            String data_to_save=strDate;

            try {
                fos = openFileOutput(FILE_NAME, Context.MODE_APPEND);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
           }

            String networktype_str = getNetworkType();

            if (networktype_str == "LTE") {

                srsrp = String.valueOf(cellRsrp);
                srsrq = String.valueOf(cellRsrq);
                srssnr = String.valueOf(cellRssnr);
                scqi = String.valueOf(cellCqi);
                scellPci = String.valueOf(cellPci);
                scellCi = String.valueOf(cellCi);
                scellBands = getBand(cellEarfcn);
                //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    //scellBands = "Old phone";
                //}
                scellBaW = String.valueOf(cellBaW);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    scellBaW = "Old phone";
                }
                scellEarfcn = String.valueOf(cellEarfcn);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    scellEarfcn = "Old phone";
                }
                scellRssi = String.valueOf(cellRssi);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    scellRssi = "Old phone";
                }
                scellTi = String.valueOf(cellTi);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    scellTi = "Old phone";
                }

                res = updateSignalStrengthTextLTE(cellRsrp);

                tvlabel1.setVisibility(TextView.VISIBLE);
                tvdata1.setVisibility(TextView.VISIBLE);
                tvlabel2.setVisibility(TextView.VISIBLE);
                tvdata2.setVisibility(TextView.VISIBLE);
                tvlabel3.setVisibility(TextView.VISIBLE);
                tvdata3.setVisibility(TextView.VISIBLE);
                tvlabel4.setVisibility(TextView.VISIBLE);
                tvdata4.setVisibility(TextView.VISIBLE);
                tvlabel5.setVisibility(TextView.VISIBLE);
                tvdata5.setVisibility(TextView.VISIBLE);
                tvlabel6.setVisibility(TextView.VISIBLE);
                tvdata6.setVisibility(TextView.VISIBLE);
                tvlabel7.setVisibility(TextView.VISIBLE);
                tvdata7.setVisibility(TextView.VISIBLE);
                tvlabel8.setVisibility(TextView.VISIBLE);
                tvdata8.setVisibility(TextView.VISIBLE);
                tvlabel9.setVisibility(TextView.VISIBLE);
                tvdata9.setVisibility(TextView.VISIBLE);
                tvlabel10.setVisibility(TextView.VISIBLE);
                tvdata10.setVisibility(TextView.VISIBLE);
                tvlabel11.setVisibility(TextView.VISIBLE);
                tvdata11.setVisibility(TextView.VISIBLE);

                tvlabel1.setText("LTE Phy ID");
                tvdata1.setText(scellPci);
                tvlabel2.setText("LTE ID");
                tvdata2.setText(scellCi);
                tvlabel3.setText("EARFCN");
                tvdata3.setText(scellEarfcn);
                tvlabel4.setText("Band");
                tvdata4.setText(scellBands);
                tvlabel5.setText("BandWidth");
                tvdata5.setText(scellBaW);
                tvlabel6.setText("LTE CQI");
                tvdata6.setText(scqi);
                tvlabel7.setText("LTE RSRP");
                tvdata7.setText(srsrp);
                tvlabel8.setText("LTE RSRQ");
                tvdata8.setText(srsrq);
                tvlabel9.setText("LTE RSSNR");
                tvdata9.setText(srssnr);
                tvlabel10.setText("LTE RSSI");
                tvdata10.setText(scellRssi);
                tvlabel11.setText("Timing Adv");
                tvdata11.setText(scellTi);


                data_to_save = strDate + "," + "LTE," + res + "," + scellPci + "," + scellCi + ","
                        + scellEarfcn + "," + scellBands + "," + scellBaW + "," + scqi + "," +
                        srsrp + "," + srsrq + "," + srssnr + "," + scellRssi + "," + scellTi + "\n";


            } else  if (networktype_str == "EDGE" || networktype_str == "GPRS") {

                sgsmCid = String.valueOf(cellgsmCid);
                sgsmdBid = String.valueOf(cellgsmBid);
                sgsmArfcn = String.valueOf(cellgsmArfcn);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    sgsmdBid = "Old phone";
                    sgsmArfcn = "Old phone";
                }
                sgsmLac = String.valueOf(cellgsmLac);
                sgsmdBit = String.valueOf(cellgsmBit);
                sgsmRssi = String.valueOf(cellgsmRssi);
                sgsmTi = String.valueOf(cellgsmTi);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        sgsmTi = "Old phone";
                }

                updateSignalStrengthTextGSM(cellgsmRssi);

                tvlabel1.setVisibility(TextView.VISIBLE);
                tvdata1.setVisibility(TextView.VISIBLE);
                tvlabel2.setVisibility(TextView.VISIBLE);
                tvdata2.setVisibility(TextView.VISIBLE);
                tvlabel3.setVisibility(TextView.VISIBLE);
                tvdata3.setVisibility(TextView.VISIBLE);
                tvlabel4.setVisibility(TextView.VISIBLE);
                tvdata4.setVisibility(TextView.VISIBLE);
                tvlabel5.setVisibility(TextView.VISIBLE);
                tvdata5.setVisibility(TextView.VISIBLE);
                tvlabel6.setVisibility(TextView.VISIBLE);
                tvdata6.setVisibility(TextView.VISIBLE);
                tvlabel7.setVisibility(TextView.VISIBLE);
                tvdata7.setVisibility(TextView.VISIBLE);
                tvlabel8.setVisibility(TextView.INVISIBLE);
                tvdata8.setVisibility(TextView.INVISIBLE);
                tvlabel9.setVisibility(TextView.INVISIBLE);
                tvdata9.setVisibility(TextView.INVISIBLE);
                tvlabel10.setVisibility(TextView.INVISIBLE);
                tvdata10.setVisibility(TextView.INVISIBLE);
                tvlabel11.setVisibility(TextView.INVISIBLE);
                tvdata11.setVisibility(TextView.INVISIBLE);

                tvlabel1.setText("Base ID");
                tvdata1.setText(sgsmdBid);
                tvlabel2.setText("Cell ID");
                tvdata2.setText(sgsmCid);
                tvlabel3.setText("ARFCN");
                tvdata3.setText(sgsmArfcn);
                tvlabel4.setText("Area ID");
                tvdata4.setText(sgsmLac);
                tvlabel5.setText("Bit Err Rate");
                tvdata5.setText(sgsmdBit);
                tvlabel6.setText("GSM RSSI");
                tvdata6.setText(sgsmRssi);
                tvlabel7.setText("Timing Adv");
                tvdata7.setText(sgsmTi);

            } else  if (networktype_str == "HSPA" || networktype_str == "UMTS" || networktype_str == "HSPA+" || networktype_str == "HSDPA" || networktype_str == "HSPUPA") {

                sumtsCid = String.valueOf(cellumtsCid);
                sumtsLac = String.valueOf(cellumtsLac);
                sumtsUarfnc = getBand(cellumtsUarfcn);//String.valueOf(cellumtsUarfcn);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    sumtsUarfnc = "Old phone";
                }
                sumtsRscp = String.valueOf(cellumtsRscp);
                sumtsEcno = String.valueOf(cellumtsEcno);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    sumtsEcno = "Old phone";
                }

                res = updateSignalStrengthTextUMTS(cellumtsRscp);

                tvlabel1.setVisibility(TextView.VISIBLE);
                tvdata1.setVisibility(TextView.VISIBLE);
                tvlabel2.setVisibility(TextView.VISIBLE);
                tvdata2.setVisibility(TextView.VISIBLE);
                tvlabel3.setVisibility(TextView.VISIBLE);
                tvdata3.setVisibility(TextView.VISIBLE);
                tvlabel4.setVisibility(TextView.VISIBLE);
                tvdata4.setVisibility(TextView.VISIBLE);
                tvlabel5.setVisibility(TextView.VISIBLE);
                tvdata5.setVisibility(TextView.VISIBLE);
                tvlabel6.setVisibility(TextView.INVISIBLE);
                tvdata6.setVisibility(TextView.INVISIBLE);
                tvlabel7.setVisibility(TextView.INVISIBLE);
                tvdata7.setVisibility(TextView.INVISIBLE);
                tvlabel8.setVisibility(TextView.INVISIBLE);
                tvdata8.setVisibility(TextView.INVISIBLE);
                tvlabel9.setVisibility(TextView.INVISIBLE);
                tvdata9.setVisibility(TextView.INVISIBLE);
                tvlabel10.setVisibility(TextView.INVISIBLE);
                tvdata10.setVisibility(TextView.INVISIBLE);
                tvlabel11.setVisibility(TextView.INVISIBLE);
                tvdata11.setVisibility(TextView.INVISIBLE);

                tvlabel1.setText("Cell ID");
                tvdata1.setText(sumtsCid);
                tvlabel2.setText("Area ID");
                tvdata2.setText(sumtsLac);
                tvlabel3.setText("UARFCN");
                tvdata3.setText(sumtsUarfnc);
                tvlabel4.setText("UMTS RSCP");
                tvdata4.setText(sumtsRscp);
                tvlabel5.setText("UMTS Ec/No");
                tvdata5.setText(sumtsEcno);

                data_to_save = strDate + "," + "UMTS," + res + "," + sumtsCid + "," + sumtsLac +
                        "," + sumtsUarfnc + "," + sumtsRscp + "," + sumtsEcno + "\n";

            }

            try {
                fos.write(data_to_save.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onDataActivity(int direction) {
            super.onDataActivity(direction);

            tv1 = (TextView)findViewById(R.id.data_activity);
            tv1.setText(getDataState(direction));
            tv1.invalidate();  // for refreshment

        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            super.onDataConnectionStateChanged(state, networkType);

            tv2 = (TextView)findViewById(R.id.state);
            tv2.setText(getDataState(state));
            tv2.invalidate();  // for refreshment
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);

            String networktype_str = getNetworkType();
            tv = (TextView)findViewById(R.id.name);
            tv.setText(networktype_str);
            tv.invalidate();  // for refreshment

            tvt = (TextView)findViewById(R.id.operator);
            tvt.setText(serviceState.getOperatorAlphaLong());
            tvt.invalidate();  // for refreshment
        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfo){
            super.onCellInfoChanged(cellInfo);
            Toast.makeText(getApplicationContext(), "Cell changed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCellLocationChanged(CellLocation location){
            super.onCellLocationChanged(location);
            Toast.makeText(getApplicationContext(), "Cell location changed", Toast.LENGTH_SHORT).show();
        }

    }

    public String getBand(int ARFCN) {
        String band_str = "";
        if (ARFCN > 0 && ARFCN < 599)
            band_str = "2100 band 1";
        if (ARFCN > 1200 && ARFCN < 1949)
            band_str = "1800+ band 3";
        if (ARFCN > 2750 && ARFCN < 3449)
            band_str = "2600 band 7";
        if (ARFCN > 3450 && ARFCN < 3799)
            band_str = "900 GSM band 8";
        if (ARFCN > 6150 && ARFCN < 6449)
            band_str = "800 DD band 20";
        if (ARFCN > 6600 && ARFCN < 7399)
            band_str = "3500 band 22";
        if (ARFCN > 9210 && ARFCN < 9659)
            band_str = "700 APT band 28";
        if (ARFCN > 9870 && ARFCN < 9919)
            band_str = "450 band 31";
        if (ARFCN > 9920 && ARFCN < 10359)
            band_str = "1500 L band 32";
        if (ARFCN > 36000 && ARFCN < 36199)
            band_str = "TD1900 band 33";
        if (ARFCN > 36200 && ARFCN < 36349)
            band_str = "TD 2000 band 34";
        if (ARFCN > 37750 && ARFCN < 38249)
            band_str = "TD 2600 band 38";
        if (ARFCN > 39650 && ARFCN < 41589)
            band_str = "TD 2600+ band 41";
        return band_str;
    }

    public String getDataState(int datastate) {
        String datastate_str = "";
        if (datastate == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).DATA_ACTIVITY_DORMANT) {
            datastate_str = getString(R.string.inactive);
        } else if (datastate == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).DATA_ACTIVITY_IN) {
            datastate_str = getString(R.string.in);
        } else if (datastate == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).DATA_ACTIVITY_OUT) {
            datastate_str = getString(R.string.out);
        } else if (datastate == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).DATA_ACTIVITY_INOUT) {
            datastate_str = getString(R.string.inout);
        } else if (datastate == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).DATA_DISCONNECTED){
            datastate_str = getString(R.string.turnedoff);
        } else {
            datastate_str = "Unknown";
        }
        return datastate_str;
    }

    protected int updateSignalStrengthTextLTE(int rsrp) {
        int res = 0;
        if (rsrp >= -70) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_excellent));
            tvSignalStrength.setTextColor(Color.parseColor("#2de309"));
            res = 4;
        } else if (-71 > rsrp && rsrp >= -80) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_good));
            tvSignalStrength.setTextColor(Color.parseColor("#f7e519"));
            res = 3;
        } else if (-81 > rsrp && rsrp >= -115) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_fair));
            tvSignalStrength.setTextColor(Color.parseColor("#f7cb19"));
            res = 2;
        } else {
            tvSignalStrength.setText(getResources().getString(R.string.signal_poor));
            tvSignalStrength.setTextColor(Color.parseColor("#f73e19"));
            res = 1;
        }
        return res;
    }

    protected void updateSignalStrengthTextGSM(int rssi) {
        if (rssi >= -70) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_excellent));
        } else if (-70 >= rssi && rssi >= -85) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_good));
        } else if (-86 >= rssi && rssi >= -100) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_fair));
        } else {
            tvSignalStrength.setText(getResources().getString(R.string.signal_poor));
        }
    }

    protected int updateSignalStrengthTextUMTS(int rscp) {
        int res = 0;
        if (rscp >= -60) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_excellent));
            res = 4;
        } else if (-60 > rscp && rscp >= -75) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_good));
            res = 3;
        } else if (-75 > rscp && rscp >= -85) {
            tvSignalStrength.setText(getResources().getString(R.string.signal_fair));
            res = 2;
        } else {
            tvSignalStrength.setText(getResources().getString(R.string.signal_poor));
            res = 1;
        }
        return res;
    }

    @SuppressLint("MissingPermission")
    public String getNetworkType() {
        int networktype = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getNetworkType();
        String network_str = "";
        if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_LTE) {
            network_str = "LTE";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_HSPA) {
            network_str = "HSPA";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_EDGE) {
            network_str = "EDGE";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_GPRS) {
            network_str = "GPRS";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_UMTS) {
            network_str = "UMTS";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_CDMA) {
            network_str = "CDMA";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_HSPAP) {
            network_str = "HSPA+";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_GSM) {
            network_str = "GSM";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_EDGE) {
            network_str = "EDGE";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_1xRTT) {
            network_str = "1xRTT";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_IDEN) {
            network_str = "IDEN";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_EVDO_0) {
            network_str = "EVDO_0";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_EVDO_A) {
            network_str = "EVDO_A";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_HSDPA) {
            network_str = "HSDPA";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_HSUPA) {
            network_str = "HSPUPA";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_EVDO_B) {
            network_str = "EVDO_B";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_EHRPD) {
            network_str = "EHRPD";
        } else if (networktype == ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).NETWORK_TYPE_NR) {
            network_str = "5G";
        } else {
            network_str = this.getString(R.string.unk);
        }
        return network_str;
    }

    //Checks the dynamically-controlled permissions and requests missing permissions from end user.

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions.toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,  grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index] + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                // Galime jungti ka norime
                // initialize();
                break;
        }
    }
}