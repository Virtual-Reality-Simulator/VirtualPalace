package kr.poturns.virtualpalace.mobiletest;

import android.os.Bundle;
import android.app.Activity;

import kr.poturns.virtualpalace.controller.PalaceApplication;
import kr.poturns.virtualpalace.controller.PalaceMaster;
import kr.poturns.virtualpalace.mobiletest.fragments.DriveRestTestFragment;
import kr.poturns.virtualpalace.mobiletest.fragments.DriveTestFragment;

/**
 * Created by Myungjin Kim on 2015-10-30.
 *
 * DriveAssistant test Activity
 */
public class DriveTestActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_test);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DriveRestTestFragment())
                .commit();

    }
}
