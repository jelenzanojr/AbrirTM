package cu.teamprogress.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.StringReader;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import cu.teamprogress.BuildConfig;
import cu.teamprogress.R;
import cu.teamprogress.permission.PermissionHandlerActivity;
import cu.teamprogress.viewmodel.MainViewModel;

public class OpenTMFragment extends Fragment implements View.OnClickListener{

    public static final String PACKAGE_NAME_TRANSFERMOVIL="cu.etecsa.cubacel.tr.tm";
    private final String GOT_RESULT = "com.blikoon.qrcodescanner.got_qr_scan_relult";
    private final String TEMPLATE_TM = "transfermovil://tm_compra_en_linea/action?id_transaccion=%s&importe=%s&moneda=%s&numero_proveedor=%s";

    private EditText linkEditText;
    private TextInputLayout linkTextInputLayout;
    private MainViewModel mainViewModel;

    private OnFragmentInteractionListener mListener;

    private final ActivityResultLauncher<Intent> qrActivityForResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String codeJsonTM = data.getStringExtra(GOT_RESULT);

                    if( codeJsonTM.isEmpty() || codeJsonTM.trim().isEmpty() ){
                        Toast.makeText(requireContext().getApplicationContext(), R.string.not_founded_qr, Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(isJsonValid(codeJsonTM)){
                        String enlace = create_link(codeJsonTM);
                        if (!enlace.isEmpty())
                            linkEditText.setText(enlace);
                        else
                            return;
                    }
                    else {
                        Toast.makeText(requireContext().getApplicationContext(), R.string.invalid_qr, Toast.LENGTH_LONG).show();
                        return;
                    }

                    createDialogOKNO(getString(R.string.information),getString(R.string.desea_abrir_tm)).show();
                }
            });

    private final ActivityResultLauncher<String[]> searchPermissionForResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                if (result.containsValue(false)) {
                    Toast.makeText(requireContext().getApplicationContext(), R.string.camera_permission_not_granted, Toast.LENGTH_LONG).show();
                }
                else {
                    Intent i = new Intent(requireContext(), QrCodeActivity.class);
                    qrActivityForResultLauncher.launch(i);
                }
            });

    private final TextWatcher peopleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s == null || s.toString().isEmpty() ) {
                linkTextInputLayout.setError(getText(R.string.error_codigo_vacio));
                mainViewModel.setLinkTM("");
                return;
            }
            linkTextInputLayout.setError(null);
            mainViewModel.setLinkTM(s.toString());
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        mainViewModel =
                new ViewModelProvider(this)
                        .get(MainViewModel.class);
        return inflater.inflate(R.layout.fragment_open_tm, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linkEditText = view.findViewById(R.id.etLinkTM);
        linkTextInputLayout = view.findViewById(R.id.tiLinkTM);
        view.findViewById(R.id.btnOpen).setOnClickListener(this);
        view.findViewById(R.id.btnClean).setOnClickListener(this);
        linkEditText.addTextChangedListener(peopleTextWatcher);

        ((TextView) view.findViewById(R.id.tvAppVersion)).setText(String.format(getString(R.string.version), BuildConfig.VERSION_NAME));

        if (!mainViewModel.getLinkTM().isEmpty())
            linkEditText.setText(mainViewModel.getLinkTM());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void openLink(){
        linkTextInputLayout.setError(null);

        if( !isAppInstall(PACKAGE_NAME_TRANSFERMOVIL,requireContext()) ){
            createDialog(getString(R.string.error_tm),getString(R.string.no_tiene_instalado_tm)).show();
            return;
        }

        if (linkEditText.getText() == null || linkEditText.getText().toString().isEmpty()) {
//            createDialog(getString(R.string.error_link), getString(R.string.formato_enlace_incorrecto)).show();
            linkTextInputLayout.setError(getText(R.string.error_codigo_vacio));
            return;
        }

        String URL_TRANSFERMOVIL = linkEditText.getText().toString();
        if (isJsonValid(URL_TRANSFERMOVIL)){
            URL_TRANSFERMOVIL = create_link(URL_TRANSFERMOVIL);
            if (!URL_TRANSFERMOVIL.isEmpty())
                linkEditText.setText(URL_TRANSFERMOVIL);
        }

        URL_TRANSFERMOVIL = mainViewModel.fixLinkTM(URL_TRANSFERMOVIL);
        try {
            Uri uri = Uri.parse(URL_TRANSFERMOVIL);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }catch(Exception e){
            System.out.println(e.getMessage());
            createDialog(getString(R.string.error_link), getString(R.string.enlace_incorrecto)).show();
        }
    }

    private String create_link(String codeJsonTM) {
        JsonReader reader = new JsonReader(new StringReader(codeJsonTM));
        reader.setLenient(true);
        String id_transaccion = "";
        String importe = "";
        String moneda = "";
        String numero_proveedor = "";
        String version = "";

        try {
            reader.beginObject();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext().getApplicationContext(), R.string.error_create_link, Toast.LENGTH_LONG).show();
            return "";
        }
        while(true){
            try {
                if (!reader.hasNext()) {
                    break;
                }
                String name = reader.nextName();
                switch (name){
                    case "id_transaccion":
                        id_transaccion = reader.nextString();
                        break;
                    case "importe":
                        importe = reader.nextString();
                        break;
                    case "moneda":
                        moneda = reader.nextString();
                        break;
                    case "numero_proveedor":
                        numero_proveedor = reader.nextString();
                        break;
                    case "version":
                        version = reader.nextString();
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext().getApplicationContext(), R.string.error_create_link, Toast.LENGTH_LONG).show();
                return "";
            }

        }
        try {
            reader.endObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String enlace = String.format(TEMPLATE_TM,id_transaccion,importe,moneda,numero_proveedor);
        return enlace;
    }

    private boolean isJsonValid(String json){
        final Gson gson = new Gson();
        try {
            gson.fromJson(json, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    public boolean isAppInstall(String namePackage, Context context) {

        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(namePackage, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void sharedLink(){

        if( linkEditText.getText() == null || linkEditText.getText().toString().trim().isEmpty() ) {
            Toast.makeText(requireContext(),"Enlace vacío",Toast.LENGTH_SHORT).show();
            return;
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, linkEditText.getText().toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    public AlertDialog createDialog(String tile,String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setTitle(tile)
                .setMessage(body)
                .setPositiveButton("Aceptar",
                        (dialog, which) -> {

                        });
/*                .setNegativeButton("CANCELAR",
                        (dialog, which) -> listener.onNegativeButtonClick());*/

        return builder.create();
    }

    public AlertDialog createDialogOKNO(String tile,String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setTitle(tile)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage(body)
                .setPositiveButton("Si",
                        (dialog, which) -> {
                            openLink();
                        })
                .setNegativeButton("No",
                        (dialog, which) -> {

                        });

        return builder.create();
    }

    private void clean(){
        linkEditText.setText(null);
//        mListener.changeFragment();
    }

    public void scanQr() {
        PermissionHandlerActivity activity = (PermissionHandlerActivity) requireActivity();
        if (activity.requestCameraPermission(searchPermissionForResultLauncher)) {
            Intent i = new Intent(requireContext(), QrCodeActivity.class);
            qrActivityForResultLauncher.launch(i);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_scan_qr) {
            scanQr();
        }
        else if( id == R.id.action_share){
            sharedLink();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.btnOpen) {
            openLink();
        } else if (id == R.id.btnClean) {
            clean();
        }

    }

    public interface OnFragmentInteractionListener{
        void changeFragment();
    }
}