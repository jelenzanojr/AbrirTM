package cu.teamprogress.permission;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import cu.teamprogress.R;

public class PermissionHandlerActivity extends AppCompatActivity {

    private boolean checkForPermission(String permissionName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        return checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean requestPermission(String permission, int rationaleMessage, ActivityResultLauncher<String> requestPermissionLauncher) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkForPermission(permission))
            return true;

        if (shouldShowRequestPermissionRationale(permission))
            new AlertDialog.Builder(this)
                    .setTitle(R.string.need_for_permission)
                    .setMessage(rationaleMessage)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setPositiveButton(R.string.ok, (dialog, which) -> requestPermissionLauncher.launch(permission))
                    .setNegativeButton(R.string.no, null)
                    .show();
        else
            requestPermissionLauncher.launch(permission);

        return false;
    }
    private boolean requestPermissions(String[] permissions, int rationaleMessage, ActivityResultLauncher<String[]> requestPermissionLauncher) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        List<String> noGranted = new ArrayList<>();
        for (String permission: permissions) {
            if (!checkForPermission(permission))
                noGranted.add(permission);
        }

        if (noGranted.size() == 0)
            return true;

        if (shouldShowRequestPermissionRationale(noGranted.get(0)))
            new AlertDialog.Builder(this)
                    .setTitle(R.string.need_for_permission)
                    .setMessage(rationaleMessage)
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setPositiveButton(R.string.ok, (dialog, which) -> requestPermissionLauncher.launch(noGranted.toArray(new String[0])))
                    .setNegativeButton(R.string.no, null)
                    .show();
        else
            requestPermissionLauncher.launch(noGranted.toArray(new String[0]));

        return false;
    }

    public boolean requestCameraPermission(ActivityResultLauncher<String[]> requestPermissionLauncher) {
        return requestPermissions(
                new String[] { Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE },
                R.string.need_for_camera_permission, requestPermissionLauncher);
    }
}
