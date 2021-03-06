package com.passkeyper.android.fragment.setup;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.passkeyper.android.R;
import com.passkeyper.android.UserPreferences;
import com.passkeyper.android.Vault;
import com.passkeyper.android.activity.LocalSetupActivity;
import com.passkeyper.android.auth.AuthData;
import com.passkeyper.android.auth.FingerprintAuthHelper;
import com.passkeyper.android.auth.SetupFingerprintDialog;
import com.passkeyper.android.fragment.AbstractLoginFragment;
import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vault.local.LocalVaultManager;

import java.util.Arrays;

/**
 * LoginFragment for finishing the setup of the local vault.
 */
public class LocalSetup4Fragment extends AbstractLoginFragment<LocalSetupActivity> implements SetupFingerprintDialog.FingerprintSetupListener {

    private static final String TAG = "Setup Step 4";

    private Switch fingerprintEnabled;
    private ImageView icon;
    private ProgressBar loading;

    @Override
    protected View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.local_setup_4_fragment, container, false);

        view.findViewById(R.id.back_btn).setOnClickListener(v -> loginFragmentActivity.pop());
        view.findViewById(R.id.finish_btn).setOnClickListener(v -> {
            char[] pass = loginFragmentActivity.getSetup2Fragment().getPassword();
            if (fingerprintEnabled.isChecked() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                setupFingerprint(pass);
            else
                setupVault(pass);
        });

        fingerprintEnabled = view.findViewById(R.id.fingerprint_enabled_switch);
        icon = view.findViewById(R.id.setup_icon);
        loading = view.findViewById(R.id.loading);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && FingerprintAuthHelper.isAvailable(getContext())) {
            fingerprintEnabled.setVisibility(View.VISIBLE);
            view.findViewById(R.id.no_settings_avail).setVisibility(View.GONE);
        } else {
            fingerprintEnabled.setVisibility(View.GONE);
            view.findViewById(R.id.no_settings_avail).setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onCancelled() {
        Toast.makeText(getContext(), R.string.fingerprint_setup_cancelled, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess() {
        //now that the fingerprint is done, setup the vault
        setupVault(loginFragmentActivity.getSetup2Fragment().getPassword());
    }

    @Override
    public void onFailure() {
        Toast.makeText(getContext(), R.string.fingerprint_failed, Toast.LENGTH_LONG).show();
    }

    //TODO: determine if this is a potential bug or not
    @SuppressLint("StaticFieldLeak")
    private void setupVault(char[] pass) {
        //must be done async or else UI hangs
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                icon.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                char[] securityAnswer = null;
                try {
                    AuthData authData = new AuthData(getContext());
                    UserPreferences userPreferences = new UserPreferences(getContext());
                    //setup the data needed outside of the database
                    String securityQuestion = loginFragmentActivity.getSetup3Fragment().getQuestion();
                    securityAnswer = loginFragmentActivity.getSetup3Fragment().getAnswer();
                    //start with the encrypted password because that may cause an error
                    authData.setEncryptedPassword(pass, securityQuestion, securityAnswer);
                    authData.setSecurityQuestion(securityQuestion);
                    //save this fragments user preferences
                    userPreferences.setFingerprintEnabled(fingerprintEnabled.isChecked());
                    //setup and log into database
                    Vault vault = Vault.get();
                    LocalVaultManager.setupLocalDb(getContext(), pass, securityQuestion, securityAnswer);
                    vault.signIn(getContext(), pass);
                    //update the recovery data in the database
                    VaultManager.RecoveryData recoveryData = vault.getManager().getRecoveryData();
                    recoveryData.setSecurityQuestion(securityQuestion);
                    recoveryData.setSecurityAnswer(securityAnswer);
                    vault.getManager().updateRecoveryData(recoveryData);
                    recoveryData.free();
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Unable to setup vault", e);
                    return false;
                } finally {
                    Arrays.fill(pass, '\0');
                    if (securityAnswer != null)
                        Arrays.fill(securityAnswer, '\0');
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                loading.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);

                if (success)
                    loginFragmentActivity.redirectAndFinish();
                else {
                    //if unsuccessful, notify the user of the error and try to run setup again
                    Toast.makeText(getContext(), R.string.setup_fatal_error, Toast.LENGTH_LONG).show();
                    loginFragmentActivity.redirectAndFinish();
                }
            }
        }.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupFingerprint(char[] pass) {
        SetupFingerprintDialog setupDialog = new SetupFingerprintDialog();
        setupDialog.setCancelable(false);
        setupDialog.setListener(this);
        setupDialog.setPassword(pass);
        setupDialog.show(getFragmentManager(), "Setup");
        //clear the password
        Arrays.fill(pass, '\0');
    }

}
